package com.example.girlfriend.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.girlfriend.data.AppDatabase
import kotlinx.coroutines.runBlocking
import java.io.File

object MarkdownExporter {

    private fun shareFile(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/markdown"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }

    fun exportNotes(context: Context) {
        val md = runBlocking {
            val notes = AppDatabase.getInstance(context).noteDao().getAll()
            buildNotesMarkdown(notes)
        }
        val file = File(context.cacheDir, "她的喜好手册.md")
        file.writeText(md, Charsets.UTF_8)
        shareFile(context, file, "导出喜好手册")
    }

    fun exportGifts(context: Context) {
        val md = runBlocking {
            val gifts = AppDatabase.getInstance(context).giftDao().getAllList()
            buildGiftsMarkdown(gifts)
        }
        val file = File(context.cacheDir, "礼物清单.md")
        file.writeText(md, Charsets.UTF_8)
        shareFile(context, file, "导出礼物清单")
    }

    private fun buildGiftsMarkdown(gifts: List<com.example.girlfriend.data.entity.Gift>): String {
        val want = gifts.filter { it.status == "want" }
        val bought = gifts.filter { it.status == "bought" }

        return buildString {
            appendLine("# 🎁 礼物清单")
            appendLine()
            appendLine("## 想送")
            appendLine()
            if (want.isEmpty()) {
                appendLine("_暂无_")
            } else {
                for (g in want) {
                    append("- **${g.name}**")
                    if (g.link.isNotBlank()) append("  [🔗](${g.link})")
                    if (g.note.isNotBlank()) append(" — ${g.note}")
                    appendLine()
                }
            }
            appendLine()
            appendLine("## 已送")
            appendLine()
            if (bought.isEmpty()) {
                appendLine("_暂无_")
            } else {
                for (g in bought) {
                    append("- ~~${g.name}~~")
                    if (g.note.isNotBlank()) append(" — ${g.note}")
                    appendLine()
                }
            }
            appendLine()
            appendLine("> 由「恋爱提醒」App 生成")
        }
    }

    private fun buildNotesMarkdown(notes: List<com.example.girlfriend.data.entity.Note>): String {
        val likes = notes.filter { it.category == "like" }
        val dislikes = notes.filter { it.category == "dislike" }

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
                for (item in items) appendLine("- $item")
                appendLine()
            }
            if (likes.isEmpty()) appendLine("_还没记录_")
            appendLine()

            appendLine("## 💔 讨厌")
            appendLine()
            for ((tag, items) in dislikeGrouped) {
                appendLine("### $tag")
                for (item in items) appendLine("- $item")
                appendLine()
            }
            if (dislikes.isEmpty()) appendLine("_还没记录_")
            appendLine()

            appendLine("> 由「恋爱提醒」App 生成")
        }
    }
}
