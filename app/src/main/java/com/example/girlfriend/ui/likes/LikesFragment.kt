package com.example.girlfriend.ui.likes

import com.example.girlfriend.R
import com.example.girlfriend.ui.noteedit.NoteListFragment

class LikesFragment : NoteListFragment() {
    override val category = "like"
    override val title = "喜欢"
    override val accentColorResId = R.color.secondary
    override val accentContainerColorResId = R.color.secondary_container
    override val accentOnContainerColorResId = R.color.on_secondary_container
}
