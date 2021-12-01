package com.example.musicplayer.Service

import android.app.Service
import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log

import com.example.musicplayer.adapter.Songsadapter
import android.provider.MediaStore
import com.example.musicplayer.*
import java.io.File
import java.util.*
import com.example.musicplayer.BluetoothReceiver.MediaButtonEventReceiver
import com.example.musicplayer.Extensions.*
import android.content.IntentFilter
import android.bluetooth.BluetoothDevice
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.SongdetailActivity
import com.example.musicplayer.Model.Songdetails


/*
 * This is demo code to accompany the Mobiletuts+ series:
 * Android SDK: Creating a Music Player
 *
 * Sue Smith - February 2014
 */

class MusicService : Service(),AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    override fun onAudioFocusChange(focusChange: Int) {
        if(focusChange<=0 && player!!.isPlaying) {
           pausePlayer()
        } else {
           go()
        }
    }

    var mreceiver:BroadcastReceiver?=null
    //media player
    var player: MediaPlayer? = null
    //song list
    private var songs: List<Songdetails>? = null
    //current position
    public var songPosn: Int = 0
    //binder
    private val musicBind = MusicBinder()
    //title of current song
    var mAudioManager:AudioManager?=null

    var mReceiverComponent:ComponentName?=null

    private var songTitle = ""
    //shuffle flag and random
    private var shuffle = false
    private var rand: Random? = null

    var previouspos:Int?=null
    val isPlaying: Boolean
        get() = player!!.isPlaying

    //playback methods
    val posn: Int
        get() = player!!.currentPosition

    val dur: Int
        get() = player!!.duration


    val iscomp: Boolean
        get() = player!!.currentPosition>0

    override fun onCreate() {
        //create the service
        super.onCreate()
        //initialize position
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        mReceiverComponent = ComponentName(this, MediaButtonEventReceiver::class.java)
        mAudioManager!!.registerMediaButtonEventReceiver(mReceiverComponent)

        songPosn = 0
        //random
        rand = Random()
        //create player
        player = MediaPlayer()
        //initialize
        initMusicPlayer()

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_ANSWER)
        mreceiver = MyReceiver()
        registerReceiver(mreceiver, filter)
    }
    var disconnectreceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent!!.action)){
                "Disconnection Received".log()
                pausePlayer()
            }

        }

    }
fun setloop(bool:Boolean){
    player!!.isLooping = bool
}
    fun getloop():Boolean{
        return player!!.isLooping
    }
    fun initMusicPlayer() {
        //set player properties
        player!!.setWakeMode(applicationContext,
                PowerManager.PARTIAL_WAKE_LOCK)
        player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        //set listeners
        player!!.setOnPreparedListener(this)
        player!!.setOnCompletionListener(this)
        player!!.setOnErrorListener(this)
    }

    //pass song list
    fun setList(theSongs: List<Songdetails>?) {
        songs = theSongs
    }

    //binder
    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    //activity will bind to service
    override fun onBind(intent: Intent): IBinder? {
        return musicBind
    }

    //release resources when unbind
    override fun onUnbind(intent: Intent): Boolean {
        player!!.stop()
        player!!.release()
        return false
    }

    //play a song
    fun playSong() {
        //play
        player!!.reset()
        //get song
        var playSong: Songdetails?=null
        //get id
        var uri:String
        if(nexturl!=null){
            playSong = songs!![nexturl!!]
            songTitle = playSong!!.songname!!
            uri = playSong.songurl!!
            nextbool = true
        }
        else{
                playSong = songs!![songPosn!!]
                songTitle = playSong!!.songname!!
                uri = playSong.songurl!!
        }
        //set uri
        val prefs = this.getSharedPreferences("OURINFO", MODE_PRIVATE)
        val state = prefs.getBoolean("active", false)
        //set the data source
        if(MainActivity.getInstance()!!.result!=null)
        {
            val playSong = MainActivity.getInstance()!!.result!![songPosn]
            val uri = playSong!!.songurl
            try {
                player!!.setDataSource(applicationContext, Uri.parse(uri))
            } catch (e: Exception) {
                Log.e("MUSIC SERVICE", "Error setting data source", e)
            }
        }
        else
        {
            try {
                player!!.setDataSource(applicationContext, Uri.fromFile(File(uri)))
            } catch (e: Exception) {
                Log.e("MUSIC SERVICE", "Error setting data source", e)
            }
        }

        try {
            player!!.prepareAsync()
        }
        catch(e:Exception)
        {

        }
        if (state==true)
        {
            try {
                SongdetailActivity.getInstance()!!.updateui()
            }
            catch (e:Exception){

            }
            SongdetailActivity.getInstance()!!.upprogress()
            SongdetailActivity.getInstance()!!.setanimation()
            SongdetailActivity.getInstance()!!.setpausebutton()
        }
        MainActivity.getInstance()!!.upprogress()
        MainActivity.getInstance()!!.setpausebuttonnotification()
        if(previouspos!=null)
        {
          Songsadapter.getInstance()!!.notifyItemChanged(previouspos!!)
        }
        if(nexturl!=null){
            previouspos = nexturl
        }
        else{
            previouspos = poss
        }
        if(nexturl!=null){
            Songsadapter.getInstance()!!.lastCheckedPosition = nexturl
        }
        else{
            Songsadapter.getInstance()!!.lastCheckedPosition = poss
        }
        Songsadapter.getInstance()!!.notifyItemChanged(Songsadapter.getInstance()!!.lastCheckedPosition!!)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val f1 = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        val f2 = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        this.registerReceiver(disconnectreceiver, f1)
        this.registerReceiver(disconnectreceiver, f2)
        return START_STICKY
    }
    private fun readFromMediaStore(context:Context, uri:Uri):Media {
        val cursor = context.getContentResolver().query(uri,
                null, null, null, "date_added DESC")
        var media: Media? = null
        if (cursor.moveToNext())
        {
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val filePath = cursor.getString(dataColumn)
            val mimeTypeColumn = cursor
                    .getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val mimeType = cursor.getString(mimeTypeColumn)
            media = Media(File(filePath), mimeType)
        }
        cursor.close()
        return media!!
    }
    private class Media(file:File, type:String) {
        val file:File
        val type:String
        init{
            this.file = file
            this.type = type
        }
    }
    //set the song
    fun setSong(songIndex: Int) {
        songPosn = songIndex
    }

    override fun onCompletion(mp: MediaPlayer) {
        //check if playback has reached the end of a track
        if (player!!.currentPosition > 0) {
            mp.reset()
            if(nextbool==true) {
            nextbool = false
            nexturl = null
            }
           MainActivity.getInstance()!!.playNext()
            MainActivity.getInstance()!!.setpausebuttonnotification()
            val prefs = this.getSharedPreferences("OURINFO", MODE_PRIVATE)
            val state = prefs.getBoolean("active", false)
            if (state==true) {
                try {
                    SongdetailActivity.getInstance()!!.updateui()
                }
                catch(e:Exception)
                {

                }
            }
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.v("MUSIC PLAYER", "Playback Error")
        mp.reset()
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        //start playback
        mp.start()
        MainActivity.getInstance()!!.setplspstab()
        MainActivity.getInstance()!!.update()
    }

    fun pausePlayer() {
        player!!.pause()
        val prefs = this.getSharedPreferences("OURINFO", MODE_PRIVATE)
        val state = prefs.getBoolean("active", false)
        if (state)
        {
            SongdetailActivity.getInstance()!!.stopanimation()
            SongdetailActivity.getInstance()!!.setplaybutton()
        }
        MainActivity.getInstance()!!.setplaybuttonnotification()
        Songsadapter.getInstance()?.lastCheckedPosition = null
        Songsadapter.getInstance()!!.notifyItemChanged(poss!!)
        MainActivity.getInstance()!!.setplspstab()
    }

    fun seek(posn: Int) {
        player!!.seekTo(posn)
    }
    fun stop(){
        player!!.stop()
    }

    fun go() {
        player!!.start()
        val prefs = this.getSharedPreferences("OURINFO", MODE_PRIVATE)
        val state = prefs.getBoolean("active", false)
        if (state==true)
        {
            SongdetailActivity.getInstance()!!.setanimation()
            SongdetailActivity.getInstance()!!.setpausebutton()
            SongdetailActivity.getInstance()!!.upprogress()
        }
        MainActivity.getInstance()!!.setpausebuttonnotification()
        MainActivity.getInstance()!!.upprogress()
        Songsadapter.getInstance()!!.lastCheckedPosition = poss
        if(previouspos!=null)
        {
            Songsadapter.getInstance()!!.notifyItemChanged(previouspos!!)
        }
        previouspos = poss
        Songsadapter.getInstance()!!.lastCheckedPosition = poss
        Songsadapter.getInstance()!!.notifyItemChanged(Songsadapter.getInstance()!!.lastCheckedPosition!!)
        MainActivity.getInstance()!!.setplspstab()
    }

    //skip to previous track
    fun playPrev() {
        songPosn--
        if (songPosn < 0) songPosn = songs!!.size - 1
        playSong()
    }

    //skip to next
    fun playNext() {
        if (shuffle) {
            var newSong = songPosn
            while (newSong == songPosn) {
                newSong = rand!!.nextInt(songs!!.size)
            }
            songPosn = newSong
        } else {
            poss!!.inc()
            if (songPosn >= songs!!.size) songPosn = 0
        }
        playSong()
    }

    override fun onDestroy() {
        stopForeground(true)
        mAudioManager!!.abandonAudioFocus(this);
        MainActivity.getInstance()!!.notificationManager!!.cancelAll()
        mAudioManager!!.unregisterMediaButtonEventReceiver(mReceiverComponent)
    }

    //toggle shuffle
    fun setShuffle() {
        if (shuffle)
            shuffle = false
        else
            shuffle = true
    }

    companion object {
        //notification id
        private val NOTIFY_ID = 1
    }
    inner class MyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                // Log.i("via Receiver","Normal ScreenOFF" );
            } else if (intent.action == Intent.ACTION_SCREEN_ON) {
                //startActivity(Intent(this@MusicService,LockScreen::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } else if (intent.action == Intent.ACTION_ANSWER) {

            }
        }

    }
}
