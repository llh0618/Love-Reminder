package com.example.girlfriend.ui.dislikes

import com.example.girlfriend.R
import com.example.girlfriend.ui.noteedit.NoteListFragment

class DislikesFragment : NoteListFragment() {
    override val category = "dislike"
    override val title = "讨厌"
    override val accentColorResId = R.color.tertiary
    override val accentContainerColorResId = R.color.tertiary_container
    override val accentOnContainerColorResId = R.color.on_tertiary_container
}
