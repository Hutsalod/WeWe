package chat.wewe.android.push.gcm

import bolts.Task
import chat.wewe.android.R
import chat.wewe.android.RocketChatApplication
import chat.wewe.android.RocketChatCache
import chat.wewe.android.api.RaixPushHelper
import chat.wewe.persistence.realm.RealmHelper
import chat.wewe.persistence.realm.models.ddp.RealmUser
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import java.io.IOException

object GcmPushHelper {

    fun getGcmToken(): String? = getGcmToken(getSenderId())

    @Throws(IOException::class)
    private fun registerGcmTokenForServer(realmHelper: RealmHelper): Task<Void> {
        val gcmToken = getGcmToken(getSenderId())
        val currentUser = realmHelper.executeTransactionForRead({ realm -> RealmUser.queryCurrentUser(realm).findFirst() })
        val userId = if (currentUser != null) currentUser.getId() else null
        val pushId = RocketChatCache.getOrCreatePushId()

        return RaixPushHelper(realmHelper)
                .pushUpdate(pushId!!, gcmToken, userId)
    }

    @Throws(IOException::class)
    private fun getGcmToken(senderId: String): String {
        return InstanceID.getInstance(RocketChatApplication.getInstance())
                .getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)
    }

    private fun getSenderId(): String {
        return RocketChatApplication.getInstance().getString(R.string.gcm_sender_id)
    }
}