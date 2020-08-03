package chat.wewe.android.fragment.chatroom;

import chat.wewe.android.widget.AbsoluteUrl;
import chat.wewe.core.models.ServerInfo;
import chat.wewe.core.models.Session;
import chat.wewe.core.models.User;

public class RocketChatAbsoluteUrl implements AbsoluteUrl {

  private final String baseUrl;
  private final String userId;
  private final String token;

  public RocketChatAbsoluteUrl(ServerInfo info, User user, Session session) {
    baseUrl = (info.isSecure() ? "https://" : "http://") + info.getHostname();
    userId = user.getId();
    token = session.getToken();
  }

  @Override
  public String from(String url) {
    return url.startsWith("/") ? baseUrl + url + "?rc_uid=" + userId + "&rc_token=" + token : url;
  }

  public String getUserId() {
    return userId;
  }

  public String getToken() {
    return token;
  }

  public String getBaseUrl() {
    return baseUrl;
  }
}