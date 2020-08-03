package chat.wewe.android.fragment.chatroom

import chat.wewe.android.R

class HomeFragment : AbstractChatRoomFragment() {

    override fun getLayout(): Int {
        return R.layout.fragment_home
    }

    override fun onSetupView() {
        setToolbarTitle(getText(R.string.home_fragment_title))
    }
}