# #0005 Generalize rectangle merging to affine parallelograms and normalize fallback tint to RGB.

- 2026-11-22T09:38:54Z `issue`: Generalize rectangle merging to affine parallelograms and normalize fallback tint to RGB.
- 2026-11-22T09:41:11Z `attempt`: Verified real OBJ output in three stages: Wakamo 36861, 36811, 36791 models; Laevatain 216594 in all stages. (worked)
- 2026-11-22T09:41:15Z `fix`: Affine parallelogram merging and 24-bit fallback tint verified by all 15 tests, offline Javadoc, and git diff --check.
