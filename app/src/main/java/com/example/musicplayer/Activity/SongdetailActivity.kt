package com.example.musicplayer.Activity

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import android.util.Log
import kotlinx.android.synthetic.main.activity_songdetail.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.azoft.carousellayoutmanager.CarouselLayoutManager
import com.example.musicplayer.adapter.CoverFlowAdapter
import com.example.musicplayer.adapter.SwipeListener
import com.mbh.timelyview.TimelyShortTimeView
import io.realm.Realm
import io.realm.Sort
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.*
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.lang.Runnable
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener
import com.azoft.carousellayoutmanager.CenterScrollListener
import com.example.musicplayer.Extensions.*
import com.example.musicplayer.Model.Songdetails
import com.example.musicplayer.R


class SongdetailActivity : AppCompatActivity(){
    var songList:List<Songdetails>?=null
    var sname:String?=null
    var sart:String?=null
    var url:String?=null
    var image:Bitmap?=null
    var dur:Int?=null
    var time:Int?=null
    var realm:Realm?=null
    override fun onResume() {
        super.onResume()
        coverflow.isNestedScrollingEnabled = false
        if(nexturl!=null) {
            coverflow.scrollToPosition(nexturl!!)
        }
        else{
            coverflow.scrollToPosition(poss!!)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songdetail)
        ins = this
        val extras = intent.extras
        ttv.setTextColor(resources.getColor(R.color.blue))
        ttv.timeFormat = TimelyShortTimeView.FORMAT_MIN_SEC
        realm = Realm.getDefaultInstance()
        if(MainActivity.getInstance()!!.result!=null)
        {
            songList = MainActivity.getInstance()!!.result
        }
        else
        {
            var query = realm!!.where(Songdetails::class.java)
            songList = query.findAll().sort("songname", Sort.ASCENDING)
        }

        //val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        var playSong: Songdetails?=null
        if(nexturl!=null) {
            playSong =  songList!!.get(nexturl!!)
        }
        else{
            playSong =  songList!!.get(poss!!)
        }
        sname = playSong.songname
        sart = playSong.songartist
        url = playSong.songurl
        image = getimage(this,url!!)
        var bool = MainActivity.getInstance()!!.getloop()
        when(bool)
        {
            true -> loop.setColorFilter(ContextCompat.getColor(this, R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN)
            false -> loop.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        loop.setOnClickListener {
         var bool = MainActivity.getInstance()!!.getloop()
         when(bool)
         {
             true -> removeloop()
             false -> setloop()
         }

        }
        fetchimage()
        song_name.font("AmericanKestrel-lyZd.otf")
        song_artist.font("BlackWidowMovie-d95Rg.ttf")
        song_name.isSelected = true
        song_name.text = sname
        song_artist.text = sart
        setanimation()
        val layoutManager = CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL)
        layoutManager.setPostLayoutListener(CarouselZoomPostLayoutListener())
            madapter = CoverFlowAdapter(this@SongdetailActivity, songList!!)
            coverflow.layoutManager = layoutManager
            coverflow.addOnScrollListener(CenterScrollListener())

        coverflow.adapter = madapter
        imgplps.setOnClickListener({
            if(MainActivity.getInstance()!!.isPlaying)
            {
                MainActivity.getInstance()!!.pause()
                //timer.resetCountdownTimer()
            }
            else
            {
                MainActivity.getInstance()!!.start()
            }
        })
        imgnext.setOnClickListener({
            MainActivity.getInstance()!!.playNext()
        })
        imgprev.setOnClickListener({
          MainActivity.getInstance()!!.playPrev()
        })
        if( musicSrv.isPlaying) {
            setpausebutton()
        }
        else
        {
            setplaybutton()
        }
        songdetly.setOnTouchListener(object : SwipeListener(this@SongdetailActivity) {

            override fun onSwipeRight() {
               MainActivity.getInstance()!!.playPrev()
            }

            override fun onSwipeLeft() {
               MainActivity.getInstance()!!.playNext()
            }

        })
        upprogress()
    }
    fun setloop(){
        MainActivity.getInstance()!!.setloop(true)
        loop.setColorFilter(ContextCompat.getColor(this, R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN)
    }
    fun removeloop(){
        MainActivity.getInstance()!!.setloop(false)
        loop.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
    }
    fun updateui()
    {
        var playSong: Songdetails?=null
        if(nexturl!=null) {
            playSong =  songList!!.get(nexturl!!)
        }
        else{
            playSong =  songList!!.get(poss!!)
        }
        sname = playSong.songname
        sart = playSong.songartist
        url = playSong.songurl
        image = getimage(this,url!!)

            if(nexturl!=null) {
                coverflow.scrollToPosition(nexturl!!)
            }
            else{
                coverflow.scrollToPosition(poss!!)
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

                songdetbackg.background = null
                Blurry.with(this).radius(10).sampling(10).from(image).into(songdetbackg)
                song_image.setImageBitmap(image)
                song_name.text = sname
                song_artist.text = sart
    }
    fun upprogress() {
        val handler = Handler()
        var moveSeekBartimerthread:Runnable?=null
        var moveSeekBarThread = object : Runnable {
            override fun run() {

                if (MainActivity.getInstance()!!.isPlaying) {
                    val mediaPos_new = MainActivity.getInstance()!!.currentPosition
                    val mediaMax_new = MainActivity.getInstance()!!.duration
                    detailseekbar!!.max=mediaMax_new.toFloat() // Set the Maximum range of the
                    detailseekbar!!.progress =mediaPos_new.toFloat()// set current progress to song's
                    handler.postDelayed(this, 100) //Looping the thread after 0.1 second
                    time= mediaPos_new
                    ttv.setTime(time!!.toLong())

                }


            }
        }
        var mediaPos = MainActivity.getInstance()!!.currentPosition
       var mediaMax:Int?=null
        if(MainActivity.getInstance()!!.duration!=0)
        {
            mediaMax = MainActivity.getInstance()!!.duration
            Log.i("checkthis:",mediaMax.toString())
            time= mediaPos
            ttv.setTime(time!!.toLong())
            detailseekbar!!.max= mediaMax!!.toFloat() // Set the Maximum range of the
            detailseekbar!!.progress =mediaPos.toFloat()// set current progress to song's
            handler.postDelayed(moveSeekBarThread, 100)
        }
        else
        {
           musicSrv.player!!.setOnPreparedListener (object :MediaPlayer.OnPreparedListener{
                override fun onPrepared(mp: MediaPlayer?) {
                    mediaMax = mp!!.duration
                    mp.start()
                    Log.i("checkthis:",mediaMax.toString())
                    time=mediaPos
                    ttv.setTime(time!!.toLong())
                    detailseekbar!!.max= mediaMax!!.toFloat() // Set the Maximum range of the
                    detailseekbar!!.progress =mediaPos.toFloat()// set current progress to song's
                     moveSeekBartimerthread = object : Runnable {
                        override fun run() {

                            if (MainActivity.getInstance()!!.isPlaying) {
                                val mediaPos_new = mp!!.currentPosition
                                val mediaMax_new = mp!!.duration
                                detailseekbar!!.max=mediaMax_new.toFloat() // Set the Maximum range of the
                                detailseekbar!!.progress =mediaPos_new.toFloat()// set current progress to song's
                                handler.postDelayed(this, 100) //Looping the thread after 0.1 second
                                time= mediaPos_new
                                ttv.setTime(time!!.toLong())
                            }
                        }
                    }
                    handler.postDelayed(moveSeekBartimerthread, 100)


                }

            }

            )
        }

        detailseekbar.setOnSeekBarChangeListener(object : CircularSeekBar.OnCircularSeekBarChangeListener{
            override fun onProgressChanged(circularSeekBar: CircularSeekBar?, progress: Float, fromUser: Boolean) {
               if (moveSeekBartimerthread==null) {
                   handler.removeCallbacks(moveSeekBarThread)
               }
                else{
                   handler.removeCallbacks(moveSeekBartimerthread)
               }
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
                if (moveSeekBartimerthread==null)
                {
                    handler.removeCallbacks(moveSeekBarThread)
                }
                else
                {
                    handler.removeCallbacks(moveSeekBartimerthread)
                }
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
                    MainActivity.getInstance()!!.seekTo(seekBar!!.progress.toInt())
                if (moveSeekBartimerthread==null)
                {
                    handler.postDelayed(moveSeekBarThread, 100)
                    upprogress()
                }
                else
                {
                    handler.postDelayed(moveSeekBartimerthread, 100)
                    upprogress()
                }
            }

        })
    }
    //endregion

    fun stopanimation(){
        val rotation = AnimationUtils.loadAnimation(this@SongdetailActivity, R.anim.rotate)
        rotation.repeatCount = Animation.INFINITE
            song_image.clearAnimation()
    }
    fun setanimation()
    {
        if(musicSrv.isPlaying)
        {
                        val rotation = AnimationUtils.loadAnimation(this@SongdetailActivity,
                            R.anim.rotate
                        )
                        rotation.repeatCount = Animation.INFINITE
                        song_image.startAnimation(rotation)
           }

    }
    override fun onStart() {
        super.onStart()
        // Store our shared preference
        val sp = getSharedPreferences("OURINFO", MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("active", true)
        ed.commit()
    }
    override fun onStop() {
        super.onStop()
        // Store our shared preference
        //val sp = getSharedPreferences("OURINFO", MODE_PRIVATE)
        //val ed = sp.edit()
        //ed.putBoolean("active", false)
        //ed.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Store our shared preference
        val sp = getSharedPreferences("OURINFO", MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("active", false)
        ed.commit()
        finish()
    }

    companion object {
        var madapter:CoverFlowAdapter?=null
        var ins: SongdetailActivity? = null
        fun getInstance(): SongdetailActivity? {
            return ins
        }
    }
}
