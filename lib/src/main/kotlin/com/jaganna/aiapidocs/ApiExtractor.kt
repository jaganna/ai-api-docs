package com.jaganna.aiapidocs

import java.io.File
import java.net.URLClassLoader
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Extracts public API information from JAR files or project classpaths.
 * Uses reflection to list packages, classes, methods, and fields.
 *
 * Optionally outputs the result as a Markdown or JSON summary for LLM context.
 */
object ApiExtractor {

    data class ApiClass(
        val name: String,
        val methods: List<ApiMethod>,
        val fields: List<ApiField>
    )

    data class ApiMethod(
        val name: String,
        val returnType: String,
        val parameters: List<String>
    )

    data class ApiField(
        val name: String,
        val type: String
    )

    /**
     * Scans a JAR file or directory for classes and extracts their public API.
     * @param jarPath Path to the JAR or directory to scan.
     * @param packagePrefix Optional package filter (e.g. "com.mycompany.lib")
     * @return List of discovered API classes.
     */
    fun extractApi(
        jarPath: String,
        packagePrefix: String? = null
    ): List<ApiClass> {
        val jarFile = File(jarPath)
        require(jarFile.exists()) { "JAR or directory not found: $jarPath" }

        val urls = arrayOf(jarFile.toURI().toURL())
        val classLoader = URLClassLoader.newInstance(urls, ApiExtractor::class.java.classLoader)
        val classNames = collectClassNames(jarFile)

        val results = mutableListOf<ApiClass>()

        for (className in classNames) {
            if (packagePrefix != null && !className.startsWith(packagePrefix)) continue

            try {
                val clazz = classLoader.loadClass(className)
                if (clazz.isSynthetic || clazz.isAnonymousClass) continue

                val methods = clazz.methods
                    .filter { Modifier.isPublic(it.modifiers) }
                    .map { method ->
                        ApiMethod(
                            name = method.name,
                            returnType = method.returnType.simpleName,
                            parameters = method.parameterTypes.map { it.simpleName }
                        )
                    }

                val fields = clazz.fields
                    .filter { Modifier.isPublic(it.modifiers) }
                    .map { field ->
                        ApiField(name = field.name, type = field.type.simpleName)
                    }

                results.add(ApiClass(clazz.name, methods, fields))

            } catch (_: Throwable) {
                // Skip classes that fail to load
            }
        }

        return results
    }

    /**
     * Collects fully qualified class names from a JAR or compiled classes directory.
     */
    private fun collectClassNames(source: File): List<String> {
        val names = mutableListOf<String>()

        if (source.isDirectory) {
            source.walkTopDown()
                .filter { it.isFile && it.extension == "class" }
                .forEach {
                    val relative = it.relativeTo(source)
                        .path
                        .removeSuffix(".class")
                        .replace(File.separatorChar, '.')
                    names.add(relative)
                }
        } else {
            java.util.jar.JarFile(source).use { jar ->
                jar.entries().asSequence()
                    .filter { !it.isDirectory && it.name.endsWith(".class") }
                    .forEach {
                        val className = it.name.removeSuffix(".class").replace('/', '.')
                        names.add(className)
                    }
            }
        }
        return names
    }

    /**
     * Writes the extracted API summary to a Markdown file for LLM context.
     */
    fun writeMarkdownSummary(classes: List<ApiClass>, outputPath: String) {
        val builder = StringBuilder("# API Summary\n\n")
        for (cls in classes) {
            builder.append("## ${cls.name}\n\n")

            if (cls.fields.isNotEmpty()) {
                builder.append("### Fields\n")
                for (field in cls.fields) {
                    builder.append("- `${field.type} ${field.name}`\n")
                }
                builder.append("\n")
            }

            if (cls.methods.isNotEmpty()) {
                builder.append("### Methods\n")
                for (method in cls.methods) {
                    val params = method.parameters.joinToString(", ")
                    builder.append("- `${method.returnType} ${method.name}($params)`\n")
                }
                builder.append("\n")
            }
        }

        Files.createDirectories(Paths.get(File(outputPath).parent ?: "."))
        Files.writeString(Paths.get(outputPath), builder.toString())
    }

    /**
     * Convenience method: extract + write summary in one call.
     */
    fun generateMarkdown(jarPath: String, packagePrefix: String? = null, outputFile: String = "api_summary.md") {
        val api = extractApi(jarPath, packagePrefix)
        writeMarkdownSummary(api, outputFile)
    }
}
