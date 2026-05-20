package com.example.girlfriend.ui.noteedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.girlfriend.MainActivity
import com.example.girlfriend.R
import com.example.girlfriend.data.entity.Note
import com.example.girlfriend.ui.dislikes.DislikesFragment
import com.example.girlfriend.ui.likes.LikesFragment
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText

class NoteEditFragment : Fragment() {

    private var editId: String? = null
    private var category = "like"
    private var selectedTags = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_note_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments
        category = args?.getString("category") ?: "like"

        val etContent = view.findViewById<TextInputEditText>(R.id.et_content)
        val etTags = view.findViewById<TextInputEditText>(R.id.et_tags)

        view.findViewById<android.widget.TextView>(R.id.tv_title).text =
            if (category == "like") "添加喜欢" else "添加讨厌"

        // 回填
        if (args?.getString("id") != null) {
            editId = args.getString("id")
            etContent.setText(args.getString("content"))
            etTags.setText(args.getString("tags"))
            args.getString("tags")?.split("，")?.forEach { selectedTags.add(it) }
            view.findViewById<View>(R.id.btn_delete).visibility = View.VISIBLE
        }

        val tagChips = listOf(
            view.findViewById<Chip>(R.id.chip_food) to "美食",
            view.findViewById<Chip>(R.id.chip_drink) to "饮品",
            view.findViewById<Chip>(R.id.chip_color) to "颜色",
            view.findViewById<Chip>(R.id.chip_brand) to "品牌",
            view.findViewById<Chip>(R.id.chip_hobby) to "爱好"
        )
        tagChips.forEach { (chip, tag) ->
            chip.isChecked = tag in selectedTags
            chip.setOnClickListener { if (chip.isChecked) selectedTags.add(tag) else selectedTags.remove(tag) }
        }

        view.findViewById<View>(R.id.tv_back).setOnClickListener {
            goBack()
        }

        view.findViewById<View>(R.id.tv_save).setOnClickListener {
            val content = etContent.text.toString().trim()
            if (content.isEmpty()) return@setOnClickListener

            val customTags = etTags.text.toString().trim()
            val allTags = (selectedTags + (if (customTags.isNotBlank()) listOf(customTags) else emptyList()))
                .joinToString("，")

            val note = Note(
                id = editId ?: java.util.UUID.randomUUID().toString(),
                content = content,
                category = category,
                tags = allTags
            )

            val vm = ViewModelProvider(this)[NoteViewModel::class.java]
            vm.save(note)
            goBack()
        }

        view.findViewById<View>(R.id.btn_delete).setOnClickListener {
            if (editId != null) {
                val vm = ViewModelProvider(this)[NoteViewModel::class.java]
                vm.delete(Note(id = editId!!, content = "", category = category))
                goBack()
            }
        }
    }

    private fun goBack() {
        val f = if (category == "like") LikesFragment() else DislikesFragment()
        (activity as MainActivity).switchToFragment(f)
    }
}
