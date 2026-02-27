# TextEditor

안드로이드용 텍스트 에디터 앱입니다. `txt`, `json`, `xml` 파일을 열고 편집/저장할 수 있으며, `json`/`xml` 문법 하이라이트를 제공합니다.

## 기능

- 파일 열기: Storage Access Framework로 문서 선택
- 파일 저장: 기존 URI 저장 또는 새 문서로 저장
- 파일 타입 선택: TXT / JSON / XML
- 문법 스타일 표시:
	- JSON: key, string, number, boolean/null
	- XML: tag, attribute name, attribute value, comment

## 로컬 빌드

요구사항:
- JDK 17
- Android SDK (환경변수 `ANDROID_SDK_ROOT` 설정)

명령:

```bash
./gradlew assembleDebug
```

생성 APK:

`app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions APK 빌드

워크플로 파일: `.github/workflows/android-apk.yml`

- `push`/`pull_request`(`main`) 및 `workflow_dispatch`에서 동작
- Debug APK 빌드 후 Artifact로 업로드