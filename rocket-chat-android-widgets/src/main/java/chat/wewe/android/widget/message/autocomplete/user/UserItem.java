package chat.wewe.android.widget.message.autocomplete.user;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import chat.wewe.android.widget.AbsoluteUrl;
import chat.wewe.android.widget.helper.UserStatusProvider;
import chat.wewe.android.widget.message.autocomplete.AutocompleteItem;
import chat.wewe.core.models.SpotlightUser;

public class UserItem implements AutocompleteItem {

  private final SpotlightUser user;
  private final AbsoluteUrl absoluteUrl;
  private final UserStatusProvider userStatusProvider;

  public UserItem(@NonNull SpotlightUser user, AbsoluteUrl absoluteUrl,
                  UserStatusProvider userStatusProvider) {
    this.user = user;
    this.absoluteUrl = absoluteUrl;
    this.userStatusProvider = userStatusProvider;
  }

  @NonNull
  @Override
  public String getSuggestion() {
    return user.getUsername();
  }

  public AbsoluteUrl getAbsoluteUrl() {
    return absoluteUrl;
  }

  @DrawableRes
  public int getStatusResId() {
    return userStatusProvider.getStatusResId(user.getStatus());
  }
}
