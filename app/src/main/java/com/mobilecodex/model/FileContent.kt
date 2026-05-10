package com.mobilecodex.model

/**
 * 文件内容
 */
data class FileContent(
    val path: String,
    val name: String,
    val content: String,
    val encoding: String = "utf-8",
    val size: Long = 0,
    val sha: String? = null,
    val url: String? = null,
    val isModified: Boolean = false,
    val language: String = inferLanguage(name)
) {
    companion object {
        fun inferLanguage(fileName: String): String {
            return when (fileName.substringAfterLast(".", "").lowercase()) {
                "kt" -> "Kotlin"
                "java" -> "Java"
                "py" -> "Python"
                "js" -> "JavaScript"
                "ts" -> "TypeScript"
                "go" -> "Go"
                "rs" -> "Rust"
                "swift" -> "Swift"
                "c", "h" -> "C"
                "cpp", "hpp", "cc", "cxx" -> "C++"
                "cs" -> "C#"
                "rb" -> "Ruby"
                "php" -> "PHP"
                "html" -> "HTML"
                "css" -> "CSS"
                "scss", "sass" -> "SCSS"
                "xml" -> "XML"
                "json" -> "JSON"
                "yaml", "yml" -> "YAML"
                "md", "mdx" -> "Markdown"
                "sql" -> "SQL"
                "sh", "bash" -> "Shell"
                "gradle", "gradle.kts" -> "Gradle"
                else -> "Plain Text"
            }
        }
    }
}
