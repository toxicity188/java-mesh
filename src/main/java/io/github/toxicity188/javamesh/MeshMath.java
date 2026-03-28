/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import com.google.gson.JsonArray;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static java.lang.Math.*;

final class MeshMath {

    private MeshMath() {
        throw new RuntimeException();
    }

    static @NotNull JsonArray toJson(@NotNull Matrix4f mat) {
        var json = new JsonArray(16);
        json.add(mat.m00());
        json.add(mat.m10());
        json.add(mat.m20());
        json.add(mat.m30());
        json.add(mat.m01());
        json.add(mat.m11());
        json.add(mat.m21());
        json.add(mat.m31());
        json.add(mat.m02());
        json.add(mat.m12());
        json.add(mat.m22());
        json.add(mat.m32());
        json.add(mat.m03());
        json.add(mat.m13());
        json.add(mat.m23());
        json.add(mat.m33());
        return json;
    }

    static @NotNull MeshMatrix triangleMat(
        @NotNull MeshPoint p0,
        @NotNull MeshPoint p1,
        @NotNull MeshPoint p2,
        @NotNull MeshImage image
    ) {
        return new MeshMatrix(
            triangleMat(
                p0.position(),
                p1.position(),
                p2.position()
            ),
            sampleTriangleAverageColor(
                image,
                p0.uv(),
                p1.uv(),
                p2.uv()
            )
        );
    }

    static @NotNull Matrix4f triangleMat(
            @NotNull Vector3f p0,
            @NotNull Vector3f p1,
            @NotNull Vector3f p2
    ) {
        var xAxis = new Vector3f(p1).sub(p0).mul(1, -1, 1);
        var yAxis = new Vector3f(p2).sub(p0).mul(-1, 1, -1);
        var zAxis = new Vector3f(xAxis).cross(yAxis);

        zAxis.normalize();

        var mat = new Matrix4f();

        // column 0 = local X axis
        mat.m00(xAxis.x);
        mat.m01(xAxis.y);
        mat.m02(xAxis.z);
        mat.m03(0.0f);

        // column 1 = local Y axis
        mat.m10(yAxis.x);
        mat.m11(yAxis.y);
        mat.m12(yAxis.z);
        mat.m13(0.0f);

        // column 2 = local Z axis
        mat.m20(zAxis.x);
        mat.m21(zAxis.y);
        mat.m22(zAxis.z);
        mat.m23(0.0f);

        return mat.translateLocal(-p0.x + 0.5F, p0.y + 0.5F, -p0.z + 0.5F);
    }

    static int sampleTriangleAverageColor(
        @NotNull MeshImage image,
        @NotNull Vector2f uv0,
        @NotNull Vector2f uv1,
        @NotNull Vector2f uv2
    ) {
        var width = image.width();
        var height = image.height();

        var p0 = uvToPixel(uv0, width, height);
        var p1 = uvToPixel(uv1, width, height);
        var p2 = uvToPixel(uv2, width, height);

        var minX = max(0, (int) floor(min(p0.x, min(p1.x, p2.x))));
        var maxX = min(width - 1, (int) ceil(max(p0.x, max(p1.x, p2.x))));
        var minY = max(0, (int) floor(min(p0.y, min(p1.y, p2.y))));
        var maxY = min(height - 1, (int) ceil(max(p0.y, max(p1.y, p2.y))));

        var sumR = 0L;
        var sumG = 0L;
        var sumB = 0L;
        var count = 0;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                var px = x + 0.5f;
                var py = y + 0.5f;

                if (!pointInTriangle(px, py, p0, p1, p2)) {
                    continue;
                }

                var argb = image.rgb(x, y);
                var r = (argb >>> 16) & 0xFF;
                var g = (argb >>> 8) & 0xFF;
                var b = argb & 0xFF;

                sumR += r;
                sumG += g;
                sumB += b;
                count++;
            }
        }

        if (count == 0) {
            var center = new Vector2f(uv0).add(uv1).add(uv2).div(3.0f);
            var pc = uvToPixel(center, width, height);

            return image.rgb(
                clamp(round(pc.x), 0, width - 1),
                clamp(round(pc.y), 0, height - 1)
            );
        }

        var r = (int) (sumR / count);
        var g = (int) (sumG / count);
        var b = (int) (sumB / count);

        return ((r & 0xFF) << 16)
            | ((g & 0xFF) << 8)
            | (b & 0xFF);
    }

    private static Vector2f uvToPixel(@NotNull Vector2f uv, int width, int height) {
        return new Vector2f(
            uv.x * (width - 1),
            (1.0f - uv.y) * (height - 1)
        );
    }

    private static boolean pointInTriangle(float px, float py, @NotNull Vector2f a, @NotNull Vector2f b, @NotNull Vector2f c) {
        var d1 = sign(px, py, a, b);
        var d2 = sign(px, py, b, c);
        var d3 = sign(px, py, c, a);

        var hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        var hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(hasNeg && hasPos);
    }

    private static float sign(float px, float py, @NotNull Vector2f a, @NotNull Vector2f b) {
        var abx = (double) a.x - b.x;
        var aby = (double) a.y - b.y;
        var pbx = (double) px - b.x;
        var pby = (double) py - b.y;

        return (float) fma(pbx, aby, -abx * pby);
    }
}
