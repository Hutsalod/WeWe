package chat.wewe.persistence.realm;

import java.util.HashMap;

import chat.wewe.persistence.realm.modules.RocketChatLibraryModule;
import chat.wewe.persistence.realm.modules.RocketChatServerModule;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmStore {
  public static HashMap<String, RealmConfiguration> sStore = new HashMap<>();

  private static RealmConfiguration createConfigFor(String name) {
    return new RealmConfiguration.Builder()
        .name(name + ".realm")
        .modules(new RocketChatLibraryModule())
        .migration(new Migration())
        .schemaVersion(7)
            .deleteRealmIfMigrationNeeded()
        .build();
  }

  public static void put(String name) {
    sStore.put(name, createConfigFor(name));
  }

  public static RealmHelper getDefault() {
    return new RealmHelper();
  }

  public static RealmHelper get(String name) {
    if (!sStore.containsKey(name)) {
      return null;
    }

    return new RealmHelper(sStore.get(name));
  }

  public static RealmHelper getOrCreate(String name) {
    if (!sStore.containsKey(name)) {
      put(name);
    }
    return new RealmHelper(sStore.get(name));
  }

  public static RealmHelper getOrCreateForServerScope(String name) {
    if (!sStore.containsKey(name)) {
      sStore.put(name, new RealmConfiguration.Builder()
          .name(name + ".realm")
          .modules(new RocketChatServerModule())
              .deleteRealmIfMigrationNeeded().build());
    }
    return new RealmHelper(sStore.get(name));
  }

  public static Realm getRealm(String name) {
    RealmHelper realmHelper = get(name);

    if (realmHelper == null) {
      return null;
    }

    return realmHelper.instance();
  }
}
