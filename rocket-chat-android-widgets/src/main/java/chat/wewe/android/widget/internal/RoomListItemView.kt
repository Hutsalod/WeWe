package chat.wewe.android.widget.internal

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import chat.wewe.android.widget.R
import chat.wewe.android.widget.helper.AvatarHelper.getTextDrawable
import chat.wewe.android.widget.helper.DrawableHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.room_list_item.view.*

/**
 * Room list-item view used in sidebar.
 */
class RoomListItemView : FrameLayout {
    lateinit private var roomId: String
    private val privateChannelDrawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_chanel_vector_room, null)
    private val publicChannelDrawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_public_chanel_vector_room, null)
    private val liveChatChannelDrawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_livechat_white_24dp, null)
    private val userStatusDrawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_user_status_black_24dp, null)?.mutate()

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(context)
    }

    private fun initialize(context: Context) {
        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        val array = context
                .theme
                .obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackground))

        background = array.getDrawable(0)
        array.recycle()

        View.inflate(context, R.layout.room_list_item, this)
    }

    fun setRoomId(roomId: String) {
        this.roomId = roomId
    }

    fun setUnreadCount(count: Int) {
        if (count > 0) {
            alertCount.text = count.toString()
            alertCount.visibility = View.VISIBLE
        } else {
            alertCount.visibility = View.GONE
        }
    }

    fun setAlert(alert: Boolean) {

    }



    fun setRoomName(roomName: String) {
        name.text = roomName

        val placeholderDrawable = getTextDrawable(roomName, context)
        avatar.loadImage("https://chat.weltwelle.com/avatar/$roomName", placeholderDrawable)

    }



    fun setRoomTime(time : String ){
        timeText.setText(time)
    }

    fun setRoomLastMessage(msg : String ){
        message_out.setText(msg)
    }


    fun showPrivateChannelIcon() {
        type.setImageDrawable(privateChannelDrawable)
        prepareDrawableAndShow(R.color.color_user_status_offline)
        userStatus.visibility = View.GONE
       type.visibility = View.VISIBLE
    }

    fun showPublicChannelIcon() {
        type.setImageDrawable(privateChannelDrawable)
        prepareDrawableAndShow(R.color.color_user_status_offline)
        userStatus.visibility = View.GONE
        type.visibility = View.VISIBLE
    }

    fun showLivechatChannelIcon() {
     //   type.setImageDrawable(liveChatChannelDrawable)
     //   userStatus.visibility = View.GONE
      //  type.visibility = View.VISIBLE
    }

    fun showOnlineUserStatusIcon() {
        prepareDrawableAndShow(R.color.color_user_status_online)
        name.alpha = 1.0F
    }

    fun showBusyUserStatusIcon() {
        prepareDrawableAndShow(R.color.color_user_status_busy)
    }

    fun showAwayUserStatusIcon() {
        prepareDrawableAndShow(R.color.color_user_status_away)
    }

    fun showOfflineUserStatusIcon() {
        prepareDrawableAndShow(R.color.color_user_status_offline)
        name.alpha = 0.9F
    }

    private fun prepareDrawableAndShow(@ColorRes resId: Int) {
        DrawableHelper.wrapDrawable(userStatusDrawable)
        DrawableHelper.tintDrawable(userStatusDrawable, context, resId)
        userStatus.setImageDrawable(userStatusDrawable)
        type.visibility = View.GONE
        userStatus.visibility = View.VISIBLE
    }



}