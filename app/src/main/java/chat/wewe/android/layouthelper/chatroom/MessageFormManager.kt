package chat.wewe.android.layouthelper.chatroom

import android.util.Log
import chat.wewe.android.widget.AbsoluteUrl
import chat.wewe.android.widget.message.MessageFormLayout
import chat.wewe.core.models.Message
import java.util.*

class MessageFormManager(private val messageFormLayout: MessageFormLayout, val callback: MessageFormLayout.ExtraActionSelectionClickListener) {
    private var sendMessageCallback: SendMessageCallback? = null
    private var replyMarkDown: String = ""
    private var roomName: String = ""
    var mnameBlack = arrayOf("id")
    init {
        messageFormLayout.setExtraActionSelectionClickListener(callback)
        messageFormLayout.setSubmitTextListener(this::sendMessage)
        messageFormLayout.setBlocingUsers { this.sendBlocking() }

       // if (Arrays.asList<String>(*mnameBlack).contains(nameRoom)) messageFormLayout.setBlocing(true) else messageFormLayout.setBlocing(false)
        println("other message"+roomName)
    }

    fun setSendMessageCallback(sendMessageCallback: SendMessageCallback) {
        this.sendMessageCallback = sendMessageCallback
    }

    fun getRoomName(roomName: String) {
        this.roomName = roomName
        println("other message"+roomName)
    }


    fun onMessageSend() {
        clearComposingText()
    }

    fun setEditMessage(message: String) {
        clearComposingText()
        messageFormLayout.setText(message)
    }

    fun clearComposingText() {
        messageFormLayout.setText("")
    }

    fun enableComposingText(enable: Boolean) {
        messageFormLayout.isEnabled = enable
    }

    fun setReply(absoluteUrl: AbsoluteUrl, replyMarkDown: String, message: Message) {
        this.replyMarkDown = replyMarkDown
        messageFormLayout.setReplyContent(absoluteUrl, message)
        messageFormLayout.setReplyCancelListener({
            this.replyMarkDown = ""
            messageFormLayout.clearReplyContent()
            messageFormLayout.hideKeyboard()
        })
    }

    private fun sendMessage(message: String) {
        val finalMessage = if (replyMarkDown.isNotEmpty()) "$replyMarkDown $message" else message
        replyMarkDown = ""
        sendMessageCallback?.onSubmitText(finalMessage)
    }

    interface SendMessageCallback {
        fun onSubmitText(messageText: String)
    }

    private fun sendBlocking() {
        Log.d("QAZX", "TRUE")
    //    SettingActivity().UF_ROCKET_LOGIN_BLOC(getName)
    }
}