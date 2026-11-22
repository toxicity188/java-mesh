/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.joml.Matrix4f;
import io.github.toxicity188.javamesh.parser.MeshParseResult;
import io.github.toxicity188.javamesh.parser.MeshParseContext;
import io.github.toxicity188.javamesh.parser.obj.ObjParser;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

final class ParallelogramTest {
    private static final MeshTriangleName NAMES = MeshTriangleName.of("tri", "tri2");

    private static JsonArray triangleModels(List<MeshShape> shapes, Map<String, MeshImage> textures) {
        var parts = new ArrayList<MeshModel>();
        var images = new MeshNamedImage(textures);
        MeshFace.inferAndBuild(shapes, (shape, face) -> {
            for (var matrix : shape.toTriangleMat(images)) {
                if (matrix.transformation().isFinite()) {
                    MeshModel.create(face == MeshFace.SINGLE ? NAMES.single() : NAMES.duplex(), matrix, parts);
                }
            }
        });
        return MeshModel.composite(parts, new Matrix4f()).getAsJsonArray("models");
    }

    private static MeshPoint point(float x, float y, float z) {
        return new MeshPoint(new Vector3f(x, y, z));
    }

    private static final MeshPoint A = point(0, 0, 0), B = point(3, 0, 0),
            C = point(3, 2, 0), D = point(0, 2, 0);

    private static JsonArray models(MeshBuilder builder) {
        return builder.toJson().getAsJsonArray("models");
    }

    private static void fallback(MeshShape... shapes) {
        var input = List.of(shapes);
        assertEquals(triangleModels(input, Map.of()), models(MeshBuilder.of(NAMES).load(input)));
    }

    @Test void cornersAndFront() {
        checkCorners(List.of(A, B, C, D));
    }

    @Test void shearedCornersAndFront() {
        checkCorners(List.of(A, B, point(4, 2, 0), point(1, 2, 0)));
    }

    private static void checkCorners(List<MeshPoint> input) {
        for (float angle : new float[]{0, 0.43f}) {
            for (float tilt : new float[]{0, 0.81f}) {
                for (var face : MeshFace.values()) {
                    var corners = new ArrayList<MeshPoint>();
                    for (var p : input) {
                        corners.add(new MeshPoint(new Vector3f(p.position()).rotateZ(angle).rotateX(tilt).add(2, -1, 3)));
                    }
                    if (face == MeshFace.DOUBLE) Collections.reverse(corners);
                    var shape = new MeshShape(corners, null);
                    var old = triangleModels(List.of(shape), Map.of());
                    var result = models(MeshBuilder.of(NAMES).load(shape, face));
                    assertEquals(6, old.size());
                    assertEquals(1, result.size());
                    var model = result.get(0).getAsJsonObject();
                    assertEquals(face == MeshFace.SINGLE ? NAMES.single() : NAMES.duplex(), model.get("model").getAsString());
                    assertEquals(old.get(0).getAsJsonObject().get("tints"), model.get("tints"));
                    verifyMatrix(model, corners);
                }
            }
        }
    }

    private static void verifyMatrix(JsonObject model, List<MeshPoint> corners) {
        var rows = model.getAsJsonArray("transformation");
        float[] m = new float[16];
        for (int i = 0; i < m.length; i++) {
            m[i] = rows.get(i).getAsFloat();
            assertTrue(Float.isFinite(m[i]));
        }
        var expected = corners.stream().map(p -> new Vector3f(p.position()).mul(-1, 1, -1).add(0.5f, 0.5f, 0.5f)).toList();
        var origin = new Vector3f(m[3], m[7], m[11]);
        int start = -1;
        for (int i = 0; i < 4; i++) if (origin.distance(expected.get(i)) < 1e-5) start = i;
        assertTrue(start >= 0);
        var x = new Vector3f(m[0], m[4], m[8]).mul(-0.5f);
        var y = new Vector3f(m[1], m[5], m[9]).mul(0.5f);
        assertTrue(new Vector3f(origin).add(x).distance(expected.get((start + 1) % 4)) < 1e-5);
        assertTrue(new Vector3f(origin).add(y).distance(expected.get((start + 3) % 4)) < 1e-5);
        assertTrue(new Vector3f(origin).add(x).add(y).distance(expected.get((start + 2) % 4)) < 1e-5);
        assertTrue(x.cross(y).normalize().distance(new Vector3f(-m[2], -m[6], -m[10])) < 1e-5);
    }

    @Test void independentShapesAndLoadBoundaries() {
        var first = MeshShape.of(A, B, C);
        var second = MeshShape.of(A, C, D);
        var input = List.of(first, second);
        var result = models(MeshBuilder.of(NAMES).load(input));
        assertEquals(1, result.size());
        assertEquals(NAMES.duplex(), result.get(0).getAsJsonObject().get("model").getAsString());
        assertEquals(result, models(MeshBuilder.of(NAMES).load((MeshParseResult) () -> input)));
        assertEquals(6, models(MeshBuilder.of(NAMES).load(List.of(first)).load(List.of(second))).size());
        assertEquals(6, triangleModels(input, Map.of()).size());
    }

    @Test void sharedFaceReachesAllThreeLegacyTriangleTips() {
        var parts = models(MeshBuilder.of(NAMES).load(MeshShape.of(A, B, D), MeshFace.SINGLE));
        assertEquals(3, parts.size());
        var tips = new HashSet<Vector3f>();
        for (var part : parts) {
            var m = part.getAsJsonObject().getAsJsonArray("transformation");
            for (float x : new float[]{-0.5f, 0}) {
                for (float y : new float[]{0, 0.5f}) {
                    var corner = new Vector3f();
                    for (int row = 0; row < 3; row++) {
                        int i = row * 4;
                        corner.setComponent(row, x * m.get(i).getAsFloat() + y * m.get(i + 1).getAsFloat()
                                + m.get(i + 3).getAsFloat());
                    }
                    tips.add(corner);
                }
            }
        }
        for (var vertex : List.of(A, B, D)) {
            assertTrue(tips.contains(new Vector3f(vertex.position()).mul(-1, 1, -1).add(0.5f, 0.5f, 0.5f)));
        }
    }

    @Test void rejectInvalidPairs() {
        var t = MeshShape.of(A, B, C);
        fallback(t, MeshShape.of(D, C, A));
        fallback(t, t);
        fallback(t, MeshShape.of(A, C, D), MeshShape.of(A, C, point(0, 0, 1)));
        fallback(MeshShape.of(A, B, C, point(0, 2, 0.1f)));
        fallback(MeshShape.of(A, B, point(4, 2, 0), D));
        fallback(MeshShape.of(A, B, point(1, 0.5f, 0), C, D));
        fallback(MeshShape.of(A, A, B));
        var thin = point(1, 1e-8f, 0);
        var far = point(4, 1e-8f, 0);
        fallback(MeshShape.of(A, B, far), MeshShape.of(A, far, thin));
        fallback(new MeshShape(List.of(A, B, C), "one"), new MeshShape(List.of(A, C, D), "two"));
    }

    @Test void sampledAndFallbackTintsUseRgb() {
        var a = new MeshPoint(A.position(), new Vector2f(0, 1));
        var b = new MeshPoint(B.position(), new Vector2f(0, 0));
        var c = new MeshPoint(C.position(), new Vector2f(1, 1));
        var d = new MeshPoint(D.position(), new Vector2f(0, 0.99f));
        var input = List.of(new MeshShape(List.of(a, b, c), "img"), new MeshShape(List.of(a, c, d), "img"));
        for (int alpha : new int[]{0, 128, 255}) {
            var image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) image.setRGB(x, y, (alpha << 24) | 0x123456);
            }
            var textures = Map.of("img", MeshImage.from(image));
            var original = triangleModels(input, textures);
            assertEquals(6, original.size());
            for (var part : original) {
                var tint = part.getAsJsonObject().getAsJsonArray("tints").get(0).getAsJsonObject();
                assertEquals(0x123456, tint.get("value").getAsInt());
            }
            assertEquals(1, models(MeshBuilder.of(NAMES).image(textures).load(input)).size());
        }
    }

    @Test void objMaterialBoundaries() throws IOException {
        String geometry = """
                v 0 0 0
                v 3 0 0
                v 3 2 0
                v 0 2 0
                vt 0 0
                usemtl first
                f 1/1/1 2/1/1 3/1/1
                usemtl %s
                f 1/1/1 3/1/1 4/1/1
                """;
        for (String material : List.of("first", "second")) {
            var bytes = geometry.formatted(material).getBytes(StandardCharsets.UTF_8);
            var source = ObjParser.INSTANCE.parse(MeshParseContext.builder("rectangle")
                    .asset("obj", () -> new ByteArrayInputStream(bytes)).build());
            var optimized = models(MeshBuilder.of(NAMES).load(source));
            assertEquals(optimized, models(MeshBuilder.of(NAMES).load(source)));
            if (material.equals("first")) assertEquals(1, optimized.size());
            else assertEquals(triangleModels(source.toShape(), Map.of()), optimized);
        }
    }

    @Test void directQuadAndTrianglesKeepDifferentTints() {
        var image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) image.setRGB(x, y, x * 32 << 16);
        var p = List.of(new MeshPoint(A.position(), new Vector2f(0, 0)),
                new MeshPoint(B.position(), new Vector2f(1, 0)), new MeshPoint(C.position(), new Vector2f(1, 1)),
                new MeshPoint(D.position(), new Vector2f(0, 1)));
        for (var shapes : List.of(List.of(new MeshShape(p, "img")), List.of(new MeshShape(p.subList(0, 3), "img"),
                new MeshShape(List.of(p.get(0), p.get(2), p.get(3)), "img")))) {
            var textures = Map.of("img", MeshImage.from(image));
            var old = triangleModels(shapes, textures);
            assertNotEquals(old.get(0).getAsJsonObject().get("tints"), old.get(3).getAsJsonObject().get("tints"));
            assertEquals(old, models(MeshBuilder.of(NAMES).image(textures).load(shapes)));
        }
    }

    @Test void compositeTransformReplacesAndCopies() {
        var builder = MeshBuilder.of(NAMES);
        var initial = builder.toJson();
        //assertEquals(MeshMath.toJson(new Matrix4f()), initial.get("transformation"));
        var supplied = new Matrix4f().rotateY(0.7f);
        var expected = new Matrix4f(supplied).scale(2);
        builder.matrixModifier(m -> m.translate(1, 2, 3))
                .transformation(supplied)
                .matrixModifier(m -> m.scale(2));
        assertEquals(MeshMath.toJson(new Matrix4f().rotateY(0.7f)), MeshMath.toJson(supplied));
        supplied.zero();
        assertEquals(MeshMath.toJson(expected), builder.toJson().get("transformation"));
        assertEquals(MeshMath.toJson(new Matrix4f()), initial.get("transformation"));
    }

    @Test void modifiersAccumulateImmediatelyOnParentOnly() {
        var quad = MeshShape.of(A, B, C, D);
        var triangle = MeshShape.of(point(10, 1, 2), point(12, 1, 2), point(11, 3, 4));
        var children = models(MeshBuilder.of(NAMES).load(quad, MeshFace.DOUBLE).load(List.of(triangle)));
        var builder = MeshBuilder.of(NAMES).load(quad, MeshFace.DOUBLE);
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        builder.matrixModifier(m -> {
            calls.incrementAndGet();
            m.translate(1, 2, 3);
        });
        assertEquals(1, calls.get());
        builder.matrixModifier(m -> {
            calls.incrementAndGet();
            m.rotateY(0.7f).scale(2, 3, 4);
        });
        assertEquals(2, calls.get());
        builder.load((MeshParseResult) () -> List.of(triangle));
        var json = builder.toJson();
        assertEquals(MeshMath.toJson(new Matrix4f().translate(1, 2, 3).rotateY(0.7f).scale(2, 3, 4)),
                json.get("transformation"));
        assertEquals(4, children.size());
        assertEquals(children, json.get("models"));
        assertEquals(json, builder.toJson());
        assertEquals(2, calls.get());
    }
}
