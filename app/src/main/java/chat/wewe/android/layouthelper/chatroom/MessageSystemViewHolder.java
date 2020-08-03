package chat.wewe.android.layouthelper.chatroom;

import android.view.View;
import android.widget.TextView;

import chat.wewe.android.R;
import chat.wewe.android.renderer.MessageRenderer;
import chat.wewe.android.widget.AbsoluteUrl;

/**
 * ViewData holder of NORMAL chat message.
 */
public class MessageSystemViewHolder extends AbstractMessageViewHolder {
  private final TextView body;

  /**
   * constructor WITH hostname.
   */
  public MessageSystemViewHolder(View itemView, AbsoluteUrl absoluteUrl, String hostname) {
    super(itemView, absoluteUrl, hostname);
    body = itemView.findViewById(R.id.message_body);
  }

  @Override
  protected void bindMessage(PairedMessage pairedMessage, boolean autoloadImages) {
    MessageRenderer messageRenderer = new MessageRenderer(pairedMessage.target, autoloadImages);
    messageRenderer.showAvatar(avatar, hostname);
    messageRenderer.showUsername(username, subUsername);
    messageRenderer.showTimestampOrMessageState(timestamp);
    if (pairedMessage.target != null) {
      body.setText(MessageType.parse(pairedMessage.target.getType())
          .getString(body.getContext(), pairedMessage.target));
    }
  }
}
