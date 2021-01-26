package chat.wewe.android.layouthelper.chatroom;

import android.view.View;

import com.vanniktech.emoji.EmojiTextView;

import chat.wewe.android.R;
import chat.wewe.android.renderer.MessageRenderer;
import chat.wewe.android.widget.AbsoluteUrl;
import chat.wewe.android.widget.message.RocketChatMessageAttachmentsLayout;
import chat.wewe.android.widget.message.RocketChatMessageLayout;
import chat.wewe.android.widget.message.RocketChatMessageUrlsLayout;

/**
 * ViewData holder of NORMAL chat message.
 */
public class MessageNormalViewHolder extends AbstractMessageViewHolder {
  private final RocketChatMessageLayout body;
  private final RocketChatMessageUrlsLayout urls;
  private final RocketChatMessageAttachmentsLayout attachments;


  /**
   * constructor WITH hostname.
   */
  public MessageNormalViewHolder(View itemView, AbsoluteUrl absoluteUrl, String hostname) {
    super(itemView, absoluteUrl, hostname);
    body = itemView.findViewById(R.id.message_body);
    urls = itemView.findViewById(R.id.message_urls);
    attachments = itemView.findViewById(R.id.message_attachments);
  }

  @Override
  protected void bindMessage(PairedMessage pairedMessage, boolean autoloadImages) {
    MessageRenderer messageRenderer = new MessageRenderer(pairedMessage.target, autoloadImages);
    messageRenderer.showAvatar(avatar, hostname);
    messageRenderer.showUsername(username, subUsername);
    messageRenderer.showTimestampOrMessageState(timestamp);
    messageRenderer.showBody(body);
    messageRenderer.showUrl(urls);
    messageRenderer.showAttachment(attachments, absoluteUrl);


  }
}
