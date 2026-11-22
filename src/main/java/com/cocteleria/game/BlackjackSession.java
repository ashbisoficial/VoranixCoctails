package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Veintiuno con un crupier humano (el "staff") que controla la mesa y de 1 a 3 jugadores.
 * Acciones: 1 = ser crupier, 2 = unirme como jugador, 3 = repartir (crupier), 4 = pedir carta, 5 = plantarme,
 * 6 = pedir carta el crupier, 7 = plantarse el crupier (resuelve), 8 = forzar a plantarse a todos (crupier),
 * 9 = salir (el crupier cierra la mesa).
 */
public class BlackjackSession extends AbstractSession {
    private static final int LOBBY = 0, PLAYING = 1, DEALER = 2, RESULT = 3;
    public static final int MAX_PLAYERS = 3;

    private static final class Hand {
        final List<Integer> cards = new ArrayList<>();
        final String name;
        boolean done;
        String result = "";

        Hand(String name) {
            this.name = name;
        }
    }

    private final Random random = new Random();
    private List<Integer> deck = Cards.newDeck(random);
    private final Map<UUID, Hand> hands = new LinkedHashMap<>();
    private final List<Integer> dealerCards = new ArrayList<>();
    private UUID dealer;
    private String dealerName = "";
    private int phase = LOBBY;
    private long lastAction = 0;
    private String msg = "Mesa libre: alguien debe ser el crupier y de 1 a 3 jugadores se unen.";

    public BlackjackSession() {
        super(BarGame.BLACKJACK);
    }

    private int draw() {
        if (deck.size() < 15) {
            deck = Cards.newDeck(random);
        }
        return deck.remove(deck.size() - 1);
    }

    private void resetTable(String why) {
        hands.clear();
        dealerCards.clear();
        dealer = null;
        dealerName = "";
        phase = LOBBY;
        msg = why;
    }

    @Override
    public void action(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
                       int type, int a, int b, float f) {
        UUID id = player.getUUID();
        String name = player.getName().getString();
        boolean isDealer = id.equals(dealer);
        Hand mine = hands.get(id);
        lastAction = level.getGameTime();

        switch (type) {
            case 1 -> {
                if (dealer == null && mine == null) {
                    dealer = id;
                    dealerName = name;
                    msg = name + " es el crupier. Esperando jugadores (hasta " + MAX_PLAYERS + ").";
                }
            }
            case 2 -> {
                if (!isDealer && mine == null && hands.size() < MAX_PLAYERS && (phase == LOBBY || phase == RESULT)) {
                    hands.put(id, new Hand(name));
                    msg = name + " se une a la mesa (" + hands.size() + "/" + MAX_PLAYERS + ").";
                }
            }
            case 3 -> {
                if (isDealer && (phase == LOBBY || phase == RESULT) && !hands.isEmpty()) {
                    dealerCards.clear();
                    dealerCards.add(draw());
                    dealerCards.add(draw());
                    for (Hand h : hands.values()) {
                        h.cards.clear();
                        h.result = "";
                        h.cards.add(draw());
                        h.cards.add(draw());
                        h.done = Cards.value(h.cards) == 21;
                    }
                    phase = PLAYING;
                    msg = "¡Cartas repartidas! Juegan los jugadores.";
                    Msg.sound(level, pos, SoundEvents.BOOK_PAGE_TURN, 1.1F);
                    afterPlayerMove(level, pos);
                }
            }
            case 4 -> {
                if (mine != null && phase == PLAYING && !mine.done) {
                    mine.cards.add(draw());
                    Msg.sound(level, pos, SoundEvents.BOOK_PAGE_TURN, 1.3F);
                    if (Cards.value(mine.cards) >= 21) {
                        mine.done = true;
                    }
                    msg = name + " pide carta.";
                    afterPlayerMove(level, pos);
                }
            }
            case 5 -> {
                if (mine != null && phase == PLAYING && !mine.done) {
                    mine.done = true;
                    msg = name + " se planta con " + Cards.value(mine.cards) + ".";
                    afterPlayerMove(level, pos);
                }
            }
            case 6 -> {
                if (isDealer && phase == DEALER && Cards.value(dealerCards) < 21) {
                    dealerCards.add(draw());
                    Msg.sound(level, pos, SoundEvents.BOOK_PAGE_TURN, 1.3F);
                    msg = "El crupier pide carta.";
                    if (Cards.value(dealerCards) >= 21) {
                        resolve(level, pos);
                    }
                }
            }
            case 7 -> {
                if (isDealer && phase == DEALER) {
                    resolve(level, pos);
                }
            }
            case 8 -> {
                if (isDealer && phase == PLAYING) {
                    for (Hand h : hands.values()) {
                        h.done = true;
                    }
                    msg = "El crupier cierra las jugadas.";
                    afterPlayerMove(level, pos);
                }
            }
            case 9 -> {
                if (isDealer) {
                    resetTable("El crupier " + name + " cerró la mesa.");
                } else if (mine != null) {
                    hands.remove(id);
                    msg = name + " se levanta de la mesa.";
                    if (phase == PLAYING) {
                        afterPlayerMove(level, pos);
                    } else if (hands.isEmpty()) {
                        phase = LOBBY;
                    }
                }
            }
            default -> {
            }
        }
        sync(level, pos);
    }

    /** Si todos los jugadores terminaron, el crupier pasa a jugar (o se resuelve solo si todos se pasaron). */
    private void afterPlayerMove(ServerLevel level, BlockPos pos) {
        if (phase != PLAYING) {
            return;
        }
        if (hands.isEmpty()) {
            phase = LOBBY;
            return;
        }
        boolean allBust = true;
        for (Hand h : hands.values()) {
            if (!h.done) {
                return;
            }
            allBust &= Cards.value(h.cards) > 21;
        }
        phase = DEALER;
        msg = "Todos terminaron: el crupier juega su mano.";
        if (allBust) {
            resolve(level, pos);
        }
    }

    private void resolve(ServerLevel level, BlockPos pos) {
        int dv = Cards.value(dealerCards);
        boolean dealerBj = dealerCards.size() == 2 && dv == 21;
        StringBuilder sb = new StringBuilder("Crupier: " + dv + (dv > 21 ? " (se pasa)" : "") + ". ");
        for (Hand h : hands.values()) {
            int pv = Cards.value(h.cards);
            boolean bj = h.cards.size() == 2 && pv == 21;
            if (pv > 21) {
                h.result = "pierde";
            } else if (bj && !dealerBj) {
                h.result = "BLACKJACK";
            } else if (dv > 21 || pv > dv) {
                h.result = "GANA";
            } else if (pv == dv) {
                h.result = "empata";
            } else {
                h.result = "pierde";
            }
            sb.append(h.name).append(' ').append(h.result).append(". ");
        }
        phase = RESULT;
        msg = sb.toString().trim();
        Msg.sound(level, pos, SoundEvents.BOOK_PAGE_TURN, 0.8F);
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if ((dealer != null || !hands.isEmpty()) && level.getGameTime() - lastAction > 6000) {
            resetTable("Mesa cerrada por inactividad.");
            sync(level, pos);
        }
    }

    @Override
    protected CompoundTag view(ServerLevel level) {
        CompoundTag t = new CompoundTag();
        t.putInt("phase", phase);
        t.putString("dealer", dealerName);
        t.putString("did", dealer == null ? "" : dealer.toString());
        t.putIntArray("dealerCards", dealerCards.stream().mapToInt(Integer::intValue).toArray());
        t.putInt("dealerValue", Cards.value(dealerCards));
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Hand> e : hands.entrySet()) {
            Hand h = e.getValue();
            CompoundTag c = new CompoundTag();
            c.putString("name", h.name);
            c.putString("id", e.getKey().toString());
            c.putIntArray("cards", h.cards.stream().mapToInt(Integer::intValue).toArray());
            c.putBoolean("done", h.done);
            c.putInt("value", Cards.value(h.cards));
            c.putString("result", h.result);
            list.add(c);
        }
        t.put("hands", list);
        t.putInt("max", MAX_PLAYERS);
        t.putString("msg", msg);
        return t;
    }
}
