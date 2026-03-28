/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

import io.github.toxicity188.javamesh.*;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

public class JsonTest {
    @Test
    public void testJson() {
        MeshBuilder.of(MeshTriangleName.of("triangle_single", "triangle_duplex"))
            .load(MeshShape.of(
                new MeshPoint(new Vector3f(0F, 1F, 0F)),
                new MeshPoint(new Vector3f(1F, 0F, 0F)),
                new MeshPoint(new Vector3f(4F, 0F, 0F)),
                new MeshPoint(new Vector3f(5F, 1F, 0F)),
                new MeshPoint(new Vector3f(2.5F, 5F, -1F))
            ), MeshFace.DOUBLE)
            .toJson();
    }
}
