<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# DockDockBuild Changelog

## [2.4.0]
### Added
- Unit tests and UI tests (Remote Robot)
- `checkConfiguration()` validates required fields before launch

### Changed
- Migrate to IntelliJ Platform Gradle Plugin 2.x
- Target IntelliJ 2025.1+
- Upgrade Kotlin to 2.1.20, Gradle to 9.4.1
- Port CmdProcessBuilder and Parameters from Java to Kotlin
- `isDockerImage`/`isDockerfile` stored as Boolean instead of String
- Docker path detection probes Rancher Desktop and Homebrew locations
- Parameters file written to a unique temp file per run (fixes concurrent build corruption)

### Fixed
- Process not killed on timeout (now calls `destroyForcibly()` before exit)
- `DockDockBuild.jar` location uses `PluginManagerCore` instead of classloader cast (fixes IDE crash on startup)
- Wrong `Main-Class` manifest attribute
- Project service `DockDockBuildProjectSettings` was not registered in `plugin.xml`

## [2.2.1]
### Fixed
- getId() Intellij warning
### Changed
- Support for IntelliJ 2023.1

## [2.2.0]
### Changed
- Update Java to v17
- Update Gradle to v7.5

### Added
- Support for IntelliJ 2022.2


## [2.1.1]
### Changed
- Update Gradle to v7.4

### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
