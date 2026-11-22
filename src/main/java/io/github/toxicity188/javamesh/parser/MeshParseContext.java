/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh.parser;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Input configuration for mesh parsers.
 * <p>
 * The context supplies a logical asset name, a global scale factor, and lazily-opened streams for
 * source files such as OBJ and MTL so they can be converted into Minecraft resource-pack models.
 * </p>
 * <pre>{@code
 * MeshParseContext context = MeshParseContext.builder("sword")
 *     .scale(0.5f)
 *     .asset("obj", () -> Files.newInputStream(objPath))
 *     .asset("mtl", () -> Files.newInputStream(mtlPath))
 *     .build();
 * }</pre>
 *
 * @param name logical mesh name used by parsers
 * @param scale global scale applied while reading mesh coordinates
 * @param loaders asset loaders keyed by file extension
 * @since 0.0.1
 */
public record MeshParseContext(
    @NotNull String name,
    float scale,
    @NotNull Map<String, StreamLoader> loaders
) {

    /**
     * Starts building a parser context for the given mesh asset name.
     *
     * @param name logical mesh name
     * @return a new builder
     * @since 0.0.1
     */
    public static @NotNull Builder builder(@NotNull String name) {
        return new Builder(name);
    }

    /**
     * Opens the stream registered for the given extension.
     *
     * @param extension asset extension such as {@code "obj"} or {@code "mtl"}
     * @return the opened stream
     * @throws IOException if the underlying loader fails while opening the stream
     * @throws NullPointerException if no loader is registered for {@code extension}
     * @since 0.0.1
     */
    public @NotNull InputStream loadExtension(@NotNull String extension) throws IOException {
        return Objects.requireNonNull(loadExtensionOrNull(extension));
    }

    /**
     * Opens the stream registered for the given extension when available.
     *
     * @param extension asset extension such as {@code "obj"} or {@code "mtl"}
     * @return the opened stream, or {@code null} when the extension is not registered
     * @throws IOException if the underlying loader fails while opening the stream
     * @since 0.0.1
     */
    public @Nullable InputStream loadExtensionOrNull(@NotNull String extension) throws IOException {
        var loader = loaders.get(extension);
        return loader != null ? loader.load() : null;
    }

    /**
     * Fluent builder for {@link MeshParseContext}.
     * <pre>{@code
     * MeshParseContext.Builder builder = MeshParseContext.builder("shield");
     * }</pre>
     *
     * @since 0.0.1
     */
    public static final class Builder {
        private final String name;
        private float scale = 1F;
        private final Map<String, StreamLoader> loaders = new HashMap<>();

        private Builder(@NotNull String name) {
            this.name = name;
        }

        /**
         * Registers a loader for an asset extension used by a parser.
         *
         * @param name extension key such as {@code "obj"} or {@code "mtl"}
         * @param loader callback that opens the stream on demand
         * @return this builder
         * @since 0.0.1
         */
        public @NotNull Builder asset(@NotNull String name, @NotNull StreamLoader loader) {
            loaders.put(name, loader);
            return this;
        }

        /**
         * Sets the global mesh scale applied during parsing.
         *
         * @param scale coordinate scale multiplier
         * @return this builder
         * @since 0.0.1
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        /**
         * Finalizes the parser context.
         *
         * @return a parser context with an unmodifiable view of the builder's loaders
         * @since 0.0.1
         */
        public @NotNull MeshParseContext build() {
            return new MeshParseContext(
                name,
                scale,
                Collections.unmodifiableMap(loaders)
            );
        }
    }

    /**
     * Opens an asset stream used by a mesh parser.
     * <pre>{@code
     * MeshParseContext.StreamLoader loader = () -> Files.newInputStream(objPath);
     * }</pre>
     *
     * @since 0.0.1
     */
    public interface StreamLoader {
        /**
         * Opens the asset stream.
         *
         * @return an open stream for the requested asset
         * @throws IOException if the stream cannot be opened
         * @since 0.0.1
         */
        @NotNull InputStream load() throws IOException;
    }
}

