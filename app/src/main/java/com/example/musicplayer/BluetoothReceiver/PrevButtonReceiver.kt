package com.example.musicplayer.BluetoothReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicplayer.Extensions.cxt
import com.example.musicplayer.Extensions.playPrev

class PrevButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var action = intent.extras!!.get("do_action") as String
        if (action != null) {
            when (action) {
                "prev" -> {
                    cxt.playPrev()
                }
            }
        }
    }
}