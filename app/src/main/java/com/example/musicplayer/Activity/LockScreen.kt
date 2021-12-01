package com.example.musicplayer.Activity

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.Extensions.ImageSaver
import com.example.musicplayer.Extensions.musicSrv
import com.example.musicplayer.Extensions.poss
import com.example.musicplayer.Extensions.shuff
import com.example.musicplayer.Model.Songdetails
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SwipeListener
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_lock_screen.*

class LockScreen : AppCompatActivity(){
    var songList:List<Songdetails>?=null
    var sname:String?=null
    var sart:String?=null
    var image: Bitmap?=null
    var dur:Int?=null
    var time:Int?=null
    var realm: Realm?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        val mUIFlag = ( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_VISIBLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        window.decorView.systemUiVisibility = mUIFlag
        songdetly.setBackgroundResource(R.mipmap.spiderman1)
        realm = Realm.getDefaultInstance()
        if(MainActivity.getInstance()!!.result!=null){
            songList = MainActivity.getInstance()!!.result
        }
        else {
            var query = realm!!.where(Songdetails::class.java)
            songList = query.findAll().sort("songname", Sort.ASCENDING)
        }

        //val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        val playSong = songList!!.get(poss!!)
        sname = playSong.songname
        sart = playSong.songartist
        image = ImageSaver(this@LockScreen).setFileName(playSong.songimage!!).setDirectoryName("images").load()
        fetchimage()
        song_name.text = sname
        song_artist.text = sart
        imgplps.setOnClickListener({
            if(MainActivity.getInstance()!!.isPlaying)
            {
                MainActivity.getInstance()!!.pause()
                setplaybutton()
                //timer.resetCountdownTimer()
            }
            else
            {
                MainActivity.getInstance()!!.start()
                setpausebutton()
            }
        })
        imgnext.setOnClickListener({
           // MainActivity.getInstance()!!.playNext()
        })
        imgprev.setOnClickListener({
            //MainActivity.getInstance()!!.playPrev()
        })
        if( musicSrv.isPlaying) {
            setpausebutton()
        }
        else
        {
            setplaybutton()
        }
        songdetly.setOnTouchListener(object : SwipeListener(this@LockScreen) {

            override fun onSwipeRight() {
               // MainActivity.getInstance()!!.playPrev()
            }

            override fun onSwipeLeft() {
                //MainActivity.getInstance()!!.playNext()
            }

        })

    }


    fun updateui()
    {
        val playSong = songList!!.get(poss!!)
        sname = playSong.songname
        sart = playSong.songartist
        image = ImageSaver(this@LockScreen).setFileName(playSong.songimage!!).setDirectoryName("images").load()
        if( shuff!=true) {

        }
        fetchimage()
    }
    fun setplaybutton()
    {
        imgplps.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp)
    }
    fun setpausebutton()
    {
        imgplps.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_24dp)
    }
    fun fetchimage(){
        song_name.text = sname
        song_artist.text = sart
    }

    //endregion

    override fun onStart() {
        super.onStart()
        // Store our shared preference

    }
    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Store our shared preference

    }

    companion object {
    }
}
