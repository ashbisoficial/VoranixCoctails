package com.cocteleria.game;

import com.cocteleria.net.GameViewPacket;
import com.cocteleria.net.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/** Sesión que mantiene a los jugadores con la pantalla abierta sincronizados con el estado de la mesa. */
public abstract class AbstractSession implements GameSession {
    private final BarGame game;
    private final Set<UUID> viewers = new HashSet<>();

    protected AbstractSession(BarGame game) {
        this.game = game;
    }

    /** Estado que se envía a las pantallas abiertas. */
    protected abstract CompoundTag view(ServerLevel level);

    /** El jugador acaba de hacer clic en la mesa (antes de abrirle la pantalla). */
    protected void onUse(ServerLevel level, BlockPos pos, ServerPlayer player) {
    }

    @Override
    public void use(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockHitResult hit) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        onUse(level, pos, sp);
        viewers.add(sp.getUUID());
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new GameViewPacket(pos, game, true, view(level)));
        sync(level, pos);
    }

    @Override
    public void closed(ServerPlayer player) {
        viewers.remove(player.getUUID());
    }

    /** Envía el estado actual a todos los jugadores con la mesa abierta. */
    protected void sync(ServerLevel level, BlockPos pos) {
        if (viewers.isEmpty()) {
            return;
        }
        CompoundTag data = view(level);
        Iterator<UUID> it = viewers.iterator();
        while (it.hasNext()) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(it.next());
            if (p == null || p.level() != level || p.distanceToSqr(Vec3.atCenterOf(pos)) > 20 * 20) {
                it.remove();
                continue;
            }
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new GameViewPacket(pos, game, false, data));
        }
    }

    protected ServerPlayer playerById(ServerLevel level, UUID id) {
        return id == null ? null : level.getServer().getPlayerList().getPlayer(id);
    }
}
