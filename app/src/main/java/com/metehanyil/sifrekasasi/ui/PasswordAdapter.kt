package com.metehanyil.sifrekasasi.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.metehanyil.sifrekasasi.crypto.CryptoManager
import com.metehanyil.sifrekasasi.data.PasswordEntry
import com.metehanyil.sifrekasasi.databinding.ItemPasswordBinding

class PasswordAdapter(
    private val onItemClick: (PasswordEntry) -> Unit,
    private val onCopyClick: (PasswordEntry) -> Unit,
    private val onDeleteClick: (PasswordEntry) -> Unit
) : ListAdapter<PasswordEntry, PasswordAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val revealedIds = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPasswordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPasswordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: PasswordEntry) {
            binding.tvSiteName.text = entry.siteName
            binding.tvSiteDomain.text = entry.siteDomain
            binding.tvUsername.text = entry.username

            binding.tvPassword.text = if (revealedIds.contains(entry.id)) {
                runCatching { CryptoManager.decrypt(entry.encryptedPassword) }.getOrDefault("•••")
            } else {
                MASKED_PASSWORD
            }

            binding.btnTogglePassword.setOnClickListener {
                if (!revealedIds.add(entry.id)) {
                    revealedIds.remove(entry.id)
                }
                notifyItemChanged(bindingAdapterPosition)
            }

            binding.btnCopyPassword.setOnClickListener { onCopyClick(entry) }
            binding.btnDelete.setOnClickListener { onDeleteClick(entry) }
            binding.root.setOnClickListener { onItemClick(entry) }
        }
    }

    companion object {
        private const val MASKED_PASSWORD = "••••••••"

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PasswordEntry>() {
            override fun areItemsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry) =
                oldItem == newItem
        }
    }
}
