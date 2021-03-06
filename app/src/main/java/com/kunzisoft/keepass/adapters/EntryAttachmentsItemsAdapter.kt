/*
 * Copyright 2019 Jeremy Jamet / Kunzisoft.
 *
 * This file is part of KeePassDX.
 *
 *  KeePassDX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.adapters

import android.content.Context
import android.text.format.Formatter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kunzisoft.keepass.R
import com.kunzisoft.keepass.database.element.database.CompressionAlgorithm
import com.kunzisoft.keepass.model.AttachmentState
import com.kunzisoft.keepass.model.EntryAttachmentState
import com.kunzisoft.keepass.model.StreamDirection

class EntryAttachmentsItemsAdapter(context: Context)
    : AnimatedItemsAdapter<EntryAttachmentState, EntryAttachmentsItemsAdapter.EntryBinariesViewHolder>(context) {

    var onItemClickListener: ((item: EntryAttachmentState)->Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryBinariesViewHolder {
        return EntryBinariesViewHolder(inflater.inflate(R.layout.item_attachment, parent, false))
    }

    override fun onBindViewHolder(holder: EntryBinariesViewHolder, position: Int) {
        val entryAttachmentState = itemsList[position]

        holder.itemView.visibility = View.VISIBLE
        holder.binaryFileTitle.text = entryAttachmentState.attachment.name
        holder.binaryFileSize.text = Formatter.formatFileSize(context,
                entryAttachmentState.attachment.binaryAttachment.length())
        holder.binaryFileCompression.apply {
            if (entryAttachmentState.attachment.binaryAttachment.isCompressed) {
                text = CompressionAlgorithm.GZip.getName(context.resources)
                visibility = View.VISIBLE
            } else {
                text = ""
                visibility = View.GONE
            }
        }
        when (entryAttachmentState.streamDirection) {
            StreamDirection.UPLOAD -> {
                holder.binaryFileProgressIcon.isActivated = true
                when (entryAttachmentState.downloadState) {
                    AttachmentState.START,
                    AttachmentState.IN_PROGRESS -> {
                        holder.binaryFileProgressContainer.visibility = View.VISIBLE
                        holder.binaryFileProgress.apply {
                            visibility = View.VISIBLE
                            progress = entryAttachmentState.downloadProgression
                        }
                        holder.binaryFileDeleteButton.apply {
                            visibility = View.GONE
                            setOnClickListener(null)
                        }
                    }
                    AttachmentState.NULL,
                    AttachmentState.ERROR,
                    AttachmentState.COMPLETE -> {
                        holder.binaryFileProgressContainer.visibility = View.GONE
                        holder.binaryFileProgress.visibility = View.GONE
                        holder.binaryFileDeleteButton.apply {
                            visibility = View.VISIBLE
                            onBindDeleteButton(holder, this, entryAttachmentState, position)
                        }
                    }
                }
                holder.itemView.setOnClickListener(null)
            }
            StreamDirection.DOWNLOAD -> {
                holder.binaryFileProgressIcon.isActivated = false
                holder.binaryFileProgressContainer.visibility = View.VISIBLE
                holder.binaryFileDeleteButton.visibility = View.GONE
                holder.binaryFileProgress.apply {
                    visibility = when (entryAttachmentState.downloadState) {
                        AttachmentState.NULL, AttachmentState.COMPLETE, AttachmentState.ERROR -> View.GONE
                        AttachmentState.START, AttachmentState.IN_PROGRESS -> View.VISIBLE
                    }
                    progress = entryAttachmentState.downloadProgression
                }
                holder.itemView.setOnClickListener {
                    onItemClickListener?.invoke(entryAttachmentState)
                }
            }
        }
    }

    class EntryBinariesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var binaryFileTitle: TextView = itemView.findViewById(R.id.item_attachment_title)
        var binaryFileSize: TextView = itemView.findViewById(R.id.item_attachment_size)
        var binaryFileCompression: TextView = itemView.findViewById(R.id.item_attachment_compression)
        var binaryFileProgressContainer: View = itemView.findViewById(R.id.item_attachment_progress_container)
        var binaryFileProgressIcon: ImageView = itemView.findViewById(R.id.item_attachment_icon)
        var binaryFileProgress: ProgressBar = itemView.findViewById(R.id.item_attachment_progress)
        var binaryFileDeleteButton: View = itemView.findViewById(R.id.item_attachment_delete_button)
    }
}