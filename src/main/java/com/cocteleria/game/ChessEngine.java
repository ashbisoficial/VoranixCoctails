package com.cocteleria.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Motor de ajedrez con reglas completas (enroque, captura al paso, coronación, jaque, mate, tablas).
 * Casillas: índice = fila * 8 + columna, fila 0 = rango 1 (blancas), columna 0 = columna "a".
 * Piezas: positivo = blancas, negativo = negras (1 peón, 2 caballo, 3 alfil, 4 torre, 5 dama, 6 rey).
 */
public class ChessEngine {
    public static final int P = 1, N = 2, B = 3, R = 4, Q = 5, K = 6;

    public enum Status { ONGOING, CHECK, CHECKMATE, STALEMATE, DRAW }

    public static final class Move {
        public final int from, to, promo;

        public Move(int from, int to, int promo) {
            this.from = from;
            this.to = to;
            this.promo = promo;
        }
    }

    public final int[] board = new int[64];
    public boolean whiteToMove = true;
    private boolean wk = true, wq = true, bk = true, bq = true;
    private int ep = -1;
    private int halfmove = 0;

    private static final int[] KNIGHT = {-17, -15, -10, -6, 6, 10, 15, 17};

    public ChessEngine() {
        reset();
    }

    public void reset() {
        java.util.Arrays.fill(board, 0);
        int[] back = {R, N, B, Q, K, B, N, R};
        for (int f = 0; f < 8; f++) {
            board[f] = back[f];
            board[8 + f] = P;
            board[48 + f] = -P;
            board[56 + f] = -back[f];
        }
        whiteToMove = true;
        wk = wq = bk = bq = true;
        ep = -1;
        halfmove = 0;
    }

    /** Carga una posición FEN (para pruebas). */
    public void setFen(String fen) {
        java.util.Arrays.fill(board, 0);
        String[] parts = fen.split(" ");
        String[] rows = parts[0].split("/");
        for (int i = 0; i < 8; i++) {
            int rank = 7 - i, file = 0;
            for (char c : rows[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    file += c - '0';
                } else {
                    int type = "pnbrqk".indexOf(Character.toLowerCase(c)) + 1;
                    board[rank * 8 + file++] = Character.isUpperCase(c) ? type : -type;
                }
            }
        }
        whiteToMove = parts[1].equals("w");
        String c = parts[2];
        wk = c.contains("K");
        wq = c.contains("Q");
        bk = c.contains("k");
        bq = c.contains("q");
        ep = parts[3].equals("-") ? -1 : (parts[3].charAt(1) - '1') * 8 + (parts[3].charAt(0) - 'a');
        halfmove = 0;
    }

    public ChessEngine copy() {
        ChessEngine e = new ChessEngine();
        System.arraycopy(board, 0, e.board, 0, 64);
        e.whiteToMove = whiteToMove;
        e.wk = wk;
        e.wq = wq;
        e.bk = bk;
        e.bq = bq;
        e.ep = ep;
        e.halfmove = halfmove;
        return e;
    }

    public static String squareName(int sq) {
        return "" + (char) ('a' + sq % 8) + (char) ('1' + sq / 8);
    }

    private static boolean onBoard(int rank, int file) {
        return rank >= 0 && rank < 8 && file >= 0 && file < 8;
    }

    /** ¿La casilla está atacada por el bando indicado? */
    public boolean attacked(int sq, boolean byWhite) {
        int rank = sq / 8, file = sq % 8;
        int sign = byWhite ? 1 : -1;
        int pr = rank - sign; // fila de donde vendría un peón atacante
        for (int df = -1; df <= 1; df += 2) {
            if (onBoard(pr, file + df) && board[pr * 8 + file + df] == sign * P) {
                return true;
            }
        }
        int[][] kn = {{1, 2}, {2, 1}, {-1, 2}, {-2, 1}, {1, -2}, {2, -1}, {-1, -2}, {-2, -1}};
        for (int[] d : kn) {
            int r = rank + d[0], f = file + d[1];
            if (onBoard(r, f) && board[r * 8 + f] == sign * N) {
                return true;
            }
        }
        for (int dr = -1; dr <= 1; dr++) {
            for (int df = -1; df <= 1; df++) {
                if (dr == 0 && df == 0) {
                    continue;
                }
                int r = rank + dr, f = file + df;
                if (onBoard(r, f) && board[r * 8 + f] == sign * K) {
                    return true;
                }
                boolean diagonal = dr != 0 && df != 0;
                r = rank + dr;
                f = file + df;
                while (onBoard(r, f)) {
                    int p = board[r * 8 + f];
                    if (p != 0) {
                        if (p * sign > 0) {
                            int t = Math.abs(p);
                            if (t == Q || (diagonal && t == B) || (!diagonal && t == R)) {
                                return true;
                            }
                        }
                        break;
                    }
                    r += dr;
                    f += df;
                }
            }
        }
        return false;
    }

    private int kingSquare(boolean white) {
        for (int i = 0; i < 64; i++) {
            if (board[i] == (white ? K : -K)) {
                return i;
            }
        }
        return -1;
    }

    public boolean inCheck(boolean white) {
        int k = kingSquare(white);
        return k >= 0 && attacked(k, !white);
    }

    private void add(List<Move> out, int from, int to, boolean promotion) {
        if (promotion) {
            out.add(new Move(from, to, Q));
            out.add(new Move(from, to, R));
            out.add(new Move(from, to, B));
            out.add(new Move(from, to, N));
        } else {
            out.add(new Move(from, to, 0));
        }
    }

    private List<Move> pseudoMoves() {
        List<Move> out = new ArrayList<>();
        int sign = whiteToMove ? 1 : -1;
        for (int sq = 0; sq < 64; sq++) {
            int piece = board[sq];
            if (piece == 0 || piece * sign < 0) {
                continue;
            }
            int type = Math.abs(piece);
            int rank = sq / 8, file = sq % 8;
            switch (type) {
                case P -> {
                    int nr = rank + sign;
                    boolean promo = nr == (whiteToMove ? 7 : 0);
                    if (onBoard(nr, file) && board[nr * 8 + file] == 0) {
                        add(out, sq, nr * 8 + file, promo);
                        int start = whiteToMove ? 1 : 6;
                        if (rank == start && board[(rank + 2 * sign) * 8 + file] == 0) {
                            add(out, sq, (rank + 2 * sign) * 8 + file, false);
                        }
                    }
                    for (int df = -1; df <= 1; df += 2) {
                        int nf = file + df;
                        if (!onBoard(nr, nf)) {
                            continue;
                        }
                        int target = nr * 8 + nf;
                        if (board[target] * sign < 0 || target == ep) {
                            add(out, sq, target, promo);
                        }
                    }
                }
                case N -> {
                    int[][] kn = {{1, 2}, {2, 1}, {-1, 2}, {-2, 1}, {1, -2}, {2, -1}, {-1, -2}, {-2, -1}};
                    for (int[] d : kn) {
                        int r = rank + d[0], f = file + d[1];
                        if (onBoard(r, f) && board[r * 8 + f] * sign <= 0) {
                            add(out, sq, r * 8 + f, false);
                        }
                    }
                }
                case B, R, Q -> {
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int df = -1; df <= 1; df++) {
                            if (dr == 0 && df == 0) {
                                continue;
                            }
                            boolean diagonal = dr != 0 && df != 0;
                            if ((type == B && !diagonal) || (type == R && diagonal)) {
                                continue;
                            }
                            int r = rank + dr, f = file + df;
                            while (onBoard(r, f)) {
                                int p = board[r * 8 + f];
                                if (p * sign > 0) {
                                    break;
                                }
                                add(out, sq, r * 8 + f, false);
                                if (p != 0) {
                                    break;
                                }
                                r += dr;
                                f += df;
                            }
                        }
                    }
                }
                case K -> {
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int df = -1; df <= 1; df++) {
                            if (dr == 0 && df == 0) {
                                continue;
                            }
                            int r = rank + dr, f = file + df;
                            if (onBoard(r, f) && board[r * 8 + f] * sign <= 0) {
                                add(out, sq, r * 8 + f, false);
                            }
                        }
                    }
                    int base = whiteToMove ? 0 : 56;
                    if (sq == base + 4 && !attacked(sq, !whiteToMove)) {
                        boolean kingSide = whiteToMove ? wk : bk;
                        boolean queenSide = whiteToMove ? wq : bq;
                        if (kingSide && board[base + 5] == 0 && board[base + 6] == 0 && board[base + 7] == sign * R
                                && !attacked(base + 5, !whiteToMove) && !attacked(base + 6, !whiteToMove)) {
                            add(out, sq, base + 6, false);
                        }
                        if (queenSide && board[base + 3] == 0 && board[base + 2] == 0 && board[base + 1] == 0
                                && board[base] == sign * R
                                && !attacked(base + 3, !whiteToMove) && !attacked(base + 2, !whiteToMove)) {
                            add(out, sq, base + 2, false);
                        }
                    }
                }
                default -> {
                }
            }
        }
        return out;
    }

    private void apply(Move m) {
        int piece = board[m.from];
        int type = Math.abs(piece);
        int sign = piece > 0 ? 1 : -1;
        boolean capture = board[m.to] != 0;
        int newEp = -1;

        if (type == P && m.to == ep && board[m.to] == 0 && m.from % 8 != m.to % 8) {
            board[m.to - 8 * sign] = 0; // captura al paso
            capture = true;
        }
        if (type == P && Math.abs(m.to - m.from) == 16) {
            newEp = (m.from + m.to) / 2;
        }
        if (type == K && Math.abs(m.to - m.from) == 2) { // enroque: mover la torre
            if (m.to > m.from) {
                board[m.from + 1] = board[m.from + 3];
                board[m.from + 3] = 0;
            } else {
                board[m.from - 1] = board[m.from - 4];
                board[m.from - 4] = 0;
            }
        }
        board[m.to] = m.promo != 0 ? sign * m.promo : piece;
        board[m.from] = 0;

        if (type == K) {
            if (sign > 0) {
                wk = wq = false;
            } else {
                bk = bq = false;
            }
        }
        if (m.from == 0 || m.to == 0) wq = false;
        if (m.from == 7 || m.to == 7) wk = false;
        if (m.from == 56 || m.to == 56) bq = false;
        if (m.from == 63 || m.to == 63) bk = false;

        ep = newEp;
        halfmove = (type == P || capture) ? 0 : halfmove + 1;
        whiteToMove = !whiteToMove;
    }

    public List<Move> legalMoves() {
        List<Move> legal = new ArrayList<>();
        boolean mover = whiteToMove;
        for (Move m : pseudoMoves()) {
            ChessEngine c = copy();
            c.apply(m);
            if (!c.inCheck(mover)) {
                legal.add(m);
            }
        }
        return legal;
    }

    /** Casillas a las que puede ir la pieza de la casilla dada (movimientos legales). */
    public List<Integer> targetsFrom(int from) {
        List<Integer> out = new ArrayList<>();
        for (Move m : legalMoves()) {
            if (m.from == from && !out.contains(m.to)) {
                out.add(m.to);
            }
        }
        return out;
    }

    /** Juega from->to si es legal (corona siempre a dama). */
    public boolean play(int from, int to) {
        Move found = null;
        for (Move m : legalMoves()) {
            if (m.from == from && m.to == to && (m.promo == 0 || m.promo == Q)) {
                found = m;
                break;
            }
        }
        if (found == null) {
            return false;
        }
        apply(found);
        return true;
    }

    public Status status() {
        boolean check = inCheck(whiteToMove);
        if (legalMoves().isEmpty()) {
            return check ? Status.CHECKMATE : Status.STALEMATE;
        }
        if (halfmove >= 100 || insufficientMaterial()) {
            return Status.DRAW;
        }
        return check ? Status.CHECK : Status.ONGOING;
    }

    private boolean insufficientMaterial() {
        int minors = 0;
        for (int p : board) {
            int t = Math.abs(p);
            if (t == P || t == R || t == Q) {
                return false;
            }
            if (t == N || t == B) {
                minors++;
            }
        }
        return minors <= 1;
    }

    /** Pruebas: número de posiciones alcanzables a cierta profundidad. */
    public long perft(int depth) {
        if (depth == 0) {
            return 1;
        }
        long n = 0;
        for (Move m : legalMoves()) {
            ChessEngine c = copy();
            c.apply(m);
            n += c.perft(depth - 1);
        }
        return n;
    }
}
