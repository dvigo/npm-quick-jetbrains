package com.dvigo.npmquick.services

import com.dvigo.npmquick.model.PackageManager
import com.dvigo.npmquick.model.ScriptDefinition
import com.dvigo.npmquick.model.ScriptType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonFile

object ScriptService {
    private val LOG = Logger.getInstance(ScriptService::class.java)

    fun findScripts(project: Project): List<ScriptDefinition> {
        return ApplicationManager.getApplication().runReadAction<List<ScriptDefinition>> {
            val packageJson = findPackageJson(project)
            if (packageJson == null) {
                LOG.warn("package.json not found in project root: ${project.basePath}")
                return@runReadAction emptyList()
            }
            
            val psiFile = PsiManager.getInstance(project).findFile(packageJson) as? JsonFile
            if (psiFile == null) {
                LOG.warn("Could not parse package.json as JsonFile")
                return@runReadAction emptyList()
            }

            val rootObject = psiFile.topLevelValue as? JsonObject
            if (rootObject == null) {
                LOG.warn("package.json root is not a JsonObject")
                return@runReadAction emptyList()
            }

            val scriptsObject = rootObject.findProperty("scripts")?.value as? JsonObject
            if (scriptsObject == null) {
                LOG.info("No 'scripts' property found in package.json")
                return@runReadAction emptyList()
            }

            scriptsObject.propertyList.mapNotNull { property ->
                val name = property.name
                val command = (property.value?.text ?: "").removeSurrounding("\"")
                if (name.isNotEmpty()) {
                    ScriptDefinition(name, command, ScriptType.detect(name))
                } else null
            }
        }
    }

    fun detectPackageManager(project: Project): PackageManager {
        val baseDir = project.basePath?.let { path -> 
            com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(path)
        } ?: return PackageManager.NPM
        
        if (baseDir.findChild("pnpm-lock.yaml") != null) return PackageManager.PNPM
        if (baseDir.findChild("yarn.lock") != null) return PackageManager.YARN
        if (baseDir.findChild("package-lock.json") != null) return PackageManager.NPM
        
        return PackageManager.NPM
    }

    private fun findPackageJson(project: Project): VirtualFile? {
        return project.basePath?.let { path ->
            com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(path)?.findChild("package.json")
        }
    }
}
