# #0001 Release verification via Gradle wrapper hit access denied on shared gradle-9.9.2-bin.zip.lck.

- 2026-11-22T08:09:08Z `issue`: Release verification via Gradle wrapper hit access denied on shared gradle-9.9.2-bin.zip.lck. [gradle/wrapper/gradle-wrapper.properties]
- 2026-11-22T08:10:03Z `attempt`: Ran the Java 21 wrapper offline with shared Gradle cache permission. Build and both publishing task dry-runs succeeded. (worked)
- 2026-11-22T08:10:15Z `fix`: Java 21 Gradle build and dry-runs for Central and GitHub Packages passed with the verified shared wrapper cache.
