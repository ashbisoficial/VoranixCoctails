package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Veintiuno (blackjack) contra el crupier para varios jugadores. Clic derecho: unirse / pedir carta.
 * Shift + clic (manos vacías): plantarse. Cuando todos terminan, el crupier juega (pide hasta 17) y se reparten resultados.
 */
public class BlackjackSession implements GameSession {
    private static final class Hand {
        final List<Integer> cards = new ArrayList<>();
        final String name;
        boolean done;

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

    private int draw() {
        if (deck.size() < 12) {
            deck = Cards.newDeck(random);
        }
        return deck.remove(deck.size() - 1);
    }

    @Override
    public void use(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockHitResult hit) {
        UUID id = player.getUUID();
        String name = Msg.name(player);
        long now = level.getGameTime();
        lastAction = now;

        if (!playing) {
            playing = true;
            hands.clear();
            dealer.clear();
            dealer.add(draw());
            dealer.add(draw());
            Msg.say(level, pos, "§e" + name + " abre una mesa de 21. §7Otros pueden unirse con clic derecho. Crupier: "
                    + Cards.text(dealer.get(0)) + " §7+ carta oculta");
        }

        Hand hand = hands.get(id);
        if (hand == null) {
            hand = new Hand(name);
            hand.cards.add(draw());
            hand.cards.add(draw());
            hands.put(id, hand);
            int v = Cards.value(hand.cards);
            Msg.say(level, pos, "§a" + name + " §7recibe: " + Cards.show(hand.cards) + " §7(" + v + ")"
                    + (v == 21 ? " §6¡Veintiuno!" : " §8— clic: pedir, Shift+clic: plantarse"));
            if (v == 21) {
                hand.done = true;
            }
        } else if (hand.done) {
            Msg.bar(player, "§7Ya terminaste tu mano. Espera a los demás.");
            return;
        } else if (Msg.isSpecial(player)) {
            hand.done = true;
            Msg.say(level, pos, "§f" + name + " §7se planta con " + Cards.show(hand.cards) + " §7(" + Cards.value(hand.cards) + ")");
        } else {
            hand.cards.add(draw());
            int v = Cards.value(hand.cards);
            Msg.sound(level, pos, SoundEvents.BOOK_PAGE_TURN, 1.2F);
            if (v > 21) {
                hand.done = true;
                Msg.say(level, pos, "§c" + name + " pide y se pasa: " + Cards.show(hand.cards) + " §c(" + v + ")");
            } else if (v == 21) {
                hand.done = true;
                Msg.say(level, pos, "§6" + name + " llega a 21: " + Cards.show(hand.cards));
            } else {
                Msg.say(level, pos, "§f" + name + " §7pide: " + Cards.show(hand.cards) + " §7(" + v + ")");
            }
        }

        boolean allDone = true;
        for (Hand h : hands.values()) {
            allDone &= h.done;
        }
        if (allDone) {
            resolve(level, pos);
        }
    }

    private void resolve(ServerLevel level, BlockPos pos) {
        while (Cards.value(dealer) < 17) {
            dealer.add(draw());
        }
        int dv = Cards.value(dealer);
        Msg.say(level, pos, "§eCrupier: " + Cards.show(dealer) + " §e(" + dv + (dv > 21 ? ", se pasa" : "") + ")");
        for (Hand h : hands.values()) {
            int pv = Cards.value(h.cards);
            String result;
            if (pv > 21) {
                result = "§cpierde";
            } else if (dv > 21 || pv > dv) {
                result = "§a§lGANA";
            } else if (pv == dv) {
                result = "§eempata";
            } else {
                result = "§cpierde";
            }
            Msg.say(level, pos, "§f" + h.name + " " + result + " §7(" + pv + " vs " + dv + ")");
        }
        Msg.sound(level, pos, SoundEvents.BOOK_PAGE_TURN, 0.8F);
        playing = false;
        hands.clear();
        dealer.clear();
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (playing && level.getGameTime() - lastAction > 600) {
            Msg.say(level, pos, "§7Se acabó el tiempo: todos se plantan.");
            for (Hand h : hands.values()) {
                h.done = true;
            }
            resolve(level, pos);
        }
    }
}
