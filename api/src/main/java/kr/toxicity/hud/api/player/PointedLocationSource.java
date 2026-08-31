package kr.toxicity.hud.api.player;

/**
 * A source of pointer location
 */
public enum PointedLocationSource {
    /**
     * From unknown source.
     */
    EMPTY,
    /**
     * From internal API of BetterHud.
     */
    INTERNAL,
    /**
     * From GPS plugin.
     */
    GPS
}
