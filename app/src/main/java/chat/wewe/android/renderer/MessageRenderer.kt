package chat.wewe.android.renderer

import android.view.View
import android.widget.TextView
import chat.wewe.android.R
import chat.wewe.android.helper.DateTime
import chat.wewe.android.widget.AbsoluteUrl
import chat.wewe.android.widget.RocketChatAvatar
import chat.wewe.android.widget.helper.AvatarHelper
import chat.wewe.android.widget.message.RocketChatMessageAttachmentsLayout
import chat.wewe.android.widget.message.RocketChatMessageLayout
import chat.wewe.android.widget.message.RocketChatMessageUrlsLayout
import chat.wewe.core.SyncState
import chat.wewe.core.models.Message
import com.vanniktech.emoji.EmojiTextView

class MessageRenderer(val message: Message, val autoLoadImage: Boolean) {

    /**
     * Show user's avatar image in RocketChatAvatar widget.
     */
    fun showAvatar(rocketChatAvatarWidget: RocketChatAvatar, hostname: String) {
        val username: String? = message.user?.username
        if (username != null) {
            val placeholderDrawable = AvatarHelper.getTextDrawable(username, rocketChatAvatarWidget.context)
            if (message.avatar != null) {
                // Load user's avatar image from Oauth provider URI.
                rocketChatAvatarWidget.loadImage(message.avatar, placeholderDrawable)
            } else {
                rocketChatAvatarWidget.loadImage(AvatarHelper.getUri(hostname, username), placeholderDrawable)
            }
        } else {
            rocketChatAvatarWidget.visibility = View.GONE
        }
    }

    /**
     * Show username in textView.
     */
    fun showUsername(usernameTextView: TextView, subUsernameTextView: TextView?) {
        val username: String? = message.user?.username
        if (username != null) {
            if (message.alias == null) {
                usernameTextView.text = username
            } else {
                usernameTextView.text = message.alias
                if (subUsernameTextView != null) {
                    subUsernameTextView.text = subUsernameTextView.context.getString(R.string.sub_username, username)
                    subUsernameTextView.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Show timestamp or message state in textView.
     */
    fun showTimestampOrMessageState(textView: TextView) {
        when (message.syncState) {
           // SyncState.SYNCING -> textView.text = textView.context.getText(R.string.sending)
            SyncState.SYNCING -> textView.text = ""
            SyncState.NOT_SYNCED -> textView.text = textView.context.getText(R.string.not_synced)
            SyncState.FAILED -> textView.text = textView.context.getText(R.string.failed_to_sync)
            else -> textView.text = DateTime.fromEpocMs(message.timestamp, DateTime.Format.TIME)
        }
    }

    /**
     * Show body in RocketChatMessageLayout widget.
     */
    fun showBody(rocketChatMessageLayout: RocketChatMessageLayout) {
        rocketChatMessageLayout.setText(message.message)
    }

    /**
     * Show urls in RocketChatMessageUrlsLayout widget.
     */
    fun showUrl(rocketChatMessageUrlsLayout: RocketChatMessageUrlsLayout) {
        val webContents = message.webContents
        if (webContents == null || webContents.isEmpty()) {
            rocketChatMessageUrlsLayout.visibility = View.GONE
        } else {
            rocketChatMessageUrlsLayout.setUrls(webContents, autoLoadImage)
            rocketChatMessageUrlsLayout.visibility = View.VISIBLE
        }
    }

    /**
     * show attachments in RocketChatMessageAttachmentsLayout widget.
     */
    fun showAttachment(rocketChatMessageAttachmentsLayout: RocketChatMessageAttachmentsLayout, absoluteUrl: AbsoluteUrl?) {
        val attachments = message.attachments
        if (attachments == null || attachments.isEmpty()) {
            rocketChatMessageAttachmentsLayout.visibility = View.GONE
        } else {
            rocketChatMessageAttachmentsLayout.setAbsoluteUrl(absoluteUrl)
            rocketChatMessageAttachmentsLayout.setAttachments(attachments, autoLoadImage)
            rocketChatMessageAttachmentsLayout.visibility = View.VISIBLE

            rocketChatMessageAttachmentsLayout
        }
    }
}