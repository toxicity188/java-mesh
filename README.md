# java-mesh
A library for converting mesh models for use in Minecraft Java Edition.

## Automatic rectangle merging

A compatible pair of triangles uses one copy of the existing single- or double-sided
model. Ordinary triangles use three copies. Both paths use the IDs supplied to
`MeshBuilder.of(MeshTriangleName.of(single, duplex))`. Merging is scoped to one
load and requires matching material, texture, tint and shared UVs. Registering
a custom matrix modifier keeps the per-triangle path.

In the triangle transformation's local frame, the shared face spans x = [-0.5, 0]
and y = [0, 0.5] at z = 0. Its center is (-0.25, 0.25, 0).
Local -X and +Y follow the rectangle edges, and -Z follows mesh winding.
The transform preserves the model's normal-axis scale.
