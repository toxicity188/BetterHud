package kr.toxicity.hud.api.version;

import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

import java.util.Comparator;
import java.util.Objects;

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
     * 26.1.2
     */
    public static final MinecraftVersion V26_1_2 = new MinecraftVersion(26, 1, 2);
    /**
     * 26.1.1
     */
    public static final MinecraftVersion V26_1_1 = new MinecraftVersion(26, 1, 1);
    /**
     * 26.1
     */
    public static final MinecraftVersion V26_1 = new MinecraftVersion(26, 1, 0);
    /**
     * 1.21.11
     */
    public static final MinecraftVersion V1_21_11 = new MinecraftVersion(1, 21, 11);
    /**
     * 1.21.10
     */
    public static final MinecraftVersion V1_21_10 = new MinecraftVersion(1, 21, 10);
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
     * Latest
     */
    public static final MinecraftVersion LATEST = V26_1_2;

    /**
     * Parses version from string
     * @param version version like "26.1.2"
     */
    public static @NotNull MinecraftVersion of(@NotNull String version) {
        var ver = Objects.requireNonNull(Semver.coerce(version));
        return new MinecraftVersion(ver.getMajor(), ver.getMinor(), ver.getPatch());
    }

    @Override
    public @NotNull String toString() {
        return first + "." + second + "." + third;
    }
}