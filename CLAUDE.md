# CLAUDE.md

## Project Overview

**easy-cmd** (Command-GUI) is a Fabric Minecraft mod (Java 21) that provides a GUI for executing commands, managing fake players, and using a placeholder system. Targets Minecraft 1.21.11.

- Mod ID: `command-gui`
- Version: `0.1.0-beta.5`
- License: GPL-3.0

## Build & Run

```bash
# Build the mod
./gradlew build
# Output: build/libs/command-gui-<version>.jar

# Run Minecraft client in dev environment
./gradlew runClient
```

> **Dev setup notes:**
> - `runClient` enables `DevAuth-fabric` (`-Ddevauth.enabled=true`), so a real Minecraft account (or configured DevAuth) is required.
> - ModMenu is `modCompileOnly` — to test ModMenu integration locally, add it as `modLocalRuntime` in your own `build.gradle`.

## Project Structure

```
src/
  main/java/com/remrin/          # Server-side entry point (CommandGUI.java)
  client/java/com/remrin/client/ # CommandGUIClient.java, ModMenuIntegration.java
    config/                      # CommandConfig, PresetConfig, SettingsConfig
    gui/                         # All GUI screens and helpers
  main/resources/                # fabric.mod.json, lang files, presets, textures
  client/resources/              # Client mixin config
docs/                            # Bilingual documentation (10 .md files)
```

## Architecture

- **Entry points**: `CommandGUI.java` (server, minimal), `CommandGUIClient.java` (client, keybinds/ticks)
- **GUI base**: All screens extend `BaseParentedScreen<P>` or Minecraft's `Screen`
- **Main screen**: `CommandGUIScreen` → tabs: `CustomCommandTab`, `FakePlayerTab`, `PresetCommandTab`
- **Placeholder system**: `ChainedCommandExecutor` resolves 8 placeholder types — `{player}`, `{player_all}`, `{player_fake}`, `{name}`, `{number}`, `{time}`, `{coords}`, `{x}` — via sequential input screens (`{y}` and `{z}` are resolved alongside `{x}` as a single COORDS input)
- **Config**: JSON files via Gson under `config/command-gui/`

## Code Conventions

- Java 21, no external libraries beyond Fabric API and Minecraft
- Tab indentation
- Standard Java naming (PascalCase classes, camelCase methods)
- JavaDoc comments on all classes
- Bilingual UI strings (English + Simplified Chinese via `lang/` files)

## Commit Convention

Uses conventional commits (configured in `cliff.toml`):
- `feat:` — new features
- `fix:` — bug fixes
- `perf:` — performance improvements
- `refactor:` — code refactoring
- `docs:` — documentation
- `chore:` — build/maintenance
