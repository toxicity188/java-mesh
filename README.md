# java-mesh
A library for converting mesh models for use in Minecraft Java Edition.

## Automatic parallelogram merging

A compatible pair of triangles forming a nondegenerate parallelogram uses one copy of the existing single- or double-sided
model. Ordinary triangles use three copies. Both paths use the IDs supplied to
`MeshBuilder.of(MeshTriangleName.of(single, duplex))`. Merging is scoped to one
load and requires matching material, texture, tint and shared UVs.
The composite carries the whole-mesh transformation (identity by default).
`builder.transformation(matrix)` replaces it with a defensive copy.
`builder.matrixModifier(m -> m.scale(2))` applies immediately and once per call.
Changes affect previously loaded and subsequently loaded shapes alike.

In the triangle transformation's local frame, the shared face spans x = [-0.5, 0]
and y = [0, 0.5] at z = 0. Its center is (-0.25, 0.25, 0).
Local -X and +Y follow the parallelogram edges, and -Z follows mesh winding.
The transform preserves the model's normal-axis scale.
Non-orthogonal edges produce shear in the plane. Both average and fallback
texture samples produce a 24-bit RGB tint; the alpha byte is ignored.
