package io.aatricks.starnotifier.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.aatricks.starnotifier.R
import io.aatricks.starnotifier.data.model.Repository
import io.aatricks.starnotifier.ui.view.RepositoryDetailActivity

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
        private val viewsTextView: TextView = itemView.findViewById(R.id.viewsTextView)
        private val clonesTextView: TextView = itemView.findViewById(R.id.clonesTextView)
        private val checkBox: CheckBox = itemView.findViewById(R.id.repoCheckBox)

        fun bind(repository: Repository) {
            nameTextView.text = repository.name.substringAfter('/')
            starsTextView.text = formatNumber(repository.currentStars)
            forksTextView.text = formatNumber(repository.currentForks)
            viewsTextView.text = formatNumber(repository.lifetimeViews)
            clonesTextView.text = formatNumber(repository.lifetimeClones)

            checkBox.isChecked = repository.isSelected

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onRepositorySelected?.invoke(repository.name, isChecked)
            }
            
            // Make the whole item clickable to open detail view
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, RepositoryDetailActivity::class.java).apply {
                    putExtra(RepositoryDetailActivity.EXTRA_REPOSITORY, Gson().toJson(repository))
                }
                context.startActivity(intent)
            }
        }
        
        private fun formatNumber(num: Int): String {
            return when {
                num >= 1000000 -> String.format("%.1fM", num / 1000000.0)
                num >= 1000 -> String.format("%.1fk", num / 1000.0)
                else -> num.toString()
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