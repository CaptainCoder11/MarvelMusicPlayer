package com.example.musicplayer.Activity

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import java.lang.Exception

class NotifyprevHandler : Activity() {
    val PERFORM_NOTIFICATION_BUTTON = "perform_notification_button"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent.extras!!.get("do_action") as String

        if (action != null) {
            if (action == "prev") {
                   // MainActivity.getInstance()!!.playPrev()
            }

        }
        finish()
    }
}
