package chat.wewe.android.extensions

import chat.wewe.android.BuildConfig

fun Throwable.printStackTraceOnDebug() {
    if (BuildConfig.DEBUG) {
        this.printStackTrace()
    }
}