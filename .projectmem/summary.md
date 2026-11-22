# projectmem - java-mesh

_Last updated: 2026-11-22_

## Project purpose
A library for converting mesh models for use in Minecraft Java Edition.

## Recent issues
- [DONE] #0003 Rectangle merging incorrectly requires additional model IDs and assumes a unit-square template. (fixed)
- [DONE] #0002 Investigate single-model rectangle rendering for planar mesh regions while preserving tint and face sidedness [src/main/java/io/github/toxicity188/javamesh/MeshBuilder.java:126] (fixed)
- [DONE] #0001 Release verification via Gradle wrapper hit access denied on shared gradle-9.9.2-bin.zip.lck. [gradle/wrapper/gradle-wrapper.properties] (fixed)
- [DONE] #legacy_4f2a Legacy issue: fix: group (fixed)

## Decisions
- Publish from a GitHub Release whose tag exactly matches project_version. Reuse the Vanniktech Maven publication for Central and GitHub Packages and use the workflow GITHUB_TOKEN for Packages. [.github/workflows/release.yml]

## Notes
- Initial Commit
- chore: bump build
- Root AGENTS.md documents java-mesh's Java 21 conversion and parser boundaries, public Javadoc conventions, and Gradle verification commands. [AGENTS.md:1]

## Key files
- `.editorconfig`
- `.gitattributes`
- `.gitignore`
- `.idea/codeStyles/codeStyleConfig.xml`
- `LICENSE.md`
- `LICENSE_HEADER`
- `README.md`
- `build.gradle.kts`
- `gradle.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradlew`
- `gradlew.bat`
- `settings.gradle.kts`
- `src/main/java/io/github/toxicity188/javamesh/MeshBuilder.java`
- `src/main/java/io/github/toxicity188/javamesh/MeshFace.java`
- `src/main/java/io/github/toxicity188/javamesh/MeshImage.java`
- `src/main/java/io/github/toxicity188/javamesh/MeshMath.java`
- `src/main/java/io/github/toxicity188/javamesh/MeshMatrix.java`
- `src/main/java/io/github/toxicity188/javamesh/MeshModel.java`

## Open questions
- None logged yet.
