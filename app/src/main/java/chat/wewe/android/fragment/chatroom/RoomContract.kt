package chat.wewe.android.fragment.chatroom

import chat.wewe.android.shared.BaseContract
import chat.wewe.android.widget.AbsoluteUrl
import chat.wewe.core.models.Message
import chat.wewe.core.models.Room
import chat.wewe.core.models.User

interface RoomContract {

    interface View : BaseContract.View {

        fun setupWith(rocketChatAbsoluteUrl: RocketChatAbsoluteUrl)

        fun render(room: Room)

        fun showUserStatus(user: User)

        fun updateHistoryState(hasNext: Boolean, isLoaded: Boolean)

        fun onMessageSendSuccessfully()

        fun disableMessageInput()

        fun enableMessageInput()

        fun showUnreadCount(count: Int)

        fun showMessages(messages: List<Message>)

        fun showMessageSendFailure(message: Message)

        fun showMessageDeleteFailure(message: Message)

        fun autoloadImages()

        fun manualLoadImages()

        fun onReply(absoluteUrl: AbsoluteUrl, markdown: String, message: Message)

        fun onCopy(message: String)

        fun showMessageActions(message: Message)
    }

    interface Presenter : BaseContract.Presenter<View> {

        fun loadMessages()

        fun loadMoreMessages()

        fun onMessageSelected(message: Message?)

        fun onMessageTap(message: Message?)

        fun sendMessage(messageText: String)

        fun resendMessage(message: Message)

        fun updateMessage(message: Message, content: String)

        fun deleteMessage(message: Message)

        fun onUnreadCount()

        fun onMarkAsRead()

        fun refreshRoom()

        fun replyMessage(message: Message, justQuote: Boolean)

        fun acceptMessageDeleteFailure(message: Message)

        fun loadMissedMessages()
    }
}
