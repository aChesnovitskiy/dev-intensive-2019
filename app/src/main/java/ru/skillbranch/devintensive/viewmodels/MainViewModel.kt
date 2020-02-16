package ru.skillbranch.devintensive.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import ru.skillbranch.devintensive.extensions.mutableLiveData
import ru.skillbranch.devintensive.extensions.shortFormat
import ru.skillbranch.devintensive.models.BaseMessage
import ru.skillbranch.devintensive.models.ImageMessage
import ru.skillbranch.devintensive.models.TextMessage
import ru.skillbranch.devintensive.models.data.Chat
import ru.skillbranch.devintensive.models.data.ChatItem
import ru.skillbranch.devintensive.models.data.ChatType
import ru.skillbranch.devintensive.repositories.ChatRepository
import ru.skillbranch.devintensive.utils.Utils

class MainViewModel : ViewModel() {

    private val query = mutableLiveData("")
    private val chatRepository = ChatRepository
    private val chats = Transformations.map(chatRepository.loadChats()){chats ->
        var result = chats.filter { !it.isArchived }
            .map { it.toChatItem() }
            .sortedBy { it.id.toInt() }
            .toMutableList()

        val archiveChats = chats.filter { it.isArchived }

        if (archiveChats.isNotEmpty()) {
            val archiveChatItem = createArchiveChatItem(archiveChats)
            result.add(0, archiveChatItem)
        }

        return@map result
    }

    private fun createArchiveChatItem(chats: List<Chat>): ChatItem {

        var messageCount = 0
        val messages = chats.map { it.messages }
        val lastMessage = if (messages.isNullOrEmpty()) {
            null
        } else {
            messages.filter { !it.isNullOrEmpty() }
                .map { it.last() }
                .maxBy { it.date }
        }

        for (message in messages) {
            messageCount += message.size
        }

        val lastMessageShort = Utils.toMessageShort(lastMessage)

        return ChatItem(
            "archive",
            null,
            "",
            "",
            lastMessageShort.first,
            messageCount,
            lastMessage?.date?.shortFormat(),
            false,
            ChatType.ARCHIVE,
            lastMessageShort.second
        )
    }

    fun getChatData() : LiveData<List<ChatItem>> {
        val result = MediatorLiveData<List<ChatItem>>()

        val filterF = {
            val queryStr = query.value!!
            val chats = chats.value!!

            result.value = if (queryStr.isEmpty()) chats
            else chats.filter { it.title.contains(queryStr, true) }
        }

        result.addSource(chats) { filterF.invoke() }
        result.addSource(query) { filterF.invoke() }

        return result
    }

    fun addToArchive(chatId: String) {
        val chat = chatRepository.find(chatId) ?: return
        chatRepository.update(chat.copy(isArchived = true))
    }

    fun restoreFromArchive(chatId: String) {
        val chat = chatRepository.find(chatId) ?: return
        chatRepository.update(chat.copy(isArchived = false))
    }

    fun handleSearchQuery(text: String?) {
        query.value = text
    }
}