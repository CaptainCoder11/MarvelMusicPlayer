package com.example.musicplayer.BluetoothReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicplayer.Activity.MainActivity


class PlayButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var action = intent.extras!!.get("do_action") as String
        if (action != null) {
            when (action) {
                "play" -> {
                    if (MainActivity.getInstance()!!.isPlaying) {
                        MainActivity.getInstance()!!.pause()
                    } else {
                        MainActivity.getInstance()!!.start()
                    }

                }
            }
        }
    }
}