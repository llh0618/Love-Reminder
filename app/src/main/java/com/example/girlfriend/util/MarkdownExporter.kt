package com.example.girlfriend.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.girlfriend.data.AppDatabase
import kotlinx.coroutines.runBlocking
import java.io.File

object MarkdownExporter {

    fun exportAndShare(context: Context) {
        val md = runBlocking {
            val notes = AppDatabase.getInstance(context).noteDao().getAll()
            buildMarkdown(notes)
        }
        val file = File(context.cacheDir, "她的喜好手册.md")
        file.writeText(md, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/markdown"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "导出喜好手册"))
    }

    private fun buildMarkdown(notes: List<com.example.girlfriend.data.entity.Note>): String {
        val likes = notes.filter { it.category == "like" }
        val dislikes = notes.filter { it.category == "dislike" }

        // 按 tags 分组
        val tagGroups = linkedMapOf(
            "美食" to mutableListOf<String>(),
            "饮品" to mutableListOf<String>(),
            "颜色" to mutableListOf<String>(),
            "品牌" to mutableListOf<String>(),
            "爱好" to mutableListOf<String>(),
            "其他" to mutableListOf<String>()
        )

        fun groupNotes(list: List<com.example.girlfriend.data.entity.Note>): Map<String, List<String>> {
            val grouped = mutableMapOf<String, MutableList<String>>()
            for (note in list) {
                val tag = if (note.tags.isBlank()) "其他" else note.tags.split("，").first()
                grouped.getOrPut(tag) { mutableListOf() }.add(note.content)
            }
            return grouped
        }

        val likeGrouped = groupNotes(likes)
        val dislikeGrouped = groupNotes(dislikes)

        return buildString {
            appendLine("# 她的喜好手册")
            appendLine()
            appendLine("## ❤️ 喜欢")
            appendLine()
            for ((tag, items) in likeGrouped) {
                appendLine("### $tag")
                for (item in items) {
                    appendLine("- $item")
                }
                appendLine()
            }
            if (likes.isEmpty()) appendLine("_还没记录_")
            appendLine()

            appendLine("## 💔 讨厌")
            appendLine()
            for ((tag, items) in dislikeGrouped) {
                appendLine("### $tag")
                for (item in items) {
                    appendLine("- $item")
                }
                appendLine()
            }
            if (dislikes.isEmpty()) appendLine("_还没记录_")
            appendLine()

            appendLine("> 由「女友助手」App 生成")
        }
    }
}
