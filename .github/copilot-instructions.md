# Copilot Instructions for TextEditor

## Project overview
- This repository is a single-module Android app (`:app`) built with Kotlin + ViewBinding.
- App purpose: edit `txt`, `json`, `xml` files and apply syntax styling for JSON/XML.
- Main user flow is implemented in `app/src/main/java/com/texteditor/MainActivity.kt`.

## Architecture and data flow
- UI + behavior are intentionally in one screen (`MainActivity`) and one layout (`activity_main.xml`).
- File I/O boundary uses Android Storage Access Framework via `OpenDocument` / `CreateDocument` launchers.
- Read flow: user selects document → `readText(uri)` → editor text set → file type inferred from extension.
- Save flow: save to existing `currentUri`, or create a new document with type-based default name.
- Highlight flow: `TextWatcher.afterTextChanged` → `applySyntaxHighlighting()` → `SyntaxHighlighter.highlight(...)`.

## Syntax highlighting conventions
- Keep language mode source-of-truth in `FileType` enum (`TXT`, `JSON`, `XML`).
- JSON/XML styling is regex-based in `SyntaxHighlighter.kt`; avoid introducing parser dependencies unless requested.
- Preserve caret position after re-highlighting (`selectionStart`/`selectionEnd` restoration pattern in `MainActivity`).
- Prefer extending existing regex/color mapping before adding new editor components.

## Build and CI workflows
- Local build command: `./gradlew assembleDebug`.
- APK output path: `app/build/outputs/apk/debug/app-debug.apk`.
- CI workflow is defined in `.github/workflows/android-apk.yml` and uploads debug APK artifact.
- CI expects JDK 17 and Android SDK setup; keep workflow changes aligned with `README.md` instructions.

## Project-specific implementation patterns
- Use `Snackbar` for user feedback (`File loaded`, `Saved`) rather than adding new notification mechanisms.
- Keep dependencies minimal (`androidx`, `material`); do not add large editor frameworks by default.
- Theme/resources are centralized under `app/src/main/res/values`; add colors/strings there instead of hardcoding in Kotlin.
- Prefer incremental edits over introducing extra modules, services, or architecture layers.

## Files to consult first
- `app/src/main/java/com/texteditor/MainActivity.kt`
- `app/src/main/java/com/texteditor/SyntaxHighlighter.kt`
- `app/src/main/java/com/texteditor/FileType.kt`
- `app/src/main/res/layout/activity_main.xml`
- `app/build.gradle.kts`
- `.github/workflows/android-apk.yml`
- `README.md`

## Validation expectations
- For code changes, run at least `./gradlew assembleDebug` when environment allows.
- If local build is blocked by environment (e.g., missing Android SDK/JDK mismatch), report exact blocker and keep CI path intact.
- Update `README.md` when commands, outputs, or workflow behavior changes.
