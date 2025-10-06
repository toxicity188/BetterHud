package kr.toxicity.hud.api.fabric.mixin.pack;

import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.InclusiveRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(PackFormat.IntermediaryFormat.class)
public class IntermediaryFormatMixin {
    @Overwrite
    public static PackFormat.IntermediaryFormat fromRange(InclusiveRange<PackFormat> range, int versionThreshold) {
        return new PackFormat.IntermediaryFormat(
                Optional.of(range.minInclusive()),
                Optional.of(range.maxInclusive()),
                Optional.of(range.minInclusive().major()),
                Optional.of(new InclusiveRange<>(range.minInclusive().major(), range.maxInclusive().major()))
        );
    }
}
