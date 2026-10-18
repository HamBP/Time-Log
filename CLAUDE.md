# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Time Log is a single-module Android app (`:app`) for tracking time spent on user-defined activities with a stopwatch, then reviewing daily efficiency history. Package root: `me.algosketch.timelog`. UI is 100% Jetpack Compose (Material3). Minimum SDK 30.

## Build & test commands

Use the Gradle wrapper (`./gradlew` in Git Bash, or `gradlew.bat` in PowerShell/cmd):

- Build debug APK: `./gradlew assembleDebug`
- Install on a connected device/emulator: `./gradlew installDebug`
- Run JVM unit tests: `./gradlew test` (debug variant: `testDebugUnitTest`)
- Run a single unit test: `./gradlew test --tests "me.algosketch.timelog.ExampleUnitTest"`
- Run instrumented tests (needs a device/emulator): `./gradlew connectedAndroidTest`
- Lint: `./gradlew lint`

There is currently no real test suite — only the generated `ExampleUnitTest` / `ExampleInstrumentedTest` placeholders.

Dependencies and versions are centralized in `gradle/libs.versions.toml` (version catalog); add libraries there and reference them via `libs.*` in `app/build.gradle.kts`, not as inline coordinates.

## Architecture

**MVVM + unidirectional state.** Each feature under `ui/feature/<name>/` has three parts:
- `<Name>ViewModel` — `@HiltViewModel`, holds a `private val _uiState = MutableStateFlow(...)` exposed as a read-only `StateFlow`. State is mutated only via `_uiState.update { }`.
- `<Name>UiState` — immutable data class describing everything the screen renders (often with derived `val`s computed in the constructor body, e.g. `HistoryUiState.monthlyEfficiency`).
- `<Name>Screen` — `@Composable` that collects the state and is otherwise stateless.

**Dependency injection: Hilt.** `TimeLogApplication` is `@HiltAndroidApp`; `MainActivity` is `@AndroidEntryPoint`. `di/DatabaseModule` provides the Room database and DAOs as singletons. ViewModels receive `@ApplicationContext Context` (used to resolve string resources for locale-aware formatting) and `LogRepository`.

**Navigation: AndroidX Navigation3** (not the older Compose Navigation). Destinations are `@Serializable data object`s implementing `NavKey` in `ui/navigation/AppNavigation.kt`. Navigation is a `rememberNavBackStack` driven by a custom bottom bar; tab switches `clear()` then `add()` the destination (single-level tabs, no deep back stack).

**Data layer: Room (`data/local/`).**
- `AppDatabase` (currently `version = 4`) exposes `LogTypeDao` and `LogSessionDao`. `LogRepository` is the single `@Singleton` access point — UI/ViewModels never touch DAOs directly.
- Two entities: `LogTypeEntity` (an activity category: name, colorHex, icon, `includeEfficiency` flag) and `LogSessionEntity` (a tracked interval: `typeId`, `startedAt`, `endedAt`).
- `LocalDateTime` is persisted as **epoch seconds** via `Converters` (system default zone). SQL that does date math relies on this — see `getDailyAggregates()` using `date(s.startedAt, 'unixepoch', 'localtime')` and `endedAt - startedAt` for durations. Keep the stored unit in seconds.
- History totals are computed in SQL via the `DailyAggregate` projection (GROUP BY day), not in Kotlin.
- The DB uses `fallbackToDestructiveMigration()` and seeds two default log types on first open via a `RoomDatabase.Callback`. There is an open TODO to replace destructive migration before release; bumping schema currently wipes data.

**String-keyed colors and icons.** `LogTypeEntity` stores `colorHex` (e.g. `"#4ADE80"`) and `icon` (e.g. `"play_arrow"`) as plain strings. Convert at the UI boundary with `String.toComposeColor()` (`ui/theme/ColorExt`) and `String.toMaterialIcon()` (`ui/util/IconMapper`). When adding a new selectable icon, add a `when` branch in `IconMapper`.

## Internationalization

The app is fully localized. **Korean is the base locale** living in `res/values/strings.xml` (there is intentionally no `values-ko/`). Translations are in `values-en`, `values-ja`, `values-zh`, `values-es`.

- All user-facing text must be a string resource referenced via `stringResource(R.string.…)` in Composables, or `context.getString(R.string.…)` in ViewModels. Do not hardcode display strings.
- Date/time formatting is locale-driven: format patterns themselves are resources (`date_format_pattern`, `time_format_pattern`) fed to `DateTimeFormatter.ofPattern(pattern, Locale.getDefault())`. Durations use plural-style resources (`duration_hours_and_minutes`, `duration_minutes_only`) with positional args.
- When adding a string, add it to `values/strings.xml` **and** all four translation files, keeping placeholder args (`%1$d`, etc.) consistent across every locale.

## 지시 사항

- 지시 사항에 대한 의도 혹은 누락된 정보로 인해 의문이 있을 경우, 이를 임의 구현하지 않고 질문해도 된다.
- 대답은 한국어로.
- 코드 변경 후 빌드(`./gradlew assembleDebug`)에 성공하면 별도 요청 없이 커밋한다. 실패하면 커밋하지 않고 보고한다. `main`에 있으면 작업 브랜치를 새로 만든 뒤 커밋한다.
- 커밋은 하나로 몰지 말고 리뷰하기 좋은 단위로 나눈다.
- 이슈 수정을 지시받으면 `main`에서 `feat/<이슈번호>` 브랜치를 만들고, 구현이 끝나면 push 후 PR(base `main`)까지 올린다. 이슈가 아닌 작업은 push·PR을 자동으로 하지 않는다.
- 프롬프트마다 docs 수정이 필요한지 판단해 필요하면 수정한다.

## 프로젝트 문서 (docs/)

작업을 시작하기 전에 아래 두 문서를 먼저 읽고, 작업이 끝나면 직접 갱신한다. 별도 요청이 없어도 갱신한다.

- `docs/prompt-guidelines.md` — 작업 규칙과 지시 기록. 새 지시·규칙·결정이 생기면 "지시 기록" 표에 날짜와 함께 추가하고, 계속 적용될 규칙이면 "상시 적용 규칙"에도 반영한다.
- `docs/features.md` — 기능 트리, 엣지 케이스 동작, 미구현 기능. 기능을 추가·변경·제거하면 트리와 관련 엣지 케이스(자정, 백그라운드, 시계 변경 등)를 함께 고치고, 상단의 마지막 업데이트 날짜를 바꾼다.

문서 갱신은 해당 작업과 같은 커밋/PR에 포함한다.