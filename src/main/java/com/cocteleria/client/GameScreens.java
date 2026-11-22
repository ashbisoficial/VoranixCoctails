package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import net.minecraft.core.BlockPos;

public final class GameScreens {
    private GameScreens() {
    }

    public static GameScreen create(BarGame game, BlockPos pos) {
        return switch (game) {
            case CHESS -> new ChessScreen(pos);
            case POOL -> new PoolScreen(pos);
            case FOOSBALL -> new FoosballScreen(pos);
            case ARM -> new ArmScreen(pos);
            case DICE -> new DiceScreen(pos);
            case BLACKJACK -> new BlackjackScreen(pos);
            case DARTS -> new DartsScreen(pos);
        };
    }
}
