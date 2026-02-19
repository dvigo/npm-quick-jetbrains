package com.dvigo.npmquick.model

enum class PackageManager(val command: String) {
    NPM("npm"),
    PNPM("pnpm"),
    YARN("yarn");

    fun getRunCommand(scriptName: String): String {
        return when (this) {
            YARN -> "yarn $scriptName"
            else -> "${this.command} run $scriptName"
        }
    }
}

enum class ScriptType(val label: String, val icon: String) {
    TEST("Test", "testing-passed-icon"),
    BUILD("Build", "settings-gear"),
    DEV("Dev", "run"),
    LINT("Lint", "checklist"),
    FORMAT("Format", "edit"),
    DEPLOY("Deploy", "cloud-upload"),
    DOCS("Docs", "book"),
    OTHER("Run", "play");

    companion object {
        fun detect(name: String): ScriptType {
            val lower = name.lowercase()
            return when {
                lower.contains("test") || lower.contains("spec") || lower.contains("jest") || lower.contains("vitest") -> TEST
                lower.contains("build") || lower.contains("bundle") || lower.contains("pack") || lower.contains("compile") -> BUILD
                lower.contains("dev") || lower.contains("start") || lower.contains("serve") || lower == "dev:server" -> DEV
                lower.contains("lint") || lower.contains("eslint") || (lower.contains("prettier") && lower.contains("check")) -> LINT
                lower.contains("format") || (lower.contains("prettier") && !lower.contains("check")) -> FORMAT
                lower.contains("deploy") || lower.contains("publish") || lower.contains("release") -> DEPLOY
                lower.contains("doc") || lower.contains("docs") || lower.contains("jsdoc") -> DOCS
                else -> OTHER
            }
        }
    }
}

data class ScriptDefinition(
    val name: String,
    val command: String,
    val type: ScriptType
)
