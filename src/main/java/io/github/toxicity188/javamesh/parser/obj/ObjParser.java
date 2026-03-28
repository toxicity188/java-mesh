/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh.parser.obj;

import io.github.toxicity188.javamesh.MeshPoint;
import io.github.toxicity188.javamesh.MeshShape;
import io.github.toxicity188.javamesh.parser.MeshParseContext;
import io.github.toxicity188.javamesh.parser.MeshParseResult;
import io.github.toxicity188.javamesh.parser.MeshParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * OBJ and MTL parser that converts Wavefront mesh assets into polygons for Minecraft model generation.
 * <pre>{@code
 * MeshParseResult result = ObjParser.INSTANCE.parse(
 *     MeshParseContext.builder("blade")
 *         .asset("obj", () -> Files.newInputStream(objPath))
 *         .asset("mtl", () -> Files.newInputStream(mtlPath))
 *         .build()
 * );
 * }</pre>
 *
 * @since 0.0.1
 */
public enum ObjParser implements MeshParser {
    /**
     * Singleton instance.
     *
     * @since 0.0.1
     */
    INSTANCE
    ;

    /**
     * Parses OBJ geometry and optional MTL material bindings into mesh polygons.
     *
     * @param context parser context providing OBJ and optional MTL assets
     * @return parsed mesh polygons
     * @throws IOException if any registered asset stream cannot be opened or read
     * @since 0.0.1
     */
    @Override
    public @NotNull MeshParseResult parse(@NotNull MeshParseContext context) throws IOException {
        try (
            var mtlStream = context.loadExtensionOrNull("mtl");
            var mtlReader = mtlStream == null ? null : new InputStreamReader(mtlStream, StandardCharsets.UTF_8);
            var mtlBuffer = mtlReader == null ? null : new BufferedReader(mtlReader);

            var objStream = context.loadExtension("obj");
            var objReader = new InputStreamReader(objStream, StandardCharsets.UTF_8);
            var objBuffer = new BufferedReader(objReader)
        ) {
            // mtl
            var map = new HashMap<String, String>();
            if (mtlBuffer != null) {
                String mtlNext;
                while ((mtlNext = mtlBuffer.readLine()) != null) {
                    var keySplit = mtlNext.split(" ");
                    if (!keySplit[0].equals("newmtl")) continue;
                    String value;
                    while ((value = mtlBuffer.readLine()) != null) {
                        var valueSplit = value.split(" ");
                        if (!valueSplit[0].equals("map_Kd")) continue;
                        map.put(keySplit[1], valueSplit[1]);
                        break;
                    }
                }
            }

            // obj
            var v = new ArrayList<Vector3f>();
            var vt = new ArrayList<Vector2f>();
            var f = new ArrayList<F>();

            String mtlRef = null;
            String next;
            while ((next = objBuffer.readLine()) != null) {
                var split = next.split(" ");
                switch (split[0]) {
                    case "v" -> v.add(parseF(split[1], split[2], split[3]).mul(context.scale()));
                    case "vt" -> vt.add(parseF(split[1], split[2]));
                    case "f" -> {
                        var indexes = new ArrayList<Vector3i>(split.length - 1);
                        for (int i = 1; i < split.length; i++) {
                            indexes.add(parseI(split[i].split("/")).sub(ONE));
                        }
                        f.add(new F(indexes, mtlRef));
                    }
                    case "usemtl" -> mtlRef = map.get(split[1]);
                    default -> {}
                }
            }
            return new ObjParseResult(v, vt, f);
        }
    }

    private record ObjParseResult(
        @NotNull List<Vector3f> v,
        @NotNull List<Vector2f> vt,
        @NotNull List<F> f
    ) implements MeshParseResult {

        @Override
        public @NotNull List<MeshShape> toShape() {
            return f.stream()
                .map(i -> new MeshShape(
                    i.indexes.stream().map(vec -> new MeshPoint(v.get(vec.x), vt.get(vec.y))).toList(),
                    i.texture
                ))
                .toList();
        }
    }

    private record F(@NotNull List<Vector3i> indexes, @Nullable String texture) {}

    private static final Vector3i ONE = new Vector3i(1);

    private static @NotNull Vector3i parseI(@NotNull String[] raw) {
        return parseI(raw[0], raw[1], raw[2]);
    }

    private static @NotNull Vector3i parseI(@NotNull String x, @NotNull String y, @NotNull String z) {
        return new Vector3i(
            Integer.parseInt(x),
            Integer.parseInt(y),
            Integer.parseInt(z)
        );
    }

    private static @NotNull Vector2f parseF(@NotNull String x, @NotNull String y) {
        return new Vector2f(
            Float.parseFloat(x),
            Float.parseFloat(y)
        );
    }
    private static @NotNull Vector3f parseF(@NotNull String x, @NotNull String y, @NotNull String z) {
        return new Vector3f(
            Float.parseFloat(x),
            Float.parseFloat(y),
            Float.parseFloat(z)
        );
    }
}

