package com.dvigo.npmquick.ui

import com.dvigo.npmquick.NpmQuickBundle
import com.dvigo.npmquick.services.ExecutionHistoryEntry
import com.dvigo.npmquick.services.ExecutionManager
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.ListSelectionModel

class NpmQuickToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = NpmQuickPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class NpmQuickPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val listModel = DefaultListModel<ExecutionHistoryEntry>()
    private val list = JBList(listModel)
    private val console = ConsoleViewImpl(project, false)

    init {
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.setCellRenderer(object : ColoredListCellRenderer<ExecutionHistoryEntry>() {
            override fun customizeCellRenderer(
                list: JList<out ExecutionHistoryEntry>,
                value: ExecutionHistoryEntry?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                if (value == null) return
                
                icon = when (value.status) {
                    ExecutionHistoryEntry.ScriptStatus.RUNNING -> AllIcons.Actions.Execute
                    ExecutionHistoryEntry.ScriptStatus.SUCCESS -> AllIcons.RunConfigurations.TestPassed
                    ExecutionHistoryEntry.ScriptStatus.FAILED -> AllIcons.RunConfigurations.TestFailed
                }
                
                append(value.script.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                append(" (${value.startTime.toLocalTime().toString().substringBefore('.')})", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
        })

        list.addListSelectionListener {
            val selected = list.selectedValue
            if (selected != null) {
                console.clear()
                console.print(selected.output.toString(), com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT)
                
                // Attach process handler if running to allow input
                if (selected.status == ExecutionHistoryEntry.ScriptStatus.RUNNING) {
                    val handler = ExecutionManager.getProcessHandler(selected.id)
                    if (handler != null && !handler.isProcessTerminated) {
                        console.attachToProcess(handler)
                    }
                }
            } else {
                console.clear()
            }
        }

        // Toolbar
        val actionGroup = DefaultActionGroup()
        
        // 1. Stop Process
        actionGroup.add(object : AnAction(
            NpmQuickBundle.message("action.stop.process.text"),
            NpmQuickBundle.message("action.stop.process.description"),
            AllIcons.Actions.Suspend
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                val selected = list.selectedValue
                if (selected != null && selected.status == ExecutionHistoryEntry.ScriptStatus.RUNNING) {
                    ExecutionManager.stopProcess(selected.id)
                }
            }
            
            override fun update(e: AnActionEvent) {
                val selected = list.selectedValue
                e.presentation.isEnabled = selected != null && selected.status == ExecutionHistoryEntry.ScriptStatus.RUNNING
            }
        })

        // 2. Remove Selected
        actionGroup.add(object : AnAction(
            NpmQuickBundle.message("action.remove.selected.text"),
            NpmQuickBundle.message("action.remove.selected.description"),
            AllIcons.General.Remove
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                val selected = list.selectedValue
                if (selected != null) {
                    val result = Messages.showYesNoDialog(
                        project,
                        NpmQuickBundle.message("dialog.remove.log.message", selected.script.name),
                        NpmQuickBundle.message("dialog.remove.log.title"),
                        Messages.getQuestionIcon()
                    )
                    
                    if (result == Messages.YES) {
                        ExecutionManager.removeEntry(selected)
                        if (listModel.isEmpty) {
                            console.clear()
                        }
                    }
                }
            }
            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = list.selectedValue != null
            }
        })

        // 3. Clear All
        actionGroup.add(object : AnAction(
            NpmQuickBundle.message("action.clear.all.text"),
            NpmQuickBundle.message("action.clear.all.description"),
            AllIcons.Actions.GC
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                if (listModel.isEmpty) return
                
                val result = Messages.showYesNoDialog(
                    project,
                    NpmQuickBundle.message("dialog.clear.history.message"),
                    NpmQuickBundle.message("dialog.clear.history.title"),
                    Messages.getQuestionIcon()
                )
                
                if (result == Messages.YES) {
                    ExecutionManager.clearHistory()
                    console.clear()
                }
            }
            
            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = !listModel.isEmpty
            }
        })

        val toolbar = ActionManager.getInstance().createActionToolbar("NpmQuickToolbar", actionGroup, false)
        toolbar.targetComponent = this

        val leftPanel = JPanel(BorderLayout())
        leftPanel.add(toolbar.component, BorderLayout.WEST)
        leftPanel.add(JBScrollPane(list), BorderLayout.CENTER)

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, console.component)
        splitPane.dividerLocation = 250
        add(splitPane, BorderLayout.CENTER)

        ExecutionManager.addListener {
            ApplicationManager.getApplication().invokeLater {
                updateList()
            }
        }
        updateList()
    }

    private fun updateList() {
        val currentHistory = ExecutionManager.getHistory()
        val previousSize = listModel.size()
        val selectedIndex = list.selectedIndex
        val selectedValue = list.selectedValue

        listModel.clear()
        currentHistory.forEach { listModel.addElement(it) }

        // If a new item was added (history grew), select the first one (the newest)
        if (currentHistory.size > previousSize) {
            list.selectedIndex = 0
        } else if (selectedValue != null && currentHistory.contains(selectedValue)) {
             // Maintain selection if the item still exists
             list.setSelectedValue(selectedValue, true)
        } else if (!listModel.isEmpty && list.selectedIndex == -1) {
             // Fallback: select first if nothing selected
             list.selectedIndex = 0
        }
    }
}
