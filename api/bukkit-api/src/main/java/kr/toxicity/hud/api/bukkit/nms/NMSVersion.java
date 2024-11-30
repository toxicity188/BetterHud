package kr.toxicity.hud.api.bukkit.nms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents server's version.
 */
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
    V1_20_R3(20,3, 22),
    V1_20_R4(20,4, 32),
    V1_21_R1(21,1, 34),
    V1_21_R2(21,2, 42),
    V1_21_R3(21,3, 46)
    ;
    /**
     * Main version.
     */
    private final int version;
    /**
     * Sub version.
     */
    private final int subVersion;
    /**
     * That client version's resource pack mcmeta version.
     */
    private final int metaVersion;
}
