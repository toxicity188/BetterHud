package kr.toxicity.hud.bootstrap.fabric.mixin.pack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.InclusiveRange;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PackFormat.IntermediaryFormat.class)
public class IntermediaryFormatMixin {

    @Mutable @Shadow @Final
    public static MapCodec<PackFormat.IntermediaryFormat> OVERLAY_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyOverlayCodec(CallbackInfo ci) {
        OVERLAY_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(
                                PackFormat.BOTTOM_CODEC.optionalFieldOf("min_format").forGetter(PackFormat.IntermediaryFormat::min),
                                PackFormat.TOP_CODEC.optionalFieldOf("max_format").forGetter(PackFormat.IntermediaryFormat::max),
                                InclusiveRange.codec(Codec.INT).optionalFieldOf("formats").forGetter(format -> {
                                    var support = format.supported();
                                    return support.isEmpty() ? Optional.of(new InclusiveRange<>(
                                            format.min().map(PackFormat::major).orElse(0),
                                            format.max().map(PackFormat::major).orElse(0)
                                    )) : support;
                                })
                        )
                        .apply(i, (min, max, formats) -> new PackFormat.IntermediaryFormat(
                                min,
                                max,
                                min.map(PackFormat::major),
                                formats
                        ))
        );
    }
}
