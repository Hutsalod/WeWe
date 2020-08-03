package chat.wewe.android.fragment.add_server;

import chat.wewe.android.shared.BaseContract;

public interface InputHostnameContract {

  interface View extends BaseContract.View {
    void showLoader();

    void hideLoader(Boolean isValidServerUrl);

    void showInvalidServerError();

    void showConnectionError();

    void showHome();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void connectTo(String hostname);
  }

}
