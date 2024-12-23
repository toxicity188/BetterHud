package kr.toxicity.hud.api.fabric.entity;

/**
 * Provides additional function about living entity.
 */

public interface FabricLivingEntity {
    /**
     * Gets last damage.
     * @return last damage
     */
    double betterhud$getLastDamage();

    /**
     * Gets health + last damage.
     * @return last health
     */
    double betterhud$getLastHealth();
}
