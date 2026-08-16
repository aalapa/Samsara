package com.samsara.polymath.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.samsara.polymath.R
import com.samsara.polymath.data.DecayLevel
import com.samsara.polymath.data.Persona
import com.samsara.polymath.data.PersonaWithTaskCount
import com.samsara.polymath.data.RankStatus
import com.samsara.polymath.databinding.ItemArchivedHeaderBinding
import com.samsara.polymath.databinding.ItemPersonaBinding

class PersonaAdapter(
    private val onPersonaClick: (Persona) -> Unit,
    private val onPersonaEdit: (Persona) -> Unit,
    private val onPersonaDelete: (Persona) -> Unit,
    private val onPersonaToggleFocus: (Persona) -> Unit,
    private val onPersonaToggleChakra: (Persona) -> Unit
) : ListAdapter<PersonaListItem, RecyclerView.ViewHolder>(PersonaListDiffCallback()) {

    private var activePersonas: List<PersonaWithTaskCount> = emptyList()
    private var archivedPersonas: List<PersonaWithTaskCount> = emptyList()
    private var isArchivedExpanded = false

    fun setPersonas(active: List<PersonaWithTaskCount>, archived: List<PersonaWithTaskCount>) {
        activePersonas = active
        archivedPersonas = archived
        submitList(buildDisplayList())
    }

    fun countPersonas(predicate: (PersonaWithTaskCount) -> Boolean): Int =
        (activePersonas + archivedPersonas).count(predicate)

    private fun buildDisplayList(): List<PersonaListItem> {
        val items = mutableListOf<PersonaListItem>()
        activePersonas.forEach { items.add(PersonaListItem.PersonaItem(it)) }
        if (archivedPersonas.isNotEmpty()) {
            items.add(PersonaListItem.ArchivedHeader)
            if (isArchivedExpanded) {
                archivedPersonas.forEach { items.add(PersonaListItem.PersonaItem(it)) }
            }
        }
        return items
    }

    companion object {
        private const val VIEW_TYPE_PERSONA = 0
        private const val VIEW_TYPE_HEADER = 1
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is PersonaListItem.PersonaItem -> VIEW_TYPE_PERSONA
        is PersonaListItem.ArchivedHeader -> VIEW_TYPE_HEADER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemArchivedHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemPersonaBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                PersonaViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PersonaListItem.PersonaItem -> (holder as PersonaViewHolder).bind(item.data)
            is PersonaListItem.ArchivedHeader -> (holder as HeaderViewHolder).bind(isArchivedExpanded) {
                isArchivedExpanded = !isArchivedExpanded
                submitList(buildDisplayList())
            }
        }
    }

    inner class HeaderViewHolder(
        private val binding: ItemArchivedHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expanded: Boolean, onToggle: () -> Unit) {
            binding.archivedChevronTextView.text = if (expanded) "▼" else "▶"
            binding.root.setOnClickListener { onToggle() }
        }
    }

    inner class PersonaViewHolder(
        private val binding: ItemPersonaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(personaWithCount: PersonaWithTaskCount) {
            val persona = personaWithCount.persona
            val completedCount = personaWithCount.completedTaskCount
            val openCount = personaWithCount.openTaskCount

            binding.personaNameTextView.text = persona.name

            val countsText = if (completedCount > 0) {
                "$openCount open · $completedCount done"
            } else {
                "$openCount open"
            }
            binding.personaCountsTextView.text = countsText

            var bgColor = Color.parseColor("#FFFFFF")
            try {
                bgColor = Color.parseColor(persona.backgroundColor)
            } catch (_: Exception) { }

            val finalBgColor = when (personaWithCount.decayLevel) {
                DecayLevel.SERIOUS -> desaturateColor(bgColor, 0.3f)
                else -> bgColor
            }

            val textColor = if (isColorDark(finalBgColor)) Color.WHITE else Color.BLACK
            val menuIconAlpha = (0.30f * 255).toInt()
            val menuIconColor = Color.argb(menuIconAlpha, Color.red(textColor), Color.green(textColor), Color.blue(textColor))

            binding.personaNameTextView.setTextColor(textColor)
            binding.personaCountsTextView.setTextColor(Color.argb(180, Color.red(textColor), Color.green(textColor), Color.blue(textColor)))
            binding.openCountTextView.setTextColor(textColor)
            binding.menuButton.setColorFilter(menuIconColor)

            val score = personaWithCount.score
            binding.openCountTextView.text = if (score > 0) score.toInt().toString() else "—"
            binding.openCountTextView.visibility = View.VISIBLE

            // Rank rail colour
            val rankColor = when (personaWithCount.rankStatus) {
                RankStatus.UP -> binding.root.context.getColor(R.color.positive)
                RankStatus.DOWN -> binding.root.context.getColor(R.color.negative)
                RankStatus.STABLE -> Color.argb(77, Color.red(textColor), Color.green(textColor), Color.blue(textColor))
            }
            binding.rankRailView.setBackgroundColor(rankColor)

            applyDecayVisuals(personaWithCount.decayLevel, finalBgColor)

            binding.menuButton.setOnClickListener { view ->
                val contextWrapper = android.view.ContextThemeWrapper(
                    view.context,
                    R.style.PopupMenuTheme
                )
                val popup = PopupMenu(contextWrapper, view)
                popup.menuInflater.inflate(R.menu.persona_menu, popup.menu)

                val focusItem = popup.menu.findItem(R.id.action_toggle_focus)
                focusItem?.title = if (persona.isFocused) {
                    view.context.getString(R.string.unfocus_persona)
                } else {
                    view.context.getString(R.string.focus_persona)
                }

                val chakraItem = popup.menu.findItem(R.id.action_toggle_chakra)
                chakraItem?.title = if (persona.isChakra) {
                    view.context.getString(R.string.unchakra_persona)
                } else {
                    view.context.getString(R.string.chakra_persona)
                }

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_toggle_focus -> { onPersonaToggleFocus(persona); true }
                        R.id.action_toggle_chakra -> { onPersonaToggleChakra(persona); true }
                        R.id.action_edit_persona -> { onPersonaEdit(persona); true }
                        R.id.action_delete_persona -> { onPersonaDelete(persona); true }
                        else -> false
                    }
                }
                popup.show()
            }

            binding.root.setOnClickListener { onPersonaClick(persona) }
            binding.tagsChipGroup.visibility = View.GONE
        }

        private fun applyDecayVisuals(decayLevel: DecayLevel, bgColor: Int) {
            when (decayLevel) {
                DecayLevel.NONE -> { binding.root.alpha = 1.0f; binding.root.setCardBackgroundColor(bgColor) }
                DecayLevel.SLIGHT -> { binding.root.alpha = 0.90f; binding.root.setCardBackgroundColor(bgColor) }
                DecayLevel.MEDIUM -> { binding.root.alpha = 0.75f; binding.root.setCardBackgroundColor(bgColor) }
                DecayLevel.SERIOUS -> { binding.root.alpha = 0.60f; binding.root.setCardBackgroundColor(desaturateColor(bgColor, 0.3f)) }
            }
        }

        private fun isColorDark(color: Int): Boolean {
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            return darkness >= 0.5
        }

        private fun desaturateColor(color: Int, saturation: Float): Int {
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            val newR = (gray + saturation * (r - gray)).toInt().coerceIn(0, 255)
            val newG = (gray + saturation * (g - gray)).toInt().coerceIn(0, 255)
            val newB = (gray + saturation * (b - gray)).toInt().coerceIn(0, 255)
            return Color.rgb(newR, newG, newB)
        }
    }

    class PersonaListDiffCallback : DiffUtil.ItemCallback<PersonaListItem>() {
        override fun areItemsTheSame(oldItem: PersonaListItem, newItem: PersonaListItem): Boolean {
            return when {
                oldItem is PersonaListItem.ArchivedHeader && newItem is PersonaListItem.ArchivedHeader -> true
                oldItem is PersonaListItem.PersonaItem && newItem is PersonaListItem.PersonaItem ->
                    oldItem.data.persona.id == newItem.data.persona.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: PersonaListItem, newItem: PersonaListItem): Boolean {
            return when {
                oldItem is PersonaListItem.ArchivedHeader && newItem is PersonaListItem.ArchivedHeader -> true
                oldItem is PersonaListItem.PersonaItem && newItem is PersonaListItem.PersonaItem ->
                    oldItem.data.persona == newItem.data.persona
                        && oldItem.data.completedTaskCount == newItem.data.completedTaskCount
                        && oldItem.data.openTaskCount == newItem.data.openTaskCount
                        && oldItem.data.score == newItem.data.score
                        && oldItem.data.rankStatus == newItem.data.rankStatus
                        && oldItem.data.decayLevel == newItem.data.decayLevel
                else -> false
            }
        }
    }
}
