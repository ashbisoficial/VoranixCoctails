package com.cocteleria.game;

import java.util.function.Supplier;

/** Tipos de juego de bar; cada bloque de juego crea su sesión con esta fábrica. */
public enum BarGame {
    DARTS(DartsSession::new),
    CHESS(ChessSession::new),
    ARM(ArmWrestlingSession::new),
    POOL(PoolSession::new),
    FOOSBALL(FoosballSession::new),
    DICE(DiceSession::new),
    BLACKJACK(BlackjackSession::new);

    private final Supplier<GameSession> factory;

    BarGame(Supplier<GameSession> factory) {
        this.factory = factory;
    }

    public GameSession create() {
        return factory.get();
    }
}
