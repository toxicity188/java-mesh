/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

import io.github.toxicity188.javamesh.MeshBuilder;
import io.github.toxicity188.javamesh.MeshImage;
import io.github.toxicity188.javamesh.MeshTriangleName;
import io.github.toxicity188.javamesh.parser.MeshParseContext;
import io.github.toxicity188.javamesh.parser.obj.ObjParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class SwordTest {

    // https://sketchfab.com/3d-models/handpainted-low-poly-sword-41ec50ef7fc146339331913401fcb698
    @Test
    public void testObject() {
        try (
            var texture = getClass().getResourceAsStream("/handpaint_sword.png")
        ) {
            Assertions.assertNotNull(texture);

            var json = MeshBuilder.of(MeshTriangleName.of("triangle_single", "triangle_duplex"))
                .image(Map.ofEntries(
                    Map.entry("handpaint_sword.png", MeshImage.from(ImageIO.read(texture)))
                ))
                .load(ObjParser.INSTANCE.parse(MeshParseContext.builder("handpaint_sword")
                    .asset("obj", () -> Objects.requireNonNull(getClass().getResourceAsStream("/handpaint_sword.obj")))
                    .asset("mtl", () -> Objects.requireNonNull(getClass().getResourceAsStream("/handpaint_sword.mtl")))
                    .build()))
                .toJson();

            System.out.println("Mesh: " + json.getAsJsonArray("models").size());
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }
}
