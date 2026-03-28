/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import com.google.gson.JsonObject;
import io.github.toxicity188.javamesh.parser.MeshParseResult;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.Consumer;

/**
 * Builds Minecraft resource-pack model JSON from parsed mesh polygons.
 * <p>
 * A builder instance collects triangulated mesh faces, applies an optional matrix transform,
 * and emits the final composite model definition used by Minecraft item models.
 * </p>
 * <pre>{@code
 * var json = MeshBuilder.of(MeshTriangleName.of("item/mesh_triangle", "item/mesh_triangle_double"))
 *     .image(Map.of("blade.png", MeshImage.from(texture)))
 *     .load(kr.toxicity.library.javamesh.parser.obj.ObjParser.INSTANCE.parse(
 *         kr.toxicity.library.javamesh.parser.MeshParseContext.builder("sword")
 *             .asset("obj", () -> Files.newInputStream(objPath))
 *             .asset("mtl", () -> Files.newInputStream(mtlPath))
 *             .build()
 *     ))
 *     .toJson();
 * }</pre>
 *
 * @since 0.0.1
 */
public final class MeshBuilder {

    private final MeshTriangleName name;
    private MeshNamedImage image = MeshNamedImage.EMPTY;
    private final List<MeshModel> models = new ArrayList<>();
    private Consumer<Matrix4f> matrixModifier = mat -> {};

    /**
     * Creates a builder configured with the model ids used for single-sided and double-sided triangles.
     *
     * @param name triangle model identifiers used in the generated Minecraft model JSON
     * @return a new builder instance
     * @since 0.0.1
     */
    public static @NotNull MeshBuilder of(@NotNull MeshTriangleName name) {
        return new MeshBuilder(name);
    }

    private MeshBuilder(@NotNull MeshTriangleName name) {
        this.name = name;
    }

    /**
     * Registers texture images by resource name so triangle tint colors can be sampled from UVs.
     *
     * @param image images keyed by the texture name referenced by parsed mesh faces
     * @return this builder
     * @throws NullPointerException if {@code image} is {@code null}
     * @since 0.0.1
     */
    public @NotNull MeshBuilder image(@NotNull Map<String, MeshImage> image) {
        this.image = new MeshNamedImage(Collections.unmodifiableMap(Objects.requireNonNull(image)));
        return this;
    }

    /**
     * Registers a callback that can mutate each triangle transform before JSON serialization.
     *
     * @param matrixModifier callback applied to every finite triangle transformation matrix
     * @return this builder
     * @throws NullPointerException if {@code matrixModifier} is {@code null}
     * @since 0.0.1
     */
    public @NotNull MeshBuilder matrixModifier(@NotNull Consumer<Matrix4f> matrixModifier) {
        this.matrixModifier = Objects.requireNonNull(matrixModifier);
        return this;
    }

    /**
     * Loads parsed mesh data into this builder.
     *
     * @param result parser output to convert into Minecraft triangles
     * @return this builder
     * @throws NullPointerException if {@code result} is {@code null}
     * @throws IllegalArgumentException if the parsed mesh contains degenerate polygons
     * @throws IllegalStateException if polygon triangulation fails
     * @since 0.0.1
     */
    public @NotNull MeshBuilder load(@NotNull MeshParseResult result) {
        return load(result.toShape());
    }

    /**
     * Loads polygon shapes and infers whether each shape should use a single-sided or double-sided triangle model.
     *
     * @param shapes polygon shapes extracted from a mesh source such as OBJ
     * @return this builder
     * @throws NullPointerException if {@code shapes} is {@code null}
     * @throws IllegalArgumentException if a shape contains degenerate polygon data
     * @throws IllegalStateException if polygon triangulation fails
     * @since 0.0.1
     */
    public @NotNull MeshBuilder load(@NotNull List<MeshShape> shapes) {
        MeshFace.inferAndBuild(shapes, this::load);
        return this;
    }

    /**
     * Loads a single shape using the specified triangle face type.
     *
     * @param shape polygon to triangulate and serialize
     * @param face face type that selects the single-sided or double-sided triangle model id
     * @return this builder
     * @throws NullPointerException if {@code shape} or {@code face} is {@code null}
     * @throws IllegalArgumentException if the shape contains degenerate polygon data
     * @throws IllegalStateException if polygon triangulation fails
     * @since 0.0.1
     */
    public @NotNull MeshBuilder load(@NotNull MeshShape shape, @NotNull MeshFace face) {
        var n = face == MeshFace.SINGLE ? name.single() : name.duplex();
        for (MeshMatrix mat : shape.toTriangleMat(image)) {
            if (!mat.transformation().isFinite()) continue;
            matrixModifier.accept(mat.transformation());
            MeshModel.create(n, mat, models);
        }
        return this;
    }

    /**
     * Serializes the loaded mesh into Minecraft composite model JSON.
     *
     * @return a composite model JSON object ready for resource-pack output
     * @since 0.0.1
     */
    public @NotNull JsonObject toJson() {
        return MeshModel.composite(models);
    }
}

