package chat.wewe.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import chat.wewe.android.R;
import chat.wewe.android.fragment.add_server.InputHostnameFragment;

public class AddServerActivity extends AbstractFragmentActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.simple_screen);
    showFragment(new InputHostnameFragment());
  }

  @Override
  protected int getLayoutContainerForFragment() {
    return R.id.content;
  }

  @Override
  protected void onBackPressedNotHandled() {
    moveTaskToBack(true);
  }
}
