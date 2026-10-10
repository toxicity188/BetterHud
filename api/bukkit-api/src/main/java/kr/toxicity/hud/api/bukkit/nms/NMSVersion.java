package kr.toxicity.hud.api.bukkit.nms;

import lombok.Getter;

/**
 * Represents server's version.
 */
@Getter
public enum NMSVersion {
    /**
     * 1.21.4
     */
    V1_21_R3(46),
    /**
     * 1.21.5
     */
    V1_21_R4(55),
    /**
     * 1.21.6-1.21.8
     */
    V1_21_R5(64),
    /**
     * 1.21.9-1.21.10
     */
    V1_21_R6(69),
    /**
     * 1.21.11
     */
    V1_21_R7(75),
    /**
     * 26.1
     */
    V26_R1(84),
    /**
     * 26.2-26.3
     */
    V26_R2(97)
    ;
    /**
     * That client version's resource pack mcmeta version.
     */
    private final int metaVersion;

    NMSVersion(int metaVersion) {
        this.metaVersion = metaVersion;
    }
}
