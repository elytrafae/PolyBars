package xyz.elytrafae.mc.polybars.api;

/**
 * Indicates how a texture will be sliced for use with bars
 * INDIVIDUAL: each slice will only contain its part
 * INCREMENTAL: each slice contains its part and all parts behind it
 */
public enum PolyTextureSliceMode {
    INDIVIDUAL,
    INCREMENTAL
}