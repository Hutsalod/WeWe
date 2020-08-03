package chat.wewe.android.fragment.server_config;

import chat.wewe.android.shared.BaseContract;

public interface TwoStepAuthContract {

  interface View extends BaseContract.View {

    void showLoading();

    void hideLoading();

    void showError(String message);
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onCode(String twoStepAuthCode);
  }
}
