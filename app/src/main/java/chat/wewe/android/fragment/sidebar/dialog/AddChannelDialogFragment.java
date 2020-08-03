package chat.wewe.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.helper.TextUtils;

/**
 * add Channel, add Private-group.
 */
public class AddChannelDialogFragment extends AbstractAddRoomDialogFragment {

  boolean isPrivate;

  public AddChannelDialogFragment() {
  }



  public static AddChannelDialogFragment create(String hostname) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);

    AddChannelDialogFragment fragment = new AddChannelDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  protected int getLayout() {
    return R.layout.dialog_add_channel;
  }

  @SuppressLint("RxLeakedSubscription")
  @Override
  protected void onSetupDialog() {
    Button buttonAddChannel = getDialog().findViewById(R.id.btn_add_channel);
    TextInputEditText channelNameText = (TextInputEditText) getDialog().findViewById(R.id.editor_channel_name);
    TextView channelNameId = (TextView) getDialog().findViewById(R.id.editor_channel_id);

    String symbols = "weid1234qaz";
    StringBuilder randString = new StringBuilder();
    for(int i=0;i<10;i++)
      randString.append(symbols.charAt((int)(Math.random()*symbols.length())));

    channelNameId.setText(""+randString);

    RxTextView.textChanges((TextView) channelNameText)
            .map(text -> !TextUtils.isEmpty(text))
            .compose(bindToLifecycle())
            .subscribe(
                buttonAddChannel::setEnabled,
                Logger.INSTANCE::report
            );
    Log.d("TEST_WEWE","yy");
    buttonAddChannel.setOnClickListener(view -> createRoom());

  }
  
  private boolean isChecked(int viewId) {
    CompoundButton check = (CompoundButton) getDialog().findViewById(viewId);
    return check.isChecked();
  }

  @Override
  protected Task<Void> getMethodCallForSubmitAction() {
    TextView channelNameText = (TextView) getDialog().findViewById(R.id.editor_channel_name);
    TextView channelNameId = (TextView) getDialog().findViewById(R.id.editor_channel_id);
    String channelName = channelNameText.getText().toString();
    String channelId = channelNameId.getText().toString();
    boolean isReadOnly = isChecked(R.id.checkbox_read_only);

    if (isPrivate) {
      return methodCall.createPrivateGroup(channelId,channelName, isReadOnly);
    } else {
      return methodCall.createChannel(channelId, channelName,isReadOnly);
    }
  }
}
