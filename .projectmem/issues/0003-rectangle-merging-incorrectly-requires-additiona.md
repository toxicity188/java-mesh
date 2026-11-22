# #0003 Rectangle merging incorrectly requires additional model IDs and assumes a unit-square template.

- 2026-11-22T09:03:35Z `issue`: Rectangle merging incorrectly requires additional model IDs and assumes a unit-square template.
- 2026-11-22T09:05:08Z `attempt`: Reused triangle IDs automatically; mapped inferred half-unit face and normal axis. Full Gradle test and javadoc passed. (worked)
- 2026-11-22T09:05:18Z `fix`: Automatic reuse of single/duplex IDs validated by 9 passing tests, including legacy triangle tips and transformed rectangle corners. Javadoc and diff check passed. Client rendering unverified.
