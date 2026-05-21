package com.example.girlfriend.ui.anniversary

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.girlfriend.MainActivity
import com.example.girlfriend.R
import com.example.girlfriend.data.entity.Anniversary
import com.example.girlfriend.ui.home.HomeViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class AnniversaryEditFragment : Fragment() {

    private var editId: String? = null
    private var selectedType = "birthday"
    private var selectedRepeat = "yearly"
    private var selectedRemindDays = 3
    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private val milestoneDays = mutableListOf(30, 99, 180)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_anniversary_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments

        val etName = view.findViewById<TextInputEditText>(R.id.et_name)
        val etDate = view.findViewById<TextInputEditText>(R.id.et_date)
        val etNote = view.findViewById<TextInputEditText>(R.id.et_note)
        val rgRepeat = view.findViewById<RadioGroup>(R.id.rg_repeat)
        val rgRemind = view.findViewById<RadioGroup>(R.id.rg_remind)

        if (args?.getString("id") != null) {
            editId = args.getString("id")
            etName.setText(args.getString("name"))
            etDate.setText(args.getString("date"))
            etNote.setText(args.getString("note"))
            selectedType = args.getString("type") ?: "birthday"
            selectedRepeat = args.getString("repeat") ?: "yearly"
            selectedRemindDays = args.getInt("remindBeforeDays", 3)
            // 恢复里程碑天数
            val savedDays = args.getString("relationshipDays")
            if (!savedDays.isNullOrBlank()) {
                milestoneDays.clear()
                milestoneDays.addAll(savedDays.split(",").mapNotNull { it.trim().toIntOrNull() })
            }
            view.findViewById<View>(R.id.btn_delete).visibility = View.VISIBLE
        }

        // 日期
        val dateStr = args?.getString("date")
        if (!dateStr.isNullOrBlank()) {
            val parts = dateStr.split("-").map { it.toInt() }
            selectedYear = parts[0]; selectedMonth = parts[1] - 1; selectedDay = parts[2]
        } else {
            val cal = Calendar.getInstance()
            selectedYear = cal.get(Calendar.YEAR)
            selectedMonth = cal.get(Calendar.MONTH)
            selectedDay = cal.get(Calendar.DAY_OF_MONTH)
        }
        updateDateDisplay(etDate)

        etDate.setOnClickListener {
            DatePickerDialog(requireContext(),
                { _, y, m, d -> selectedYear = y; selectedMonth = m; selectedDay = d; updateDateDisplay(etDate) },
                selectedYear, selectedMonth, selectedDay
            ).show()
        }

        // 类型 Chip
        val chips = mapOf(
            R.id.chip_valentine to "valentine", R.id.chip_520 to "520",
            R.id.chip_qixi to "qixi", R.id.chip_birthday to "birthday",
            R.id.chip_anniversary to "anniversary", R.id.chip_relationship to "relationship",
            R.id.chip_custom to "custom"
        )
        chips.forEach { (id, type) ->
            view.findViewById<Chip>(id).apply {
                isChecked = type == selectedType
                setOnClickListener { selectType(type, view) }
            }
        }

        val layoutMilestones = view.findViewById<LinearLayout>(R.id.layout_milestones)
        val layoutRepeat = view.findViewById<LinearLayout>(R.id.layout_repeat)
        val layoutRemind = view.findViewById<LinearLayout>(R.id.layout_remind)
        val milestoneChips = view.findViewById<LinearLayout>(R.id.milestone_chips)
        val etNewDay = view.findViewById<EditText>(R.id.et_new_day)
        val btnAddDay = view.findViewById<Button>(R.id.btn_add_day)

        // 初始化里程碑区域
        refreshMilestoneChips(milestoneChips)
        btnAddDay.setOnClickListener {
            val day = etNewDay.text.toString().toIntOrNull()
            if (day != null && day > 0 && day !in milestoneDays) {
                milestoneDays.add(day)
                milestoneDays.sort()
                refreshMilestoneChips(milestoneChips)
                etNewDay.text.clear()
            }
        }

        // 根据类型控制 UI
        updateTypeUI(layoutMilestones, layoutRepeat, layoutRemind, rgRepeat, rgRemind)

        // 重复
        if (selectedRepeat == "once") {
            view.findViewById<RadioButton>(R.id.rb_once).isChecked = true
        }
        rgRepeat.setOnCheckedChangeListener { _, id ->
            selectedRepeat = if (id == R.id.rb_once) "once" else "yearly"
        }

        // 提醒
        val remindIds = mapOf(R.id.rb_day0 to 0, R.id.rb_day3 to 3, R.id.rb_day7 to 7)
        remindIds.entries.find { it.value == selectedRemindDays }?.key?.let {
            view.findViewById<RadioButton>(it).isChecked = true
        }
        rgRemind.setOnCheckedChangeListener { _, id ->
            selectedRemindDays = remindIds[id] ?: 3
        }

        view.findViewById<View>(R.id.tv_back).setOnClickListener {
            (activity as MainActivity).switchToFragment(com.example.girlfriend.ui.home.HomeFragment())
        }

        view.findViewById<View>(R.id.tv_save).setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) return@setOnClickListener

            val anniversary = Anniversary(
                id = editId ?: java.util.UUID.randomUUID().toString(),
                name = name,
                date = formatDate(),
                type = selectedType,
                repeat = if (selectedType == "relationship") "yearly" else selectedRepeat,
                remindBeforeDays = selectedRemindDays,
                note = etNote.text.toString().trim(),
                calendarEventIds = args?.getString("calendarEventIds") ?: "",
                relationshipDays = if (selectedType == "relationship") milestoneDays.joinToString(",") else ""
            )

            val homeVM = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
            homeVM.save(anniversary)
            (activity as MainActivity).switchToFragment(com.example.girlfriend.ui.home.HomeFragment())
        }

        view.findViewById<View>(R.id.btn_delete).setOnClickListener {
            if (editId != null) {
                val homeVM = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
                homeVM.delete(Anniversary(
                    id = editId!!, name = "", date = formatDate(), type = selectedType,
                    repeat = selectedRepeat, remindBeforeDays = selectedRemindDays,
                    calendarEventIds = args?.getString("calendarEventIds") ?: ""
                ))
                (activity as MainActivity).switchToFragment(com.example.girlfriend.ui.home.HomeFragment())
            }
        }
    }

    private fun updateTypeUI(
        layoutMilestones: LinearLayout, layoutRepeat: LinearLayout, layoutRemind: LinearLayout,
        rgRepeat: RadioGroup, rgRemind: RadioGroup
    ) {
        if (selectedType == "relationship") {
            layoutMilestones.visibility = View.VISIBLE
            layoutRepeat.visibility = View.GONE
            layoutRemind.visibility = View.VISIBLE // 显示提醒，隐藏重复
            rgRepeat.check(R.id.rb_yearly)
            selectedRepeat = "yearly"
        } else {
            layoutMilestones.visibility = View.GONE
            layoutRepeat.visibility = View.VISIBLE
            layoutRemind.visibility = View.VISIBLE
        }
    }

    private fun refreshMilestoneChips(container: LinearLayout) {
        container.removeAllViews()
        for (day in milestoneDays) {
            val chip = Chip(requireContext()).apply {
                text = "${day}天"
                isCloseIconVisible = true
                chipBackgroundColor = ColorStateList.valueOf(0xF0D5C8.toInt())
                setTextColor(0x8C6B70.toInt())
                setOnCloseIconClickListener {
                    milestoneDays.remove(day)
                    refreshMilestoneChips(container)
                }
            }
            container.addView(chip)
        }
    }

    private fun updateDateDisplay(etDate: TextInputEditText) {
        etDate.setText(formatDate())
    }

    private fun formatDate() = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)

    private fun selectType(type: String, root: View) {
        selectedType = type
        val typeChips = listOf("valentine", "520", "qixi", "birthday", "anniversary", "relationship", "custom")
        val chipIds = listOf(R.id.chip_valentine, R.id.chip_520, R.id.chip_qixi, R.id.chip_birthday, R.id.chip_anniversary, R.id.chip_relationship, R.id.chip_custom)
        typeChips.zip(chipIds).forEach { (t, id) ->
            root.findViewById<Chip>(id).isChecked = t == type
        }
        updateTypeUI(
            root.findViewById(R.id.layout_milestones),
            root.findViewById(R.id.layout_repeat),
            root.findViewById(R.id.layout_remind),
            root.findViewById(R.id.rg_repeat),
            root.findViewById(R.id.rg_remind)
        )
    }
}
