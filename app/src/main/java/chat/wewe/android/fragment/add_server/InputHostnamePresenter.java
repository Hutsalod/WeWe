package chat.wewe.android.fragment.add_server;

import chat.wewe.android.BackgroundLooper;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.rest.DefaultServerPolicyApi;
import chat.wewe.android.api.rest.ServerPolicyApi;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.helper.OkHttpHelper;
import chat.wewe.android.helper.ServerPolicyApiValidationHelper;
import chat.wewe.android.helper.ServerPolicyHelper;
import chat.wewe.android.service.ConnectivityManagerApi;
import chat.wewe.android.shared.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class InputHostnamePresenter extends BasePresenter<InputHostnameContract.View> implements InputHostnameContract.Presenter {
  private final ConnectivityManagerApi connectivityManager;
  private boolean isValidServerUrl;

  public InputHostnamePresenter(ConnectivityManagerApi connectivityManager) {
    this.connectivityManager = connectivityManager;
  }

  @Override
  public void connectTo(final String hostname) {
    view.showLoader();
    connectToEnforced(ServerPolicyHelper.enforceHostname(hostname));
  }

  public void connectToEnforced(final String hostname) {
    final ServerPolicyApi serverPolicyApi = new DefaultServerPolicyApi(OkHttpHelper.INSTANCE.getClientForUploadFile(), hostname);
    final ServerPolicyApiValidationHelper validationHelper = new ServerPolicyApiValidationHelper(serverPolicyApi);

    clearSubscriptions();

    final Disposable subscription = ServerPolicyHelper.isApiVersionValid(validationHelper)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .doOnTerminate(() -> view.hideLoader(isValidServerUrl))
        .subscribe(
            serverValidation -> {

                isValidServerUrl=true;
                onServerValid(hostname, true);

            },
            throwable -> {
              Logger.INSTANCE.report(throwable);
              onServerValid(hostname, true);
              view.showConnectionError();
            });
    addSubscription(subscription);
  }

  private void onServerValid(String hostname, boolean usesSecureConnection) {
    RocketChatCache.INSTANCE.setSelectedServerHostname(hostname);

    String server = hostname.replace("/", ".");
    connectivityManager.addOrUpdateServer(server, server, !usesSecureConnection);
    connectivityManager.keepAliveServer();

    view.showHome();
  }
}