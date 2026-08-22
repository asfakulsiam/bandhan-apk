# Project Maintenance Rules

## Automatic Version Bumping Rule (MANDATORY)
As the maintainer of this project, whenever you touch the codebase, fix a bug, or implement a feature/change:
1. **Always increment `versionCode`** by +1 in `app/build.gradle.kts` (e.g., 4 -> 5).
2. **Always increment `versionName`** appropriately in `app/build.gradle.kts` (e.g., "1.0.3" -> "1.0.4").
3. **Preserve Release Keystore**: Never alter or overwrite `release.keystore` / `release.keystore.base64` or change the signing certificate configuration so in-place upgrades succeed without "App not installed" errors.
4. **Repository Link**: The canonical repository is `asfakulsiam/bandhan-apk`.
