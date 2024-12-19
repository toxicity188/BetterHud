package kr.toxicity.hud.api.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@RequiredArgsConstructor
@Getter
public enum ReloadFlagType {
    FORCE_GENERATE_RESOURCE_PACK("force-generate-resource-pack")
    ;
    private final  @NotNull String argument;

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
