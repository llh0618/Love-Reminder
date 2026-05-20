package com.example.girlfriend.ui.home

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
import com.example.girlfriend.data.entity.Anniversary
import com.example.girlfriend.ui.anniversary.AnniversaryEditFragment
import com.example.girlfriend.util.CalendarHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var recyclerView: RecyclerView
    private val adapter = AnniversaryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        recyclerView = view.findViewById(R.id.recycler_anniversaries)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.fab_add).setOnClickListener {
            openEditFragment(null)
        }

        adapter.onItemClick = { anniversary ->
            openEditFragment(anniversary)
        }

        viewModel.anniversaries.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list.sortedBy { daysUntil(it.date) })
        }
    }

    private fun openEditFragment(anniversary: Anniversary?) {
        val fragment = AnniversaryEditFragment().apply {
            arguments = Bundle().apply {
                if (anniversary != null) {
                    putString("id", anniversary.id)
                    putString("name", anniversary.name)
                    putString("date", anniversary.date)
                    putString("type", anniversary.type)
                    putString("repeat", anniversary.repeat)
                    putInt("remindBeforeDays", anniversary.remindBeforeDays)
                    putString("note", anniversary.note)
                    putString("calendarEventIds", anniversary.calendarEventIds)
                    putString("relationshipDays", anniversary.relationshipDays)
                }
            }
        }
        (activity as MainActivity).switchToFragment(fragment)
    }

    companion object {
        fun findNextMilestone(baseDate: String, milestoneDays: List<Int>): Pair<Int, String>? {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val base = sdf.parse(baseDate) ?: return null
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            }
            val milestones = milestoneDays.map { days ->
                val cal = Calendar.getInstance().apply {
                    time = base
                    add(Calendar.DAY_OF_MONTH, days)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }
                days to "${cal.get(Calendar.YEAR)}-${String.format("%02d", cal.get(Calendar.MONTH)+1)}-${String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))}"
            }.filter { (_, dateStr) ->
                val date = sdf.parse(dateStr)
                date != null && date.time >= today.timeInMillis
            }.sortedBy { (_, dateStr) ->
                sdf.parse(dateStr)?.time ?: Long.MAX_VALUE
            }
            return milestones.firstOrNull()
        }

        fun daysUntil(dateStr: String): Int {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val target = sdf.parse(dateStr) ?: return Int.MAX_VALUE
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            }
            val targetCal = Calendar.getInstance().apply {
                time = target
                set(Calendar.YEAR, today.get(Calendar.YEAR))
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            }
            if (targetCal.before(today)) {
                targetCal.add(Calendar.YEAR, 1)
            }
            val diff = targetCal.timeInMillis - today.timeInMillis
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
        }
    }
}

class AnniversaryAdapter : RecyclerView.Adapter<AnniversaryAdapter.VH>() {

    private var items = listOf<Anniversary>()
    var onItemClick: ((Anniversary) -> Unit)? = null

    fun submitList(list: List<Anniversary>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anniversary, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName = view.findViewById<TextView>(R.id.tv_name)
        private val tvDate = view.findViewById<TextView>(R.id.tv_date)
        private val tvCountdown = view.findViewById<TextView>(R.id.tv_countdown)
        private val tvLabel = view.findViewById<TextView>(R.id.tv_countdown_label)
        private val tvMilestones = view.findViewById<TextView>(R.id.tv_milestones)

        fun bind(a: Anniversary) {
            val emoji = when (a.type) {
                "valentine" -> "💝"; "520" -> "💕"; "qixi" -> "💗"
                "birthday" -> "🎂"; "anniversary" -> "💍"
                "relationship" -> "💑"; else -> "💖"
            }

            if (a.type == "relationship" && a.relationshipDays.isNotBlank()) {
                bindRelationship(a, emoji)
            } else {
                bindNormal(a, emoji)
            }

            itemView.setOnClickListener { onItemClick?.invoke(a) }
        }

        private fun bindNormal(a: Anniversary, emoji: String) {
            tvName.text = "$emoji  ${a.name}"
            tvDate.text = a.date
            tvMilestones.visibility = View.GONE

            val days = HomeFragment.daysUntil(a.date)
            updateCountdown(days)
        }

        private fun bindRelationship(a: Anniversary, emoji: String) {
            tvName.text = "$emoji  ${a.name}"
            tvDate.text = "始于 ${a.date}"

            // 最近的下一个里程碑
            val milestones = a.relationshipDays.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
            val nextMilestone = HomeFragment.findNextMilestone(a.date, milestones)

            if (nextMilestone != null) {
                val days = HomeFragment.daysUntil(nextMilestone.second)
                updateCountdown(days)
                tvMilestones.visibility = View.VISIBLE
                tvMilestones.text = "⏳ ${nextMilestone.first}天纪念日 · ${nextMilestone.second}"
            } else {
                // 没有未到来的里程碑，显示每年纪念日
                val days = HomeFragment.daysUntil(a.date)
                updateCountdown(days)
                tvMilestones.visibility = View.GONE
            }
        }

        private fun updateCountdown(days: Int) {
            when {
                days == 0 -> {
                    tvCountdown.text = "今天"
                    tvCountdown.textSize = 28f
                    tvLabel.text = "就是今天！"
                }
                days > 0 -> {
                    tvCountdown.text = "$days"
                    tvLabel.text = "天后"
                }
                else -> {
                    tvCountdown.text = "已过"
                    tvLabel.text = ""
                }
            }
        }
    }
}
