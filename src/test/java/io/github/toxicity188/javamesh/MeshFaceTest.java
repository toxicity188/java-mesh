/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MeshFaceTest {
    private static final MeshPoint A = point(0, 0, 0);
    private static final MeshPoint B = point(1, 0, 0);
    private static final MeshPoint C = point(0, 1, 0);
    private static final MeshPoint D = point(0, 0, 1);

    private static MeshPoint point(float x, float y, float z) {
        return new MeshPoint(new Vector3f(x, y, z));
    }

    private static List<MeshShape> shell(MeshShape base) {
        return List.of(base, MeshShape.of(B, A, D), MeshShape.of(C, B, D), MeshShape.of(A, C, D));
    }

    private static void expect(MeshFace face, List<MeshShape> input) {
        var output = new ArrayList<MeshFace>();
        MeshFace.inferAndBuild(input, (shape, side) -> output.add(side));
        assertEquals(Collections.nCopies(input.size(), face), output);
    }

    @Test void closedWithSignedZero() {
        expect(MeshFace.SINGLE, shell(MeshShape.of(point(-0f, 0, 0), B, C)));
    }

    @Test void open() {
        expect(MeshFace.DOUBLE, shell(MeshShape.of(A, B, C)).subList(1, 4));
    }

    @Test void reversedFaceInClosedShell() {
        expect(MeshFace.DOUBLE, shell(MeshShape.of(A, C, B)));
    }

    @Test void threeOwnersInOtherwiseClosedShell() {
        var base = MeshShape.of(A, B, C);
        var faces = new ArrayList<>(shell(base));
        faces.add(base);
        expect(MeshFace.DOUBLE, faces);
    }

    @Test void degenerateAndRepeatedEdges() {
        expect(MeshFace.DOUBLE, shell(MeshShape.of(A, A, B, C)));
        var tip = point(-1, 0, 0);
        expect(MeshFace.DOUBLE, shell(MeshShape.of(A, tip, A, B, C)));
    }

    @Test void nearbyPositionsDoNotSealACrack() {
        expect(MeshFace.DOUBLE, shell(MeshShape.of(point(0.00001f, 0, 0), B, C)));
    }
}
