package ru.skillbranch.devintensive.ui.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_chat_archive.*
import kotlinx.android.synthetic.main.item_chat_group.*
import kotlinx.android.synthetic.main.item_chat_single.*
import ru.skillbranch.devintensive.R
import ru.skillbranch.devintensive.models.data.ChatItem
import ru.skillbranch.devintensive.models.data.ChatType
import ru.skillbranch.devintensive.utils.Utils

class ChatAdapter(
    var context: Context,
    val listener: (ChatItem) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatItemViewHolder>() {

    companion object {
        private const val ARCHIVE_TYPE = 0
        private const val SINGLE_TYPE = 1
        private const val GROUP_TYPE = 2
    }

    var items: List<ChatItem> = listOf()

    fun updateData(data: List<ChatItem>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                items[oldPos].id == data[newPos].id

            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                items[oldPos] == data[newPos]

            override fun getOldListSize() = items.size
            override fun getNewListSize() = data.size
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = data
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SINGLE_TYPE -> SingleViewHolder(
                inflater.inflate(
                    R.layout.item_chat_single,
                    parent,
                    false
                )
            )
            GROUP_TYPE -> GroupViewHolder(inflater.inflate(R.layout.item_chat_group, parent, false))
            ARCHIVE_TYPE -> ArchiveViewHolder(
                inflater.inflate(
                    R.layout.item_chat_archive,
                    parent,
                    false
                )
            )
            else -> SingleViewHolder(inflater.inflate(R.layout.item_chat_group, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        holder.bind(items[position], listener)
    }

    override fun getItemViewType(position: Int) = when (items[position].chatType) {
        ChatType.SINGLE -> SINGLE_TYPE
        ChatType.GROUP -> GROUP_TYPE
        ChatType.ARCHIVE -> ARCHIVE_TYPE
    }

    abstract inner class ChatItemViewHolder(containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        override val containerView: View?
            get() = itemView

        abstract fun bind(item: ChatItem, listener: (ChatItem) -> Unit)
    }

    inner class SingleViewHolder(containerView: View) :
        ChatItemViewHolder(containerView), ItemTouchViewHolder {

        override fun onItemSelected() {
            val color = Utils.getColorFromResource(context, R.attr.selectedItemColor)
            itemView.setBackgroundColor(color)
        }

        override fun onItemCleared() {
            val color = Utils.getColorFromResource(context, R.attr.clearedItemColor)
            itemView.setBackgroundColor(color)
        }

        override fun bind(item: ChatItem, listener: (ChatItem) -> Unit) {
            if (item.avatar == null) {
                Glide.with(itemView).clear(iv_avatar_single)
                iv_avatar_single.setInitials(item.initials)
            } else {
                Glide.with(itemView)
                    .load(item.avatar)
                    .into(iv_avatar_single)
            }

            sv_indicator.visibility = if (item.isOnline) View.VISIBLE else View.GONE
            with(tv_date_single) {
                visibility = if (item.lastMessageDate != null) View.VISIBLE else View.GONE
                text = item.lastMessageDate
            }

            with(tv_counter_single) {
                visibility = if (item.messageCount > 0) View.VISIBLE else View.GONE
                text = item.messageCount.toString()
            }

            tv_title_single.text = item.title
            tv_message_single.text = item.shortDescription

            itemView.setOnClickListener {
                listener.invoke(item)
            }
        }
    }

    inner class GroupViewHolder(containerView: View) :
        ChatItemViewHolder(containerView), ItemTouchViewHolder {
        override fun onItemSelected() {
            val color = Utils.getColorFromResource(context, R.attr.selectedItemColor)
            itemView.setBackgroundColor(color)
        }

        override fun onItemCleared() {
            val color = Utils.getColorFromResource(context, R.attr.clearedItemColor)
            itemView.setBackgroundColor(color)
        }

        override fun bind(item: ChatItem, listener: (ChatItem) -> Unit) {
            iv_avatar_group.setInitials(item.title[0].toString())

            with(tv_date_group) {
                visibility = if (item.lastMessageDate != null) View.VISIBLE else View.GONE
                text = item.lastMessageDate
            }

            with(tv_counter_group) {
                visibility = if (item.messageCount > 0) View.VISIBLE else View.GONE
                text = item.messageCount.toString()
            }

            tv_title_group.text = item.title
            tv_message_group.text = item.shortDescription

            with(tv_message_author) {
                visibility = if (item.messageCount > 0) View.VISIBLE else View.GONE
                text = "@${item.author}"
            }

            itemView.setOnClickListener {
                listener.invoke(item)
            }
        }
    }

    inner class ArchiveViewHolder(containerView: View) :
        ChatItemViewHolder(containerView) {

        override fun bind(item: ChatItem, listener: (ChatItem) -> Unit) {
            with(tv_date_archive) {
                visibility = if (item.lastMessageDate != null) View.VISIBLE else View.GONE
                text = item.lastMessageDate
            }

            with(tv_counter_archive) {
                visibility = if (item.messageCount > 0) View.VISIBLE else View.GONE
                text = item.messageCount.toString()
            }

            tv_message_archive.text = item.shortDescription

            with(tv_message_author_archive) {
                visibility = if (item.messageCount > 0) View.VISIBLE else View.GONE
                text = "@${item.author}"
            }

            itemView.setOnClickListener {
                listener.invoke(item)
            }
        }
    }
}