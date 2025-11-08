package io.aatricks.starnotifier.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.aatricks.starnotifier.R
import io.aatricks.starnotifier.data.model.Repository

class RepositoryAdapter() :
    ListAdapter<Repository, RepositoryAdapter.RepositoryViewHolder>(RepositoryDiffCallback()) {

    var onRepositorySelected: ((String, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repository, parent, false)
        return RepositoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RepositoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.repoNameTextView)
        private val starsTextView: TextView = itemView.findViewById(R.id.starsTextView)
        private val forksTextView: TextView = itemView.findViewById(R.id.forksTextView)
        private val checkBox: CheckBox = itemView.findViewById(R.id.repoCheckBox)

        fun bind(repository: Repository) {
            nameTextView.text = repository.name.substringAfter('/')
            starsTextView.text = "${repository.currentStars} ⭐"
            forksTextView.text = "${repository.currentForks} 🍴"

            checkBox.isChecked = repository.isSelected

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onRepositorySelected?.invoke(repository.name, isChecked)
            }
        }
    }

    class RepositoryDiffCallback : DiffUtil.ItemCallback<Repository>() {
        override fun areItemsTheSame(oldItem: Repository, newItem: Repository): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Repository, newItem: Repository): Boolean {
            return oldItem == newItem
        }
    }
}