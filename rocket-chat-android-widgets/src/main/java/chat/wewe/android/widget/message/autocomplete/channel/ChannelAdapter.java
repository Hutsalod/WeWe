package chat.wewe.android.widget.message.autocomplete.channel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import chat.wewe.android.widget.R;
import chat.wewe.android.widget.message.autocomplete.AutocompleteAdapter;

public class ChannelAdapter extends AutocompleteAdapter<ChannelItem, ChannelViewHolder> {

  @Override
  public ChannelViewHolder getViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.autocomplete_channel_view, parent, false);

    return new ChannelViewHolder(view, onClickListener);
  }
}
