package com.example.musicplayer.BluetoothReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.view.KeyEvent
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Extensions.cxt
import com.example.musicplayer.Extensions.playNext
import com.example.musicplayer.Extensions.playPrev


class MediaButtonEventReceiver() : BroadcastReceiver(), Parcelable {
    constructor(parcel: android.os.Parcel) : this() {
    }

    // Constructor is mandatory
    override fun onReceive(context: Context, intent: Intent) {
        val intentAction = intent.getAction()
        //val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        //Log.i("okay", intentAction.toString() + " happended")
        //if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction))
        //{
          //  Log.i("fvfddff", "no media button information")
           // return
        //}
        val event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as KeyEvent
        if (event == null)
        {
            Log.i("dvfdvd", "no keypress")
            return
        }
        else{
            Log.i("key presses",event.toString())
        }
        if(event.action==KeyEvent.ACTION_UP)
        {
        onKeyDown(event.keyCode, event)
        }
       // if (state.equals(TelephonyManager.EXTRA_STATE_RINGING))
        {
            //Phone is ringing
            MainActivity.getInstance()!!.pause()
        }
       // else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
        {
            //Call received
            MainActivity.getInstance()!!.pause()
        }
        //else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE))
        {
            //Call Dropped or rejected
            MainActivity.getInstance()!!.start()
        }
        // other stuff you want to do
    }


    fun onKeyDown(keyCode:Int, event: KeyEvent):Boolean {
        // var clickListener: OnBbtClickListener?=null
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD ->
                // code for fast forward
                return true
            KeyEvent.KEYCODE_MEDIA_NEXT ->
                try {
                    cxt.playNext()
                    MainActivity.getInstance()!!.togplpsbt()
                } catch (e: Exception) {

                }
            KeyEvent.KEYCODE_MEDIA_PLAY ->
                try {
                    MainActivity.getInstance()!!.start()
                    MainActivity.getInstance()!!.togplpsbt()
                } catch (e: Exception) {

                }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS ->
                try
                {
                   cxt.playPrev()
                    MainActivity.getInstance()!!.togplpsbt()
                }
                catch (e: Exception)
                {

                }
            KeyEvent.KEYCODE_MEDIA_REWIND ->
                // code for rewind
                return true
            KeyEvent.KEYCODE_MEDIA_PAUSE ->
                try {
                    MainActivity.getInstance()!!.pause()
                    MainActivity.getInstance()!!.togplpsbt()
                } catch (e: Exception) {
                }
        }
        return false
    }
    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : android.os.Parcelable.Creator<MediaButtonEventReceiver> {
        override fun createFromParcel(parcel: android.os.Parcel): MediaButtonEventReceiver {
            return MediaButtonEventReceiver(parcel)
        }

        override fun newArray(size: Int): Array<MediaButtonEventReceiver?> {
            return arrayOfNulls(size)
        }
    }
}