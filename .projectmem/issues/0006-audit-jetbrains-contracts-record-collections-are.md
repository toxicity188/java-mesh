# #0006 Audit JetBrains contracts: record collections are stored by reference and cannot promise unmodifiable access.

- 2026-11-22T10:31:17Z `issue`: Audit JetBrains contracts: record collections are stored by reference and cannot promise unmodifiable access. [src/main/java/io/github/toxicity188/javamesh/MeshShape.java:38]
- 2026-11-22T10:32:48Z `fix`: Removed incorrect record collection immutability promises and annotated nullable internal paths and verified contracts. Java compilation, Javadoc and IntelliJ inspections passed; unrelated identity assertion tracked in #0007.
