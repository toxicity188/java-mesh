/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.List;

record MeshModel(
    @NotNull String model,
    int tint,
    @NotNull Matrix4f transformation
) {
    private static final String TYPE_MODEL = "model";
    private static final String TYPE_COMPOSITE = "composite";
    private static final String TYPE_TINT_CONSTANT = "constant";

    private static final Matrix4f MAT_PART_1 = new Matrix4f()
        .m10(1F)
        .m30(-0.5F);
    private static final Matrix4f MAT_PART_2 = new Matrix4f()
        .m01(1F)
        .m31(0.5F);

    static @NotNull JsonObject composite(@NotNull List<MeshModel> models, @NotNull Matrix4f transformation) {
        var json = new JsonObject();
        json.addProperty("type", TYPE_COMPOSITE);
        if ((transformation.properties() & Matrix4fc.PROPERTY_IDENTITY) == 0) {
            json.add("transformation", MeshMath.toJson(transformation));
        }
        var arr = new JsonArray(models.size());
        for (MeshModel model : models) {
            arr.add(model.toJson());
        }
        json.add("models", arr);
        return json;
    }

    static void create(
        @NotNull String model,
        @NotNull MeshMatrix matrix,
        @NotNull List<MeshModel> dest
    ) {
        var tint = matrix.tint();
        var transformation = matrix.transformation();
        dest.add(new MeshModel(model, tint, transformation));
        dest.add(new MeshModel(model, tint, transformation.mul(MAT_PART_1, new Matrix4f())));
        dest.add(new MeshModel(model, tint, transformation.mul(MAT_PART_2, new Matrix4f())));
    }

    @NotNull JsonObject toJson() {
        var json = new JsonObject();
        json.addProperty("type", TYPE_MODEL);
        json.addProperty("model", model);
        json.add("transformation", MeshMath.toJson(transformation));
        var constant = new JsonObject();
        constant.addProperty("type", TYPE_TINT_CONSTANT);
        constant.addProperty("value", tint);
        var tints = new JsonArray(1);
        tints.add(constant);
        json.add("tints", tints);
        return json;
    }
}
