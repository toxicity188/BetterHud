package kr.toxicity.hud.api.bedrock;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface BedrockAdapter {
    boolean isBedrockPlayer(@NotNull UUID uuid);
}
