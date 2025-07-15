package de.elia.cameraplugin.mirrordamage;

/**
 * Available damage transfer modes.
 */
public enum DamageMode {
    /** Mirror damage 1:1 from the villager to the player. */
    MIRROR,
    /** Apply a fixed custom damage value on each hit. */
    CUSTOM,
    /** No damage is transferred. */
    OFF
}

