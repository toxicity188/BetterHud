package kr.toxicity.hud.api.nms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NMSVersion {
    V1_17_R1(17,1, 7),
    V1_18_R1(18,1, 8),
    V1_18_R2(18,2, 8),
    V1_19_R1(19,1, 9),
    V1_19_R2(19,2, 12),
    V1_19_R3(19,3, 13),
    V1_20_R1(20,1, 15),
    V1_20_R2(20,2, 18),
    V1_20_R3(20,3, 22)
    ;
    private final int version;
    private final int subVersion;
    private final int metaVersion;
}
