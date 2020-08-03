package chat.wewe.android.service;

import io.reactivex.Single;

public interface ConnectivityServiceInterface {
  Single<Boolean> ensureConnectionToServer(String hostname);

  Single<Boolean> disconnectFromServer(String hostname);
}
