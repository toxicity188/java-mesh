/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

/**
 * Abstraction over texture image access used while sampling triangle tint colors from UV coordinates.
 * <p>
 * Implementations provide pixel dimensions and random-access ARGB reads so mesh polygons can be converted
 * into Minecraft model tint data without depending on a specific image library.
 * </p>
 * <pre>{@code
 * MeshImage image = MeshImage.from(bufferedImage);
 * int color = image.rgb(0, 0);
 * }</pre>
 *
 * @since 0.0.1
 */
public interface MeshImage {

    /**
     * Represents the empty image.
     *
     * @since 0.0.1
     */
    MeshImage EMPTY = new MeshImage() {
        @Override
        public int height() {
            return 16;
        }

        @Override
        public int width() {
            return 16;
        }

        @Override
        public int rgb(int x, int z) {
            return 0xFFFFFFFF;
        }
    };

    /**
     * Returns the image height in pixels.
     *
     * @return texture height in pixels
     * @since 0.0.1
     */
    int height();

    /**
     * Returns the image width in pixels.
     *
     * @return texture width in pixels
     * @since 0.0.1
     */
    int width();

    /**
     * Returns the ARGB color of a texture pixel.
     *
     * @param x horizontal pixel coordinate
     * @param z vertical pixel coordinate
     * @return the packed ARGB color value
     * @since 0.0.1
     */
    int rgb(int x, int z);

    /**
     * Wraps an AWT image for use by the mesh conversion pipeline.
     *
     * @param image source image
     * @return an image adapter backed by {@link BufferedImage}
     * @throws NullPointerException if {@code image} is {@code null}
     * @since 0.0.1
     */
    static @NotNull MeshImage from(@NotNull BufferedImage image) {
        return new Awt(image);
    }

    /**
     * {@link BufferedImage}-backed {@link MeshImage} implementation.
     *
     * @param image wrapped source image
     * @since 0.0.1
     */
    record Awt(@NotNull BufferedImage image) implements MeshImage {
        @Override
        public int height() {
            return image.getHeight();
        }

        @Override
        public int width() {
            return image.getWidth();
        }

        @Override
        public int rgb(int x, int z) {
            return image.getRGB(x, z);
        }
    }
}

