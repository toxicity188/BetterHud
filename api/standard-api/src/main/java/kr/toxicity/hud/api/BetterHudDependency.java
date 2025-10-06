package kr.toxicity.hud.api;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * A BetterHud's library
 */
@Getter
@SuppressWarnings("unused")
public class BetterHudDependency {
    private static final List<BetterHudDependency> DEPENDENCIES = new ArrayList<>();

    private final @NotNull String group;
    private final @NotNull String name;
    private final @NotNull String version;
    private final boolean relocate;
    private final @NotNull @Unmodifiable Set<BetterHudPlatform> availablePlatforms;

    private BetterHudDependency(
            @NotNull String group,
            @NotNull String name,
            @NotNull String version,
            boolean relocate,
            @NotNull @Unmodifiable Set<BetterHudPlatform> availablePlatforms
    ) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.relocate = relocate;
        this.availablePlatforms = availablePlatforms;

        DEPENDENCIES.add(this);
    }

    /**
     * Gets all dependencies
     * @return dependencies
     */
    public static @NotNull @Unmodifiable List<BetterHudDependency> dependencies() {
        return Collections.unmodifiableList(DEPENDENCIES);
    }

    /**
     * Gson
     */
    public static final BetterHudDependency GSON = new BetterHudDependency(
            "com{}google{}code{}gson",
            "gson",
            "2.13.2",
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.VELOCITY
            ))
    );
    /**
     * Snakeyaml
     */
    public static final BetterHudDependency SNAKEYAML = new BetterHudDependency(
            "org{}yaml",
            "snakeyaml",
            "2.5",
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.VELOCITY,
                    BetterHudPlatform.FABRIC
            ))
    );
    /**
     * Mysql connector
     */
    public static final BetterHudDependency MYSQL_CONNECTOR_J = new BetterHudDependency(
            "com{}mysql",
            "mysql-connector-j",
            "9.2.0",
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.VELOCITY,
                    BetterHudPlatform.FABRIC
            ))
    );
    /**
     * Asm commons
     */
    public static final BetterHudDependency ASM_COMMONS = new BetterHudDependency(
            "org{}ow2{}asm",
            "asm-commons",
            "9.9",
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.VELOCITY,
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Exp4j
     */
    public static final BetterHudDependency EXP4J = new BetterHudDependency(
            "net{}objecthunter",
            "exp4j",
            "0.4.8",
            true,
            BetterHudPlatform.ALL
    );
    /**
     * Expiring map
     */
    public static final BetterHudDependency EXPIRING_MAP = new BetterHudDependency(
            "net{}jodah",
            "expiringmap",
            "0.5.11",
            true,
            BetterHudPlatform.ALL
    );
    /**
     * HikariCP
     */
    public static final BetterHudDependency HIKARICP = new BetterHudDependency(
            "com{}zaxxer",
            "HikariCP",
            "7.0.2",
            false,
            BetterHudPlatform.ALL
    );
    /**
     * FastUtil
     */
    public static final BetterHudDependency FAST_UTIL = new BetterHudDependency(
            "it{}unimi{}dsi",
            "fastutil",
            "8.5.18",
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.VELOCITY
            ))
    );
    /**
     * Semver4j
     */
    public static final BetterHudDependency SEMVER4J = new BetterHudDependency(
            "org{}semver4j",
            "semver4j",
            "6.0.0",
            false,
            BetterHudPlatform.ALL
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_API = new BetterHudDependency(
            "net{}kyori",
            "adventure-api",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_KEY = new BetterHudDependency(
            "net{}kyori",
            "adventure-key",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_TEXT_LOGGER_SLF4J = new BetterHudDependency(
            "net{}kyori",
            "adventure-text-logger-slf4j",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_TEXT_SERIALIZER_ANSI = new BetterHudDependency(
            "net{}kyori",
            "adventure-text-serializer-ansi",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_TEXT_SERIALIZER_GSON = new BetterHudDependency(
            "net{}kyori",
            "adventure-text-serializer-gson",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_TEXT_SERIALIZER_PLAIN = new BetterHudDependency(
            "net{}kyori",
            "adventure-text-serializer-plain",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_TEXT_SERIALIZER_LEGACY = new BetterHudDependency(
            "net{}kyori",
            "adventure-text-serializer-legacy",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT,
                    BetterHudPlatform.FABRIC
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_TEXT_SERIALIZER_JSON = new BetterHudDependency(
            "net{}kyori",
            "adventure-text-serializer-json",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_TEXT_MINIMESSAGE = new BetterHudDependency(
            "net{}kyori",
            "adventure-text-minimessage",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Examination
     */
    public static final BetterHudDependency EXAMINATION_API = new BetterHudDependency(
            "net{}kyori",
            "examination-api",
            BetterHud.EXAMINATION_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Examination
     */
    public static final BetterHudDependency EXAMINATION_STRING = new BetterHudDependency(
            "net{}kyori",
            "examination-string",
            BetterHud.EXAMINATION_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Option
     */
    public static final BetterHudDependency OPTION = new BetterHudDependency(
            "net{}kyori",
            "option",
            "1.1.0",
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_NBT = new BetterHudDependency(
            "net{}kyori",
            "adventure-nbt",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT,
                    BetterHudPlatform.PAPER
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_TEXT_SERIALIZER_GSON_LEGACY_IMPL = new BetterHudDependency(
            "net{}kyori",
            "adventure-text-serializer-gson-legacy-impl",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT,
                    BetterHudPlatform.PAPER
            ))
    );
    /**
     * Adventure
     */
    public static final BetterHudDependency ADVENTURE_TEXT_SERIALIZER_JSON_LEGACY_IMPL = new BetterHudDependency(
            "net{}kyori",
            "adventure-text-serializer-json-legacy-impl",
            BetterHud.ADVENTURE_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT,
                    BetterHudPlatform.PAPER
            ))
    );

    /**
     * Adventure-platform
     */
    public static final BetterHudDependency ADVENTURE_PLATFORM_BUKKIT = new BetterHudDependency(
            "net{}kyori",
            "adventure-platform-bukkit",
            BetterHud.PLATFORM_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT,
                    BetterHudPlatform.PAPER
            ))
    );
    /**
     * Adventure-platform
     */
    public static final BetterHudDependency ADVENTURE_PLATFORM_API = new BetterHudDependency(
            "net{}kyori",
            "adventure-platform-api",
            BetterHud.PLATFORM_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT,
                    BetterHudPlatform.PAPER
            ))
    );
    /**
     * Adventure-platform
     */
    public static final BetterHudDependency ADVENTURE_PLATFORM_FACET = new BetterHudDependency(
            "net{}kyori",
            "adventure-platform-facet",
            BetterHud.PLATFORM_VERSION,
            false,
            Collections.unmodifiableSet(EnumSet.of(
                    BetterHudPlatform.BUKKIT,
                    BetterHudPlatform.PAPER
            ))
    );
}
