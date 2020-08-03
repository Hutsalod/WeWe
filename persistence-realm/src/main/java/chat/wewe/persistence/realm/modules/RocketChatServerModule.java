package chat.wewe.persistence.realm.modules;

import chat.wewe.persistence.realm.models.RealmBasedServerInfo;
import io.realm.annotations.RealmModule;

@RealmModule(library = true, classes = {RealmBasedServerInfo.class})
public class RocketChatServerModule {
}
