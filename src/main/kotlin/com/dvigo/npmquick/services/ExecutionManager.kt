package com.dvigo.npmquick.services

import com.dvigo.npmquick.model.ScriptDefinition
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.application.ApplicationManager
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

data class ExecutionHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val script: ScriptDefinition,
    val command: String,
    var status: ScriptStatus = ScriptStatus.RUNNING,
    val startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime? = null,
    val output: StringBuilder = StringBuilder()
) {
    enum class ScriptStatus { RUNNING, SUCCESS, FAILED }
}

object ExecutionManager {
    private val history = CopyOnWriteArrayList<ExecutionHistoryEntry>()
    private val activeProcesses = mutableMapOf<String, OSProcessHandler>()
    private val listeners = mutableListOf<() -> Unit>()

    fun getHistory() = history.toList()

    fun getProcessHandler(entryId: String): OSProcessHandler? {
        return activeProcesses[entryId]
    }

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun clearHistory() {
        // Stop all running processes before clearing
        activeProcesses.values.forEach { it.destroyProcess() }
        activeProcesses.clear()
        history.clear()
        notifyListeners()
    }

    fun removeEntry(entry: ExecutionHistoryEntry) {
        // Stop process if running
        if (entry.status == ExecutionHistoryEntry.ScriptStatus.RUNNING) {
            stopProcess(entry.id)
        }
        history.remove(entry)
        notifyListeners()
    }

    fun runScript(project: Project, script: ScriptDefinition) {
        // Activate Tool Window
        ApplicationManager.getApplication().invokeLater {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("npm quick")
            toolWindow?.activate(null)
        }

        val packageManager = ScriptService.detectPackageManager(project)
        val commandString = packageManager.getRunCommand(script.name)
        val entry = ExecutionHistoryEntry(script = script, command = commandString)
        
        history.add(0, entry)
        notifyListeners()

        try {
            val commandParts = commandString.split(" ")
            val commandLine = GeneralCommandLine(commandParts)
                .withWorkDirectory(project.basePath)
                .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            
            val handler = OSProcessHandler(commandLine)
            activeProcesses[entry.id] = handler

            handler.addProcessListener(object : ProcessAdapter() {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    entry.output.append(event.text)
                    notifyListeners()
                }

                override fun processTerminated(event: ProcessEvent) {
                    entry.status = if (event.exitCode == 0) ExecutionHistoryEntry.ScriptStatus.SUCCESS else ExecutionHistoryEntry.ScriptStatus.FAILED
                    entry.endTime = LocalDateTime.now()
                    activeProcesses.remove(entry.id)
                    notifyListeners()
                }
            })

            handler.startNotify()
        } catch (e: Exception) {
            entry.status = ExecutionHistoryEntry.ScriptStatus.FAILED
            entry.output.append("\nError starting process: ${e.message}")
            notifyListeners()
        }
    }

    fun stopProcess(id: String) {
        activeProcesses[id]?.destroyProcess()
        activeProcesses.remove(id)
    }

    private fun notifyListeners() {
        listeners.forEach { it() }
    }
}
