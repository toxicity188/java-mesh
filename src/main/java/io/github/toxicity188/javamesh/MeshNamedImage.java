/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * Stores mesh textures by name so parsed material references can be resolved during conversion.
 * <pre>{@code
 * var images = new MeshNamedImage(Map.of("blade.png", MeshImage.from(bufferedImage)));
 * MeshImage blade = images.get("blade.png");
 * }</pre>
 *
 * @param images images keyed by material or texture file name
 * @since 0.0.1
 */
public record MeshNamedImage(
    @NotNull Map<String, MeshImage> images
) {

    public static final MeshNamedImage EMPTY = new MeshNamedImage(Collections.emptyMap());

    /**
     * Resolves a texture by name and falls back to {@link MeshImage#EMPTY} when absent.
     *
     * @param name texture name referenced by a parsed mesh face
     * @return the resolved image, or {@link MeshImage#EMPTY} when no texture is available
     * @since 0.0.1
     */
    public @NotNull MeshImage get(@Nullable String name) {
        return name == null ? MeshImage.EMPTY : images.getOrDefault(name, MeshImage.EMPTY);
    }
}

