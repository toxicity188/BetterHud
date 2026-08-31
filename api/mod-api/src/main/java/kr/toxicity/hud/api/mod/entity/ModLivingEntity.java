package kr.toxicity.hud.api.mod.entity;

/**
 * Provides additional function about living entity.
 */

public interface ModLivingEntity {
    /**
     * Gets last damage.
     * @return last damage
     */
    double betterHud$getLastDamage();

    /**
     * Gets health and last damage.
     * @return last health
     */
    double betterHud$getLastHealth();
}
