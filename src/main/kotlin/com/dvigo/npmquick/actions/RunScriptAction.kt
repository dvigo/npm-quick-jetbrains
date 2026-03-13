package com.dvigo.npmquick.actions

import com.dvigo.npmquick.services.ScriptService
import com.dvigo.npmquick.services.ExecutionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.dvigo.npmquick.model.ScriptDefinition
import com.dvigo.npmquick.NpmQuickBundle

class RunScriptAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val scripts = ScriptService.findScripts(project)

        if (scripts.isEmpty()) {
            Messages.showWarningDialog(
                project, 
                NpmQuickBundle.message("warning.no.scripts.message"), 
                NpmQuickBundle.message("warning.no.scripts.title")
            )
            return
        }

        val step = object : BaseListPopupStep<ScriptDefinition>(NpmQuickBundle.message("popup.select.script.title"), scripts) {
            override fun getTextFor(value: ScriptDefinition): String {
                return "${value.type.label}: ${value.name} (${value.command})"
            }

            override fun onChosen(selectedValue: ScriptDefinition?, finalChoice: Boolean): PopupStep<*>? {
                if (finalChoice && selectedValue != null) {
                    runScript(project, selectedValue)
                }
                return FINAL_CHOICE
            }
            
            // Enable speed search by script name
            override fun isSpeedSearchEnabled(): Boolean {
                return true
            }
        }

        JBPopupFactory.getInstance().createListPopup(step).showCenteredInCurrentWindow(project)
    }

    private fun runScript(project: Project, script: ScriptDefinition) {
        ExecutionManager.runScript(project, script)
    }
}
