package com.cocteleria.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Naipes: carta = 0..51 (palo = carta / 13, número = carta % 13 + 1). */
public final class Cards {
    private Cards() {
    }

    private static final String[] RANK = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private static final String[] SUIT = {"♥", "♦", "♣", "♠"};

    public static List<Integer> newDeck(Random random) {
        List<Integer> deck = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            deck.add(i);
        }
        Collections.shuffle(deck, random);
        return deck;
    }

    public static String text(int card) {
        int suit = card / 13;
        String color = suit < 2 ? "§c" : "§f";
        return color + RANK[card % 13] + SUIT[suit] + "§r";
    }

    /** Valor de una mano de 21: los ases valen 11 o 1 según convenga. */
    public static int value(List<Integer> hand) {
        int total = 0;
        int aces = 0;
        for (int c : hand) {
            int rank = c % 13 + 1;
            if (rank == 1) {
                aces++;
                total += 11;
            } else {
                total += Math.min(rank, 10);
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    public static String show(List<Integer> hand) {
        StringBuilder sb = new StringBuilder();
        for (int c : hand) {
            sb.append(text(c)).append(' ');
        }
        return sb.toString().trim();
    }
}
