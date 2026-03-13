# npm quick for IntelliJ

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-2.1.0-blue)

**npm quick** is a lightweight and powerful IntelliJ IDEA (and Android Studio) plugin that allows you to discover and run `npm`, `pnpm`, and `yarn` scripts instantly from a searchable popup, without leaving your code.

## ✨ Features

*   **🚀 Instant Access:** Press `Cmd + Option + N` (macOS) or `Ctrl + Alt + N` (Windows/Linux) to open a searchable list of all scripts found in your `package.json`.
*   **🔍 Speed Search:** Just start typing when the popup opens to filter scripts instantly.
*   **⚡️ Smart Detection:** Automatically detects `npm`, `pnpm`, or `yarn` based on your lock files.
*   **📜 Execution History:** Keeps a history of executed scripts in a dedicated Tool Window.
*   **👀 Interactive Console:** View logs and interact with running processes (supports input).
*   **🛑 Process Control:** Stop running scripts directly from the toolbar.
*   **🧹 Management:** Clear specific logs or the entire history to keep your workspace clean.
*   **🌍 Internationalization:** Available in English and Spanish.

## 🛠 Installation

1.  Open **Settings/Preferences** > **Plugins** > **Marketplace**.
2.  Search for **"npm quick"**.
3.  Click **Install** and restart the IDE.

## 📖 Usage

### Running a Script
1.  Open any project with a `package.json` file.
2.  Press the shortcut:
    *   **macOS:** `Cmd + Option + N`
    *   **Windows/Linux:** `Ctrl + Alt + N` (Default mapping)
3.  **Type to filter** or use arrow keys to select the script you want to run.
4.  Press **Enter** to execute.

### Managing Executions
The **npm quick** Tool Window will open automatically at the bottom of the IDE.
*   **Left Panel:** List of executed scripts with status icons (Running ▶️, Success ✅, Failed ❌).
*   **Right Panel:** Console output for the selected script.
*   **Toolbar Actions:**
    *   ⏹ **Stop Process:** Kills the currently running script.
    *   🗑 **Remove Selected:** Removes the selected log entry.
    *   🧹 **Clear All:** Clears the entire execution history.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the Apache 2.0 License.
