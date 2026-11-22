# java-mesh contributor guide

These instructions apply throughout the repository. Follow any more specific `AGENTS.md` in the directory you change.

## Purpose and structure

java-mesh is a Java 21 library that converts triangle mesh geometry into Minecraft Java Edition item model JSON. It can triangulate polygon inputs and use texture samples and UV coordinates to derive triangle tints.

- `src/main/java/io/github/toxicity188/javamesh/` contains the conversion API and implementation. `MeshBuilder` accepts parsed assets or mesh shapes and builds composite item model JSON. Geometry helpers handle triangulation, transforms, and texture sampling.
- `src/main/java/io/github/toxicity188/javamesh/parser/` defines asset loading and parser contracts. `parser/obj/` handles Wavefront OBJ assets. Keep format-specific parsing here and Minecraft JSON generation in the conversion code.
- `src/test/java/` contains JUnit tests. `src/test/resources/` contains mesh and texture fixtures.
- `build.gradle.kts` configures the Java 21 toolchain, dependencies, tests, and publishing. `gradle.properties` tracks the project version. Use the Gradle wrapper.

## Java style

- Write production and test source in Java 21. Match nearby code and `.editorconfig`. Use UTF-8, LF line endings, four-space indentation, and the existing Java license header for new source files.
- Use `UpperCamelCase` for types, `lowerCamelCase` for methods and variables, and `UPPER_SNAKE_CASE` for constants. Keep new types under the existing package root.
- Use `var` for locals when the initializer makes the type clear. Declare classes `final` when they are closed to inheritance. Prefer records for immutable data carriers and `of(...)` for simple value factories. Use an enum singleton when the design calls for a singleton.
- Match existing JetBrains nullability annotations. Give implementation helpers the narrowest visibility that serves their callers.
- Reuse local helpers and JDK APIs first. Follow the existing JOML, Gson, and fastutil patterns for matrix math, JSON, and primitive collections. Justify any new dependency in terms of need, scope, and maintenance cost.

## Javadoc

- Write English Javadoc for new public API types, constructors, and methods. Update affected Javadoc when an existing public contract changes.
- Document all parameters, type parameters, and record components with `@param`. Put record component tags on the record. Use `@return` for non-void methods and `@throws` for each exception callers are expected to handle or avoid.
- Set `@since` to the version that introduces the API, using `project_version` in `gradle.properties` for new APIs. Keep existing `@since` values.
- Include a short `{@code ...}` snippet or code block at the relevant public entry point when showing a workflow. Document caller-visible details such as coordinate conventions, transforms, tint values, and the generated JSON structure where they apply.

## Exceptions and compatibility

- Use the existing unchecked exceptions for invalid arguments or failed conversion. Preserve the checked `IOException` contract at the parser and asset-loading boundaries. Keep error messages specific and preserve the cause when wrapping failures.
- Preserve public signatures, documented behavior, and generated JSON unless the requested change requires a different contract. Document the impact and migration path when a breaking change is required.
- Keep changes focused. Reuse existing code and follow local conventions in the files you touch. Add an abstraction when current callers need it.

## Verification

- Run `./gradlew test` for code changes and `./gradlew javadoc` for public API documentation changes. Use `.\gradlew.bat test` and `.\gradlew.bat javadoc` on Windows.
- Add focused regression coverage for changed behavior. Use small fixtures and check the generated JSON when the conversion contract changes.
- Use Conventional Commits for commits. Summarize the reason for the change, compatibility impact, and checks run in the pull request.
