package kr.toxicity.hud.api.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Reload flags.
 */
@RequiredArgsConstructor
@Getter
public enum ReloadFlagType {
    /**
     * Forces resource pack generation.
     */
    FORCE_GENERATE_RESOURCE_PACK("force-generate-resource-pack")
    ;
    private final  @NotNull String argument;

    /**
     * Converts raw string to flags.
     * @param raw string
     * @return flag set
     */
    public static @NotNull Set<ReloadFlagType> from(@NotNull Collection<String> raw) {
        var list = raw.stream()
                .map(s -> {
                    try {
                        return valueOf(s.replace('-', '_').toUpperCase());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        return raw.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(list);
    }
}
