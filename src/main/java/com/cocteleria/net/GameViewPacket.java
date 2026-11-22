package com.cocteleria.net;

import com.cocteleria.client.ClientGameHandler;
import com.cocteleria.game.BarGame;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor → cliente: estado de la mesa (y, si {@code open}, abre la pantalla del juego). */
public class GameViewPacket {
    public final BlockPos pos;
    public final BarGame game;
    public final boolean open;
    public final CompoundTag data;

    public GameViewPacket(BlockPos pos, BarGame game, boolean open, CompoundTag data) {
        this.pos = pos;
        this.game = game;
        this.open = open;
        this.data = data;
    }

    public static void encode(GameViewPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
        buf.writeEnum(p.game);
        buf.writeBoolean(p.open);
        buf.writeNbt(p.data);
    }

    public static GameViewPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BarGame game = buf.readEnum(BarGame.class);
        boolean open = buf.readBoolean();
        CompoundTag data = buf.readNbt();
        return new GameViewPacket(pos, game, open, data == null ? new CompoundTag() : data);
    }

    public static void handle(GameViewPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientGameHandler.handle(p)));
        ctx.get().setPacketHandled(true);
    }
}
