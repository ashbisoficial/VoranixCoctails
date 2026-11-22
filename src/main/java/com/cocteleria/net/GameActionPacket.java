package com.cocteleria.net;

import com.cocteleria.game.GameBlockEntity;
import com.cocteleria.game.GameSession;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente → servidor: una acción del jugador en la mesa (tipo 0 = cerrar la pantalla). */
public class GameActionPacket {
    public final BlockPos pos;
    public final int type;
    public final int a;
    public final int b;
    public final float f;

    public GameActionPacket(BlockPos pos, int type, int a, int b, float f) {
        this.pos = pos;
        this.type = type;
        this.a = a;
        this.b = b;
        this.f = f;
    }

    public static void encode(GameActionPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
        buf.writeInt(p.type);
        buf.writeInt(p.a);
        buf.writeInt(p.b);
        buf.writeFloat(p.f);
    }

    public static GameActionPacket decode(FriendlyByteBuf buf) {
        return new GameActionPacket(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat());
    }

    public static void handle(GameActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) {
                return;
            }
            ServerLevel level = sender.serverLevel();
            if (!level.isLoaded(p.pos) || sender.blockPosition().distSqr(p.pos) > 20 * 20) {
                return;
            }
            if (level.getBlockEntity(p.pos) instanceof GameBlockEntity entity) {
                GameSession session = entity.existingSession();
                if (session == null) {
                    return;
                }
                if (p.type == 0) {
                    session.closed(sender);
                } else {
                    session.action(level, p.pos, level.getBlockState(p.pos), sender, p.type, p.a, p.b, p.f);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
