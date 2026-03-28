/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh.parser;

import io.github.toxicity188.javamesh.MeshShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Parsed mesh data represented as polygons ready for triangulation.
 * <pre>{@code
 * List<MeshShape> shapes = result.toShape();
 * }</pre>
 *
 * @since 0.0.1
 */
public interface MeshParseResult {
    /**
     * Returns the parsed polygons extracted from the mesh source.
     *
     * @return immutable or caller-safe polygon data
     * @since 0.0.1
     */
    @NotNull List<MeshShape> toShape();
}

