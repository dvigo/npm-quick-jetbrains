# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2024-05-22

### Changed
- 🛠 **Major Update**: Migrated to IntelliJ Platform Gradle Plugin 2.0.
- 🔧 **Compatibility**: Full support for IntelliJ 2024.1+ and future versions.
- 🐛 **Fixes**: Resolved binary incompatibility issues with JSON module.

## [1.0.3] - 2024-05-22

### Changed
- 🔧 **Compatibility**: Fixed version range to support all IDE versions from 2023.2 onwards (232.0+).

## [1.0.2] - 2024-05-22

### Changed
- 🔧 **Compatibility**: Removed upper version limit to support all future IDE versions (2024.x and beyond).

## [1.0.1] - 2024-05-22

### Changed
- 🎨 **Updated Icon**: New plugin icon for better visibility in the Marketplace.

## [1.0.0] - 2024-05-22

### Added
- 🚀 **Initial Release**: First public version of npm quick.
- ✨ **Smart Discovery**: Automatically detects `npm`, `pnpm`, and `yarn` scripts from `package.json`.
- ⌨️ **Quick Access**: Searchable popup via `Cmd + Option + N` (macOS) or `Ctrl + Alt + N` (Windows/Linux).
- 🖥 **Interactive Console**: Run scripts, view logs, and interact with processes directly in the IDE.
- 📜 **Execution History**: Dedicated Tool Window to track script runs.
- 🛑 **Process Control**: Stop running scripts, clear logs, or remove specific entries.
- 🌍 **i18n**: Full support for English and Spanish languages.
