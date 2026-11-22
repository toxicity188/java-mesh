# #0002 Investigate single-model rectangle rendering for planar mesh regions while preserving tint and face sidedness

- 2026-11-22T08:40:37Z `issue`: Investigate single-model rectangle rendering for planar mesh regions while preserving tint and face sidedness [src/main/java/io/github/toxicity188/javamesh/MeshBuilder.java:126]
- 2026-11-22T08:55:50Z `attempt`: Implemented batch-scoped shared-edge rectangle merging with material, tint, winding and callback guards. Java 21 Gradle test and javadoc succeeded. (worked)
- 2026-11-22T08:56:02Z `fix`: Eight JUnit tests passed, including OBJ merging, corner and normal mapping, tint and fallback JSON equivalence. Javadoc passed. Minecraft client rendering remains unverified.
