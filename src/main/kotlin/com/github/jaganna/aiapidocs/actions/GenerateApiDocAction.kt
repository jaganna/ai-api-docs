package com.github.jaganna.aiapidocs.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.jaganna.aiapidocs.ApiExtractor
import java.nio.file.Files
import java.nio.file.Paths

class GenerateApiDocAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getProject()
        val file = e.getData<VirtualFile?>(CommonDataKeys.VIRTUAL_FILE)

        if (file == null || project == null) return

        if (!file.name.endsWith(".jar")) {
            Messages.showInfoMessage("Please select a JAR or dependency.", "Invalid Selection")
            return
        }

        val jarPath = file.getPath()
        val outputPath = Paths.get(project.getBasePath(), "docs", "${file.name}_api_summary.md").toString()

        try {
            Files.createDirectories(Paths.get(project.getBasePath(), "docs"))
            ApiExtractor.generateMarkdown(
                jarPath = jarPath,
                packagePrefix = null,
                outputFile = outputPath
            )

            Messages.showInfoMessage("Generated API summary at: $outputPath", "Success")
        } catch (ex: Exception) {
            Messages.showErrorDialog(project, "Error: " + ex.message, "Generation Failed")
        }
    }
}