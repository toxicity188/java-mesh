/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

record MeshMatrix(@NotNull Matrix4f transformation, int tint) {
}
