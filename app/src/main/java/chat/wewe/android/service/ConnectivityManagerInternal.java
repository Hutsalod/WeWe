package chat.wewe.android.service;

import java.util.List;

import chat.wewe.core.models.ServerInfo;

/**
 * interfaces used for RocketChatService and RocketChatwebSocketThread.
 */
/*package*/ interface ConnectivityManagerInternal {

  void resetConnectivityStateList();

  void ensureConnections();

  List<ServerInfo> getServerList();

  ServerInfo getServerInfoForHost(String hostname);

  void notifyConnectionEstablished(String hostname, String session);

  void notifyConnectionLost(String hostname, int reason);

  void notifyConnecting(String hostname);
}
