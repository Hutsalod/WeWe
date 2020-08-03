package chat.wewe.android.widget.message.autocomplete.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import chat.wewe.android.widget.R;
import chat.wewe.android.widget.message.autocomplete.AutocompleteAdapter;

public class UserAdapter extends AutocompleteAdapter<UserItem, UserViewHolder> {

  @Override
  public UserViewHolder getViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.autocomplete_user_view, parent, false);

    return new UserViewHolder(view, onClickListener);
  }
}