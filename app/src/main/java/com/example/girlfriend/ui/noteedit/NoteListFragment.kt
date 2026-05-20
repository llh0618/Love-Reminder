package com.example.girlfriend.ui.noteedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.girlfriend.MainActivity
import com.example.girlfriend.R
import com.example.girlfriend.data.entity.Note
import com.example.girlfriend.util.MarkdownExporter

abstract class NoteListFragment : Fragment() {

    abstract val category: String
    abstract val title: String

    private val adapter = NoteAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_notes_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vm = ViewModelProvider(this)[NoteViewModel::class.java]

        view.findViewById<TextView>(R.id.tv_title).text = title

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_notes)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        vm.getNotesByCategory(category).observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }

        view.findViewById<View>(R.id.fab_add).setOnClickListener {
            openEditFragment(null)
        }

        view.findViewById<View>(R.id.btn_export).setOnClickListener {
            MarkdownExporter.exportAndShare(requireContext())
        }

        adapter.onItemClick = { note -> openEditFragment(note) }
    }

    private fun openEditFragment(note: Note?) {
        val fragment = NoteEditFragment().apply {
            arguments = Bundle().apply {
                putString("category", category)
                if (note != null) {
                    putString("id", note.id)
                    putString("content", note.content)
                    putString("tags", note.tags)
                }
            }
        }
        (activity as MainActivity).switchToFragment(fragment)
    }
}

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.VH>() {

    private var items = listOf<Note>()
    var onItemClick: ((Note) -> Unit)? = null

    fun submitList(list: List<Note>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvContent = view.findViewById<TextView>(R.id.tv_content)
        private val tvTags = view.findViewById<TextView>(R.id.tv_tags)

        fun bind(n: Note) {
            tvContent.text = n.content
            tvTags.text = if (n.tags.isNotBlank()) "🏷 ${n.tags}" else ""
            itemView.setOnClickListener { onItemClick?.invoke(n) }
        }
    }
}
