package com.example.girlfriend.ui.gifts

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
import com.example.girlfriend.data.entity.Gift
import com.example.girlfriend.util.MarkdownExporter
import com.google.android.material.chip.Chip

class GiftsFragment : Fragment() {

    private lateinit var viewModel: GiftViewModel
    private val adapter = GiftAdapter()
    private var currentFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_gifts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[GiftViewModel::class.java]

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_gifts)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewModel.allGifts.observe(viewLifecycleOwner) { allGifts ->
            val filtered = when (currentFilter) {
                "all" -> allGifts
                else -> allGifts.filter { it.status == currentFilter }
            }
            adapter.submitList(filtered)
        }

        view.findViewById<View>(R.id.fab_add).setOnClickListener {
            openEditFragment(null)
        }

        view.findViewById<View>(R.id.btn_export_gifts).setOnClickListener {
            MarkdownExporter.exportGifts(requireContext())
        }

        adapter.onItemClick = { gift -> openEditFragment(gift) }

        // 状态筛选
        val filterChips = mapOf(
            R.id.chip_all to "all",
            R.id.chip_want to "want",
            R.id.chip_bought to "bought"
        )
        filterChips.forEach { (id, status) ->
            view.findViewById<Chip>(id).setOnClickListener {
                currentFilter = status
                // 刷新筛选
                viewModel.allGifts.value?.let { gifts ->
                    adapter.submitList(if (status == "all") gifts else gifts.filter { it.status == status })
                }
            }
        }
    }

    private fun openEditFragment(gift: Gift?) {
        val fragment = GiftEditFragment().apply {
            arguments = Bundle().apply {
                if (gift != null) {
                    putString("id", gift.id)
                    putString("name", gift.name)
                    putString("link", gift.link)
                    putString("status", gift.status)
                    putString("note", gift.note)
                }
            }
        }
        (activity as MainActivity).switchToFragment(fragment)
    }
}

class GiftAdapter : RecyclerView.Adapter<GiftAdapter.VH>() {

    private var items = listOf<Gift>()
    var onItemClick: ((Gift) -> Unit)? = null

    fun submitList(list: List<Gift>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gift, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName = view.findViewById<TextView>(R.id.tv_gift_name)
        private val tvNote = view.findViewById<TextView>(R.id.tv_gift_note)
        private val tvStatus = view.findViewById<TextView>(R.id.tv_status)

        fun bind(g: Gift) {
            tvName.text = g.name
            tvNote.text = if (g.note.isNotBlank()) g.note else if (g.link.isNotBlank()) "🔗 $g.link" else ""
            tvStatus.text = when (g.status) {
                "want" -> "想送"
                "bought" -> "已送"
                else -> ""
            }
            tvStatus.setTextColor(when (g.status) {
                "want" -> 0xFFFF9800.toInt()
                "bought" -> 0xFF4CAF50.toInt()
                else -> 0xFF999999.toInt()
            })

            itemView.setOnClickListener { onItemClick?.invoke(g) }
        }
    }
}
