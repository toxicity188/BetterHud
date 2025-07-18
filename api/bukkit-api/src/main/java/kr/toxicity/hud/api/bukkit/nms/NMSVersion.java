package kr.toxicity.hud.api.bukkit.nms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents server's version.
 */
@RequiredArgsConstructor
@Getter
public enum NMSVersion {
    /**
     * 1.19-1.19.1
     */
    V1_19_R1(19,1, 9),
    /**
     * 1.19.2-1.19.3
     */
    V1_19_R2(19,2, 12),
    /**
     * 1.19.4
     */
    V1_19_R3(19,3, 13),
    /**
     * 1.20-1.20.1
     */
    V1_20_R1(20,1, 15),
    /**
     * 1.20.2
     */
    V1_20_R2(20,2, 18),
    /**
     * 1.20.3-1.20.4
     */
    V1_20_R3(20,3, 22),
    /**
     * 1.20.5-1.20.6
     */
    V1_20_R4(20,4, 32),
    /**
     * 1.21-1.21.1
     */
    V1_21_R1(21,1, 34),
    /**
     * 1.21.2-1.21.3
     */
    V1_21_R2(21,2, 42),
    /**
     * 1.21.4
     */
    V1_21_R3(21,3, 46),
    /**
     * 1.21.5
     */
    V1_21_R4(21,4, 55),
    /**
     * 1.21.6-1.21.8
     */
    V1_21_R5(21,5, 64)
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
