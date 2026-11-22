package com.cocteleria.net;

import com.cocteleria.CocteleriaMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/** Canal de red: el servidor manda el estado de la mesa y el cliente manda las acciones del jugador. */
public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CocteleriaMod.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, GameViewPacket.class, GameViewPacket::encode, GameViewPacket::decode,
                GameViewPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, GameActionPacket.class, GameActionPacket::encode, GameActionPacket::decode,
                GameActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
