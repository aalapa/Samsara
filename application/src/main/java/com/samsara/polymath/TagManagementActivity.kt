package com.samsara.polymath

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.samsara.polymath.adapter.TagManagementAdapter
import com.samsara.polymath.data.TagWithUsageCount
import com.samsara.polymath.databinding.ActivityTagManagementBinding
import com.samsara.polymath.databinding.DialogCreateTagBinding
import com.samsara.polymath.viewmodel.TagViewModel
import kotlinx.coroutines.launch

class TagManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTagManagementBinding
    private lateinit var viewModel: TagViewModel
    private lateinit var adapter: TagManagementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TagViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        observeTags()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = TagManagementAdapter(
            onRenameTag = { tagWithUsage ->
                showRenameTagDialog(tagWithUsage)
            },
            onDeleteTag = { tagWithUsage ->
                showDeleteTagConfirmation(tagWithUsage)
            }
        )

        binding.tagsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tagsRecyclerView.adapter = adapter
    }

    private fun observeTags() {
        viewModel.tagsWithUsage.observe(this) { tagsWithUsage ->
            adapter.submitList(tagsWithUsage)
        }
    }

    private fun setupFab() {
        binding.addTagFab.setOnClickListener {
            showCreateTagDialog()
        }
    }

    private fun showCreateTagDialog() {
        val dialogBinding = DialogCreateTagBinding.inflate(LayoutInflater.from(this))

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.create_tag))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.done)) { _, _ ->
                val tagName = dialogBinding.tagNameEditText.text?.toString()?.trim()
                val tagColor = dialogBinding.tagColorEditText.text?.toString()?.trim()

                if (!tagName.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        val newTag = viewModel.createTag(tagName, tagColor)
                        if (newTag != null) {
                            Toast.makeText(this@TagManagementActivity, "Tag created", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@TagManagementActivity, "Failed to create tag", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Please enter a tag name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }

    private fun showRenameTagDialog(tagWithUsage: TagWithUsageCount) {
        val tag = tagWithUsage.tag
        val dialogBinding = DialogCreateTagBinding.inflate(LayoutInflater.from(this))
        dialogBinding.tagNameEditText.setText(tag.name)
        dialogBinding.tagColorEditText.setText(tag.color ?: "")

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.rename_tag))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.done)) { _, _ ->
                val newName = dialogBinding.tagNameEditText.text?.toString()?.trim()

                if (!newName.isNullOrEmpty()) {
                    viewModel.renameTag(tag.id, newName) { success ->
                        if (success) {
                            Toast.makeText(this, "Tag renamed", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to rename tag", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Please enter a tag name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }

    private fun showDeleteTagConfirmation(tagWithUsage: TagWithUsageCount) {
        val tag = tagWithUsage.tag
        val usageCount = tagWithUsage.usageCount

        val message = if (usageCount > 0) {
            getString(R.string.tag_in_use, usageCount) + "\n\n" + getString(R.string.delete_tag_confirmation)
        } else {
            getString(R.string.delete_tag_confirmation)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_tag))
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteTag(tag.id) { success ->
                    if (success) {
                        Toast.makeText(this@TagManagementActivity, "Tag deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@TagManagementActivity, "Failed to delete tag", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}

