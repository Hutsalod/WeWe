package chat.wewe.android.service.internal;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import chat.wewe.android.RocketChatCache;
import chat.wewe.android.service.Registrable;
import chat.wewe.android.service.ddp.stream.StreamRoomMessage;
import chat.wewe.persistence.realm.RealmHelper;

/**
 * wrapper for managing stream-notify-message depending on RocketChatCache.
 */
public class StreamRoomMessageManager implements Registrable {
    private final Context context;
    private final String hostname;
    private final RealmHelper realmHelper;
    private final AbstractRocketChatCacheObserver cacheObserver;
    private final Handler handler;
    private StreamRoomMessage streamRoomMessage;

    public StreamRoomMessageManager(Context context, String hostname,
                                    RealmHelper realmHelper) {
        this.context = context;
        this.hostname = hostname;
        this.realmHelper = realmHelper;

        cacheObserver = new AbstractRocketChatCacheObserver(realmHelper) {
            @Override
            protected void onRoomIdUpdated(String roomId) {
                unregisterStreamNotifyMessageIfNeeded();
                registerStreamNotifyMessage(roomId);
            }
        };
        handler = new Handler(Looper.myLooper());
    }

    private void registerStreamNotifyMessage(String roomId) {
        handler.post(() -> {
            streamRoomMessage = new StreamRoomMessage(context, hostname, realmHelper, roomId);
            streamRoomMessage.register();
        });
    }

    private void unregisterStreamNotifyMessageIfNeeded() {
        handler.post(() -> {
            if (streamRoomMessage != null) {
                streamRoomMessage.unregister();
                streamRoomMessage = null;
            }
        });
    }

    @Override
    public void register() {
        cacheObserver.register();
        String selectedRoomId = RocketChatCache.INSTANCE.getSelectedRoomId();
        if (selectedRoomId == null) {
            return;
        }
        registerStreamNotifyMessage(selectedRoomId);
    }

    @Override
    public void unregister() {
        unregisterStreamNotifyMessageIfNeeded();
        cacheObserver.unregister();
    }
}
