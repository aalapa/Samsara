package com.samsara.polymath.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.samsara.polymath.data.Persona
import com.samsara.polymath.databinding.ItemPersonaBinding

class PersonaAdapter(
    private val onPersonaClick: (Persona) -> Unit
) : ListAdapter<Persona, PersonaAdapter.PersonaViewHolder>(PersonaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonaViewHolder {
        val binding = ItemPersonaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PersonaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PersonaViewHolder(
        private val binding: ItemPersonaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(persona: Persona) {
            binding.personaNameTextView.text = persona.name
            
            // Show open count if greater than 0
            if (persona.openCount > 0) {
                binding.openCountTextView.text = persona.openCount.toString()
                binding.openCountTextView.visibility = android.view.View.VISIBLE
            } else {
                binding.openCountTextView.visibility = android.view.View.GONE
            }
            
            binding.root.setOnClickListener {
                onPersonaClick(persona)
            }
        }
    }

    class PersonaDiffCallback : DiffUtil.ItemCallback<Persona>() {
        override fun areItemsTheSame(oldItem: Persona, newItem: Persona): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Persona, newItem: Persona): Boolean {
            return oldItem == newItem
        }
    }
}

