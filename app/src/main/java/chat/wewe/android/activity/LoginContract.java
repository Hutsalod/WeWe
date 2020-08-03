package chat.wewe.android.activity;

import chat.wewe.android.shared.BaseContract;

public interface LoginContract {

  interface View extends BaseContract.View {
    void showLogin(String hostname);

    void showRetryLogin(String hostname);

    void closeView();
  }

  interface Presenter extends BaseContract.Presenter<View> {
  }
}
