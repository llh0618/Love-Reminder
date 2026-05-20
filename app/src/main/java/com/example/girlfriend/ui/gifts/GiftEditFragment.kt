package com.example.girlfriend.ui.gifts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.girlfriend.MainActivity
import com.example.girlfriend.R
import com.example.girlfriend.data.entity.Gift
import com.google.android.material.textfield.TextInputEditText

class GiftEditFragment : Fragment() {

    private var editId: String? = null
    private var selectedStatus = "want"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_gift_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments

        val etName = view.findViewById<TextInputEditText>(R.id.et_name)
        val etLink = view.findViewById<TextInputEditText>(R.id.et_link)
        val etNote = view.findViewById<TextInputEditText>(R.id.et_note)

        if (args?.getString("id") != null) {
            editId = args.getString("id")
            etName.setText(args.getString("name"))
            etLink.setText(args.getString("link"))
            etNote.setText(args.getString("note"))
            selectedStatus = args.getString("status") ?: "want"
            when (selectedStatus) {
                "bought" -> view.findViewById<android.widget.RadioButton>(R.id.rb_bought).isChecked = true
                "given" -> view.findViewById<android.widget.RadioButton>(R.id.rb_given).isChecked = true
            }
            view.findViewById<View>(R.id.btn_delete).visibility = View.VISIBLE
        }

        view.findViewById<android.widget.RadioGroup>(R.id.rg_status).setOnCheckedChangeListener { _, id ->
            selectedStatus = when (id) {
                R.id.rb_bought -> "bought"
                R.id.rb_given -> "given"
                else -> "want"
            }
        }

        view.findViewById<View>(R.id.tv_back).setOnClickListener {
            (activity as MainActivity).switchToFragment(GiftsFragment())
        }

        view.findViewById<View>(R.id.tv_save).setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) return@setOnClickListener

            val gift = Gift(
                id = editId ?: java.util.UUID.randomUUID().toString(),
                name = name,
                link = etLink.text.toString().trim(),
                status = selectedStatus,
                note = etNote.text.toString().trim()
            )

            val vm = ViewModelProvider(this)[GiftViewModel::class.java]
            vm.save(gift)
            (activity as MainActivity).switchToFragment(GiftsFragment())
        }

        view.findViewById<View>(R.id.btn_delete).setOnClickListener {
            if (editId != null) {
                val vm = ViewModelProvider(this)[GiftViewModel::class.java]
                vm.delete(Gift(id = editId!!, name = "", status = ""))
                (activity as MainActivity).switchToFragment(GiftsFragment())
            }
        }
    }
}
