package kr.toxicity.hud.api.version;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Minecraft version.
 * @param first title
 * @param second main update
 * @param third minor update
 */
public record MinecraftVersion(int first, int second, int third) implements Comparable<MinecraftVersion> {
    /**
     * Comparator
     */
    private static final Comparator<MinecraftVersion> COMPARATOR = Comparator.comparing(MinecraftVersion::first)
            .thenComparing(MinecraftVersion::second)
            .thenComparing(MinecraftVersion::third);

    @Override
    public int compareTo(@NotNull MinecraftVersion o) {
        return COMPARATOR.compare(this, o);
    }

    /**
     * 1.21.9
     */
    public static final MinecraftVersion V1_21_9 = new MinecraftVersion(1, 21, 9);
    /**
     * 1.21.8
     */
    public static final MinecraftVersion V1_21_8 = new MinecraftVersion(1, 21, 8);
    /**
     * 1.21.7
     */
    public static final MinecraftVersion V1_21_7 = new MinecraftVersion(1, 21, 7);
    /**
     * 1.21.6
     */
    public static final MinecraftVersion V1_21_6 = new MinecraftVersion(1, 21, 6);
    /**
     * 1.21.5
     */
    public static final MinecraftVersion V1_21_5 = new MinecraftVersion(1, 21, 5);
    /**
     * 1.21.4
     */
    public static final MinecraftVersion V1_21_4 = new MinecraftVersion(1, 21, 4);
    /**
     * 1.21.3
     */
    public static final MinecraftVersion V1_21_3 = new MinecraftVersion(1, 21, 3);
    /**
     * 1.21.2
     */
    public static final MinecraftVersion V1_21_2 = new MinecraftVersion(1, 21, 2);
    /**
     * 1.21.1
     */
    public static final MinecraftVersion V1_21_1 = new MinecraftVersion(1, 21, 1);
    /**
     * 1.21
     */
    public static final MinecraftVersion V1_21 = new MinecraftVersion(1, 21, 0);
    /**
     * 1.20.6
     */
    public static final MinecraftVersion V1_20_6 = new MinecraftVersion(1, 20, 6);
    /**
     * 1.20.5
     */
    public static final MinecraftVersion V1_20_5 = new MinecraftVersion(1, 20, 5);
    /**
     * 1.20.4
     */
    public static final MinecraftVersion V1_20_4 = new MinecraftVersion(1, 20, 4);
    /**
     * 1.20.3
     */
    public static final MinecraftVersion V1_20_3 = new MinecraftVersion(1, 20, 3);
    /**
     * 1.20.2
     */
    public static final MinecraftVersion V1_20_2 = new MinecraftVersion(1, 20, 2);
    /**
     * 1.20.1
     */
    public static final MinecraftVersion V1_20_1 = new MinecraftVersion(1, 20, 1);
    /**
     * 1.20
     */
    public static final MinecraftVersion V1_20 = new MinecraftVersion(1, 20, 0);
    /**
     * 1.19.4
     */
    public static final MinecraftVersion V1_19_4 = new MinecraftVersion(1, 19, 4);
    /**
     * 1.19.3
     */
    public static final MinecraftVersion V1_19_3 = new MinecraftVersion(1, 19, 3);
    /**
     * 1.19.2
     */
    public static final MinecraftVersion V1_19_2 = new MinecraftVersion(1, 19, 2);
    /**
     * 1.19.1
     */
    public static final MinecraftVersion V1_19_1 = new MinecraftVersion(1, 19, 1);
    /**
     * 1.19
     */
    public static final MinecraftVersion V1_19 = new MinecraftVersion(1, 19, 0);
    /**
     * Latest
     */
    public static final MinecraftVersion LATEST = V1_21_9;

    /**
     * Parses version from string
     * @param version version like "1.21.7"
     */
    public MinecraftVersion(@NotNull String version) {
        this(version.split("\\."));
    }
    /**
     * Parses version from a string array
     * @param version version array like ["1", "21", "7"]
     */
    public MinecraftVersion(@NotNull String[] version) {
        this(
                version.length > 0 ? Integer.parseInt(version[0]) : 0,
                version.length > 1 ? Integer.parseInt(version[1]) : 0,
                version.length > 2 ? Integer.parseInt(version[2]) : 0
        );
    }

    @Override
    public @NotNull String toString() {
        return first + "." + second + "." + third;
    }
}