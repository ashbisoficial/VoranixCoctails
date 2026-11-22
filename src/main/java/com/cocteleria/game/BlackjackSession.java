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
 * Veintiuno contra el crupier para varios jugadores. Acciones: 1 = repartir / unirse, 2 = pedir carta, 3 = plantarse.
 * Cuando todos terminan, el crupier juega (pide hasta 17) y se reparten los resultados.
 */
public class BlackjackSession extends AbstractSession {
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
    private final List<Integer> dealer = new ArrayList<>();
    private boolean playing = false;
    private long lastAction = 0;
    private String msg = "Pulsa «Repartir» para abrir la mesa de 21.";

    public BlackjackSession() {
        super(BarGame.BLACKJACK);
    }

    private int draw() {
        if (deck.size() < 12) {
            deck = Cards.newDeck(random);
        }
        return deck.remove(deck.size() - 1);
    }

    @Override
    public void action(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
                       int type, int a, int b, float f) {
        UUID id = player.getUUID();
        String name = player.getName().getString();
        long now = level.getGameTime();
        lastAction = now;

        if (type == 1) {
            if (!playing) {
                playing = true;
                hands.clear();
                dealer.clear();
                dealer.add(draw());
                dealer.add(draw());
                msg = name + " abre la mesa. Otros jugadores pueden unirse con «Repartir».";
            }
            if (!hands.containsKey(id)) {
                Hand h = new Hand(name);
                h.cards.add(draw());
                h.cards.add(draw());
                hands.put(id, h);
                if (Cards.value(h.cards) == 21) {
                    h.done = true;
                }
                Msg.sound(level, pos, SoundEvents.BOOK_PAGE_TURN, 1.1F);
            }
        } else if (playing && hands.containsKey(id) && !hands.get(id).done) {
            Hand h = hands.get(id);
            if (type == 2) {
                h.cards.add(draw());
                Msg.sound(level, pos, SoundEvents.BOOK_PAGE_TURN, 1.3F);
                int v = Cards.value(h.cards);
                if (v >= 21) {
                    h.done = true;
                }
            } else if (type == 3) {
                h.done = true;
            }
        }
        checkAllDone(level, pos);
        sync(level, pos);
    }

    private void checkAllDone(ServerLevel level, BlockPos pos) {
        if (!playing || hands.isEmpty()) {
            return;
        }
        for (Hand h : hands.values()) {
            if (!h.done) {
                return;
            }
        }
        resolve(level, pos);
    }

    private void resolve(ServerLevel level, BlockPos pos) {
        while (Cards.value(dealer) < 17) {
            dealer.add(draw());
        }
        int dv = Cards.value(dealer);
        StringBuilder sb = new StringBuilder("Crupier: " + dv + (dv > 21 ? " (se pasa)" : "") + ". ");
        for (Hand h : hands.values()) {
            int pv = Cards.value(h.cards);
            if (pv > 21) {
                h.result = "pierde";
            } else if (dv > 21 || pv > dv) {
                h.result = "GANA";
            } else if (pv == dv) {
                h.result = "empata";
            } else {
                h.result = "pierde";
            }
            sb.append(h.name).append(' ').append(h.result).append(". ");
        }
        msg = sb.toString().trim();
        playing = false;
        Msg.sound(level, pos, SoundEvents.BOOK_PAGE_TURN, 0.8F);
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (playing && level.getGameTime() - lastAction > 600) {
            for (Hand h : hands.values()) {
                h.done = true;
            }
            resolve(level, pos);
            msg = "Se acabó el tiempo. " + msg;
            sync(level, pos);
        }
    }

    @Override
    protected CompoundTag view(ServerLevel level) {
        CompoundTag t = new CompoundTag();
        t.putBoolean("playing", playing);
        t.putIntArray("dealer", dealer.stream().mapToInt(Integer::intValue).toArray());
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
        t.putString("msg", msg);
        return t;
    }
}
