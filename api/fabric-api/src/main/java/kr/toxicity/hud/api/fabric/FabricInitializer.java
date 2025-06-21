package kr.toxicity.hud.api.fabric;

import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric API initializer.
 */
@SuppressWarnings("unused")
public final class FabricInitializer implements ModInitializer {

    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> server = minecraftServer);
    }

    public static @NotNull net.kyori.adventure.text.Component toAdventure(@NotNull net.minecraft.network.chat.Component component) {
        return GsonComponentSerializer.gson().deserializeFromTree(
                ComponentSerialization.CODEC.encodeStart(server.registryAccess().createSerializationContext(JsonOps.INSTANCE), component)
                        .getOrThrow()
        );
    }
    public static @NotNull net.minecraft.network.chat.Component toVanilla(@NotNull net.kyori.adventure.text.Component component) {
        return ComponentSerialization.CODEC
                .decode(server.registryAccess().createSerializationContext(JsonOps.INSTANCE), GsonComponentSerializer.gson().serializeToTree(component))
                .getOrThrow()
                .getFirst();
    }
}
