/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh.parser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Parses mesh source assets into polygon data that can later be serialized as Minecraft models.
 * <pre>{@code
 * MeshParseResult result = kr.toxicity.library.javamesh.parser.obj.ObjParser.INSTANCE.parse(context);
 * }</pre>
 *
 * @since 0.0.1
 */
public interface MeshParser {
    /**
     * Parses mesh assets described by the provided context.
     *
     * @param context parser input context
     * @return parsed polygon data
     * @throws IOException if the parser cannot read one of the source assets
     * @since 0.0.1
     */
    @NotNull MeshParseResult parse(@NotNull MeshParseContext context) throws IOException;
}

