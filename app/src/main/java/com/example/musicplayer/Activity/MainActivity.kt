package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.widget.MediaController
import android.widget.RemoteViews
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.eftimoff.viewpagertransformers.FlipHorizontalTransformer
import com.example.musicplayer.BluetoothReceiver.NextButtonReceiver
import com.example.musicplayer.BluetoothReceiver.PlayButtonReceiver
import com.example.musicplayer.BluetoothReceiver.PrevButtonReceiver
import com.example.musicplayer.EventBus.DelMessage
import com.example.musicplayer.EventBus.Message
import com.example.musicplayer.Extensions.*
import com.example.musicplayer.Fragment.*
import com.example.musicplayer.Model.Songdetails
import com.example.musicplayer.R
import com.example.musicplayer.Service.MusicService
import com.example.musicplayer.adapter.Songsadapter
import com.example.musicplayer.adapter.SwipeListener
import com.example.musicplayer.adapter.Tabadapter
import com.fondesa.kpermissions.request.PermissionRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import jp.wasabeef.blurry.Blurry
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.layout_options.*
import kotlinx.android.synthetic.main.menu_drawer.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class MainActivity() : AppCompatActivity(),View.OnClickListener,MediaController.MediaPlayerControl{
    var animationcounter = 0
    fun inflatedrawer()
    {
       tvinfo.font("HeroesAssembleRegular-Kmvl.otf")
       tvprivacypolicy.font("HeroesAssembleRegular-Kmvl.otf")
       tvtheme.font("HeroesAssembleRegular-Kmvl.otf")
    }
    override fun onResume() {
        super.onResume()
    }
    private var mAudioManager: AudioManager? = null

    private var adapter: Tabadapter? = null
    var onpbool = false
    var r:Runnable?=null
    var playIntent:Intent?=null
    var musicBound = false
    var alertDialog:AlertDialog?=null
    var paused=false
    var playbackPaused = false
    var result: RealmResults<Songdetails>?=null
    var request:PermissionRequest?=null
    lateinit var textView: TextView
    var context: Context? = null
    var notificationManager:NotificationManager?=null
    var notificationbuilder: NotificationCompat.Builder?=null
    var realm:Realm?=null
    var dialogShown: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Glide.with(this)
                .asBitmap()
                .load(R.mipmap.thor)
                .apply(bitmapTransform(BlurTransformation(5, 6)))
                .placeholder( ColorDrawable(Color.BLACK))
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        var d = BitmapDrawable(resources,  resource)
                        sliding_layout.background = d
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
        cxt = this
        var builder = SlidingRootNavBuilder(this).withMenuLayout(R.layout.menu_drawer).withDragDistance(140) //Horizontal translation of a view. Default == 180dp
                .withRootViewScale(0.7f) //Content view's scale will be interpolated between 1f and 0.7f. Default == 0.65f;
                .withRootViewElevation(10) //Content view's elevation will be interpolated between 0 and 10dp. Default == 8.
                .withRootViewYTranslation(4) //Content view's translationY will be interpolated between 0 and 4. Default == 0
       builder.inject()
        inflatedrawer()
        EventBus.getDefault().register(this)
        var bottomSheetBehavior = BottomSheetBehavior.from(dragView)
        bottomSheetBehavior.setBottomSheetCallback(object:BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
                mlog(p1.toString())
                var prevfloat = 0.0.toFloat()
                if(p1>prevfloat){
                    relsng.show()
                    relsng.y = p1 * 300
                }
                else {
                    relsng.show()
                    relsng.x = p1 * 300
                }
                prevfloat = p1
            }
            override fun onStateChanged(view: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    relsng.hide()
                }else if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    relsng.show()
            }
            }
        })
        play_pause_view.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
        play_pause_view.setOnClickListener({
            if (isPlaying)
            {
                pause()
            }
            else
            {
                start()
            }
            togplpsbt()
        })
            relsng.setOnTouchListener(object : SwipeListener(this@MainActivity) {

                override fun onSwipeRight() {
                    playPrev()
                }

                override fun onSwipeLeft() {
                    playNext()

                }
            })
            expndedll.setOnTouchListener(object : SwipeListener(this@MainActivity) {
                override fun onSwipeRight() {
                    playPrev()
                }

                override fun onSwipeLeft() {
                    playNext()
                }
                override fun onClick() {
                    val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity)
                    var intent= Intent(this@MainActivity, SongdetailActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent,options.toBundle())
                }
            })
        tabplps.setOnClickListener(this)
        giftanim.setOnClickListener {
            blastanim.show()
            var h = Handler()
            h.postDelayed({
                blastanim.hide()
            }, 3000)
        }
        //searchBar.hide()
        realm = Realm.getDefaultInstance()
        var query = realm!!.where(Songdetails::class.java)
        songList = query.findAll().sort("songname", Sort.ASCENDING)
        if(songList.isNullOrEmpty())
        {
            tvnotfound.font("HeroesAssembleRegular-Kmvl.otf")
            tvnotfound.show()
        }
        val sp = getSharedPreferences("OURINFO", MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("active", false)
        ed.commit()
        ins = this
        setbackground()
        adapter = Tabadapter(supportFragmentManager)
        adapter!!.addFragment(Songsfragment(), "Songs")
        adapter!!.addFragment(Playlist(), "Playlist")
        adapter!!.addFragment(Folders(), "Folders")
        adapter!!.addFragment(Albums(), "Albums")
        adapter!!.addFragment(Artists(), "Artists")
        adapter!!.addFragment(Genres(), "Genres")
        viewPager!!.adapter = adapter
        viewPager.setPageTransformer(true, FlipHorizontalTransformer())
        tabLayout!!.setupWithViewPager(viewPager)
        if(shuff==false)
        {
            shuffle.setColorFilter(ContextCompat.getColor(this, R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        else
        {
            shuffle.setColorFilter(ContextCompat.getColor(this, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        //expndedll.setOnClickListener(this)
        shuffle.setOnClickListener(this)
        search.setOnClickListener(this)

        search()


    }
    fun togplpsbt(){
        if (isPlaying) {
            play_pause_view!!.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
        } else {
            play_pause_view!!.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
        }
    }
    fun upprogress() {
        val handler = Handler()
        var mediaPos = getInstance()!!.currentPosition
        var mediaMax = getInstance()!!.duration
        seekbar!!.setMax(mediaMax.toFloat()) // Set the Maximum range of the
        seekbar!!.setProgress(mediaPos.toFloat())// set current progress to song's
        val moveSeekBarThread = object : Runnable {
            override fun run() {

                if (getInstance()!!.isPlaying) {
                    val mediaPos_new = getInstance()!!.currentPosition
                    val mediaMax_new =  getInstance()!!.duration
                    seekbar!!.setMax(mediaMax_new.toFloat())
                    seekbar!!.setProgress(mediaPos_new.toFloat())

                    handler.postDelayed(this, 100) //Looping the thread after 0.1 second

                }


            }
        }
        handler.postDelayed(moveSeekBarThread, 100)

        seekbar!!.setOnSeekChangeListener(object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {
                handler.removeCallbacksAndMessages(null)
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {
                handler.removeCallbacksAndMessages(null)
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                if (getInstance()!!.isPlaying) {
                    getInstance()!!.seekTo(seekBar.progress)
                    handler.postDelayed(moveSeekBarThread, 100)
                }
            }
        })
    }
    fun setplspstab(){
        if(isPlaying){
            tabplps.setImageResource(R.drawable.ic_pause_blue_24dp)
        }
        else
        {
            tabplps.setImageResource(R.drawable.ic_play_arrow_blue_24dp)
        }
    }
    override fun onClick(v:View) {
        when (v.getId()) {
            R.id.shuffle -> {
                //open login screen
                if(shuff==false)
                {
                    shuff = true
                    shuffle.setColorFilter(ContextCompat.getColor(this, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
                }
                else{
                    shuff = false
                    shuffle.setColorFilter(ContextCompat.getColor(this, R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN)
                }
            }
            R.id.search -> {
               //searchBar.toggleVisibility()
            }

            R.id.tabplps -> {
                if(musicSrv.isPlaying)
                {
                   musicSrv.pausePlayer()
                }
                else
                {
                    musicSrv.go()
                }
            }
        }
    }
    //connect to the service
    private val musicConnection = object:ServiceConnection {
        override fun onServiceConnected(name:ComponentName, service:IBinder) {
            val binder = service
            musicSrv = (binder as MusicService.MusicBinder).service
            musicSrv!!.setList(songList)
            musicBound = true
        }
        override fun onServiceDisconnected(name:ComponentName) {
            musicBound = false
        }
    }
    //start and bind the service when the activity starts
    protected override fun onStart() {
        super.onStart()
        if (playIntent == null)
        {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }
    //user song select
    fun setloop(bool:Boolean){
        musicSrv.setloop(bool)
    }
    fun getloop():Boolean{
       return musicSrv.getloop()
    }
fun setpausebuttonnotification() {
    val ns = Context.NOTIFICATION_SERVICE
    notificationManager = getSystemService(ns) as NotificationManager
    val NOTIFICATION_CHANNEL_ID = "com.example.musicplayer"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationchanne = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_DEFAULT)
        notificationchanne.description = "Music Player"
        notificationchanne.vibrationPattern = longArrayOf(0L)
        notificationchanne.enableLights(true)
        notificationchanne.lightColor = Color.BLUE
        notificationchanne.setSound(null, null)
        notificationchanne.enableLights(true)
        notificationManager!!.createNotificationChannel(notificationchanne)
    }
    val notificationView = RemoteViews(getPackageName(), R.layout.notification_thor)
    notificationView.setInt(R.id.themeimg, "setBackgroundResource", R.drawable.thorhammer0)
    notificationView.setImageViewBitmap(R.id.imgalb, songImage)
    notificationView.setTextViewText(R.id.sname, snme)
    notificationView.setTextViewText(R.id.sartist, srt)
    val buttonsIntent = Intent(this@MainActivity, PlayButtonReceiver::class.java)
    val prevIntent = Intent(this@MainActivity, PrevButtonReceiver::class.java)
    val nextIntent = Intent(this@MainActivity, NextButtonReceiver::class.java)
    val detailintent = Intent(this@MainActivity, SongdetailActivity::class.java)
    notificationView.setImageViewResource(R.id.imgpl_ps, R.drawable.ic_pause_black_24dp)
    notificationView.setImageViewResource(R.id.imgnext, R.drawable.ic_skip_next_white_24dp)
    notificationView.setImageViewResource(R.id.imgprev, R.drawable.ic_skip_previous_white_24dp)
    buttonsIntent.putExtra("do_action", "play")
    prevIntent.putExtra("do_action", "prev")
    nextIntent.putExtra("do_action", "next")
    notificationView.setOnClickPendingIntent(R.id.imgprev, PendingIntent.getBroadcast(this@MainActivity, 0, prevIntent, 0))
    notificationView.setOnClickPendingIntent(R.id.imgpl_ps, PendingIntent.getBroadcast(this@MainActivity, 0, buttonsIntent, 0))
    notificationView.setOnClickPendingIntent(R.id.imgnext, PendingIntent.getBroadcast(this@MainActivity, 0, nextIntent, 0))
    notificationView.setOnClickPendingIntent(R.id.notl, PendingIntent.getActivity(this@MainActivity, 0, detailintent, 0))
     notificationbuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
    notificationbuilder!!.setAutoCancel(true)
            .setVibrate(longArrayOf(0L))
            .setOngoing(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setCustomBigContentView(notificationView)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    notificationManager!!.notify(0, notificationbuilder!!.build())
    //Handler().postDelayed({setpausebuttonnotification()},250)
}
fun setplaybuttonnotification(){
    val ns = Context.NOTIFICATION_SERVICE
    notificationManager = getSystemService(ns) as NotificationManager
    val NOTIFICATION_CHANNEL_ID = "com.example.musicplayer"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationchanne = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_DEFAULT)
        notificationchanne.description = "Music Player"
        notificationchanne.vibrationPattern = longArrayOf(0L)
        notificationchanne.enableLights(true)
        notificationchanne.lightColor = Color.BLUE
        notificationchanne.setSound(null, null)
        notificationchanne.enableLights(true)
        notificationManager!!.createNotificationChannel(notificationchanne)
    }
    val notificationView = RemoteViews(getPackageName(), R.layout.notification_thor)
    notificationView.setInt(R.id.themeimg, "setBackgroundResource", R.drawable.thorhammer0)
    notificationView.setImageViewBitmap(R.id.imgalb, songImage)
    notificationView.setTextViewText(R.id.sname, snme)
    notificationView.setTextViewText(R.id.sartist, srt)
    val buttonsIntent = Intent(this@MainActivity, PlayButtonReceiver::class.java)
    val prevIntent = Intent(this@MainActivity, PrevButtonReceiver::class.java)
    val nextIntent = Intent(this@MainActivity, NextButtonReceiver::class.java)
    val detailintent = Intent(this@MainActivity, SongdetailActivity::class.java)
    notificationView.setImageViewResource(R.id.imgpl_ps, R.drawable.ic_play_arrow_black_24dp)
    notificationView.setImageViewResource(R.id.imgnext, R.drawable.ic_skip_next_white_24dp)
    notificationView.setImageViewResource(R.id.imgprev, R.drawable.ic_skip_previous_white_24dp)
    buttonsIntent.putExtra("do_action", "play")
    prevIntent.putExtra("do_action", "prev")
    nextIntent.putExtra("do_action", "next")
    notificationView.setOnClickPendingIntent(R.id.imgprev, PendingIntent.getBroadcast(this@MainActivity, 0, prevIntent, 0))
    notificationView.setOnClickPendingIntent(R.id.imgpl_ps, PendingIntent.getBroadcast(this@MainActivity, 0, buttonsIntent, 0))
    notificationView.setOnClickPendingIntent(R.id.imgnext, PendingIntent.getBroadcast(this@MainActivity, 0, nextIntent, 0))
    notificationView.setOnClickPendingIntent(R.id.notl, PendingIntent.getActivity(this@MainActivity, 0, detailintent, 0))
    notificationbuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
    notificationbuilder!!.setAutoCancel(true)
            .setVibrate(longArrayOf(0L))
            .setOngoing(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setCustomBigContentView(notificationView)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    notificationManager!!.notify(0, notificationbuilder!!.build())
}
    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getAudioSessionId(): Int {
        return 0
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun getCurrentPosition(): Int {
        return if (musicSrv != null && musicBound && musicSrv!!.isPlaying)
            musicSrv!!.posn
        else 0
    }

    override fun getDuration(): Int {
        return if (musicSrv != null && musicBound && musicSrv!!.isPlaying)
            musicSrv!!.dur
        else
            0
    }

    override fun isPlaying(): Boolean {
        return if (musicSrv != null && musicBound) musicSrv!!.isPlaying else false
    }

    override fun pause() {
        playbackPaused = true
        musicSrv!!.pausePlayer()
    }


    override fun seekTo(pos: Int) {
        musicSrv!!.seek(pos)
    }

    override fun start() {
        musicSrv!!.go()
    }
    //region on itemclick load song interface

    //endregion
    //region Load song


    fun setbackground(){
        var pos = 0
        var h = Handler()
        r = Runnable {
            when(pos)
            {
                backlist.size -> pos = 0
            }
            Glide.with(this)
                    .asBitmap()
                    .load(backlist.get(pos))
                    .apply(bitmapTransform(BlurTransformation(5, 6)))
                    .placeholder( ColorDrawable(Color.BLACK))
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            var d = BitmapDrawable(resources,  resource)
                            sliding_layout.background = d
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            pos++
          h.postDelayed(r,20000)
        }
        h.postDelayed(r,20000)
    }
    companion object {
        var ins: MainActivity? = null
        var backlist = arrayOf(
            R.mipmap.groot,
            R.mipmap.antman1,
            R.mipmap.antman2,
            R.mipmap.antman3,
            R.mipmap.antman4,
            R.mipmap.antman5,
            R.mipmap.blackpanther1,
            R.mipmap.blackpanther2,
            R.mipmap.blackpanther3,
            R.mipmap.blackpanther4,
            R.mipmap.deadpool1,
            R.mipmap.deadpool3,
            R.mipmap.deadpool4,
            R.mipmap.deadpool,
            R.mipmap.deadpool5,
            R.mipmap.spiderman1,
            R.mipmap.spiderman2,
            R.mipmap.spiderman3,
            R.mipmap.spiderman4,
            R.mipmap.aquaman,
            R.mipmap.bat,
            R.mipmap.batman,
            R.mipmap.captainamerica,
            R.mipmap.deadpool,
            R.mipmap.spiderman,
            R.mipmap.superman,
            R.mipmap.thor
        )
        fun getInstance(): MainActivity? {
            return ins
        }
    }

    override fun onPause() {
        super.onPause()
        paused = true
        onpbool = true
    }

fun savenewsong(path:String){
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val art = retriever.getEmbeddedPicture()
        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        if (art!=null)
        {
            var bitmap = BitmapFactory.decodeByteArray(art, 0, art.size)

          /*  ImageSaver(this@MainActivity)
            .setFileName(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
            .setDirectoryName("images")
            .save(bitmap)*/
    realm!!.executeTransactionAsync(object: Realm.Transaction {

        override fun execute(realm: Realm) {
            // increment index
            var num = realm.where(Songdetails::class.java).max("id")
            var nextID:Int
            if(num == null) {
                nextID = 1
            } else {
                nextID = num.toInt() + 1
            }
            var songitem = realm!!.createObject(Songdetails::class.java,nextID)
            songitem.songname = title
            songitem.songartist = artist
            songitem.songurl = path
            songitem.songimage = title
        }
    },Realm.Transaction.OnSuccess{
        Songsfragment.getInstance()!!.setadapter()
    },Realm.Transaction.OnError{

    })
}
    else {
        realm!!.executeTransactionAsync(object: Realm.Transaction {

            override fun execute(realm: Realm) {
                // increment index
                var num = realm.where(Songdetails::class.java).max("id")
                var nextID:Int
                if(num == null) {
                    nextID = 1
                } else {
                    nextID = num.toInt() + 1
                }
                var songitem = realm!!.createObject(Songdetails::class.java,nextID)
                songitem.songname = title
                songitem.songartist = artist
                songitem.songurl = path
                songitem.songimage = "custom"
            }
        },Realm.Transaction.OnSuccess{
            Songsfragment.getInstance()!!.setadapter()
        },Realm.Transaction.OnError{

        }

        )
    }
}
    @Subscribe
    fun onStringEvent(event:String){
        if(event.equals("setplaylistfragment"))
        {
            fragment_content.show()
            val myf = PlaylistSongsFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_content, myf)
            transaction.commit()
        }
    }
    @Subscribe fun ondelevent(event: DelMessage)
    {
        if(musicSrv.songPosn == event.pos)
        {
            musicSrv.stop()
            songPicked(poss!!.inc())
        }
        else
        {
            mlog(Songsadapter.getInstance()!!.lastCheckedPosition.toString())
            mlog(poss.toString())
            if(songList!!.get(poss!!)!!.songname!!.equals(snme))
            {
               mlog("Normal Match")
            }
            else if(songList!!.get(poss!!.dec())!!.songname!!.equals(snme))
            {
                mlog("Decreased Match")
                Songsadapter.getInstance()?.lastCheckedPosition = Songsadapter.getInstance()!!.lastCheckedPosition?.minus(1)
            }
            else if(songList!!.get(poss!!.inc())!!.songname!!.equals(snme))
            {
                mlog("Increased Match")
                Songsadapter.getInstance()?.lastCheckedPosition = Songsadapter.getInstance()!!.lastCheckedPosition?.plus(1)
            }

        }
        if(nexturl == event.pos)
        {
            nexturl = null
        }
    }

    fun refreshlist(){
        var query = realm!!.where(Songdetails::class.java)
        songList = query.findAll().sort("songname", Sort.ASCENDING)
        musicSrv.setList(songList)
    }
    @Subscribe fun onInterfaceEvent(event:Message){
        val playSong = event.results!!.get(event.position)
        var songTitle = playSong!!.songname
        var songart = playSong!!.songartist
        var image = playSong!!.songimage
        name.setText(songTitle)
        name.isSelected = true
        stitle.isSelected = true
        name.font("HeroesAssembleRegular-Kmvl.otf")
        stitle.text = songTitle
        stitle.font("HeroesAssembleRegular-Kmvl.otf")
        artists.text = songart
       artists.font("BlackWidowMovie-d95Rg.ttf")
        snme=songTitle
        srt=songart
        var url = playSong.songurl
        val bitmap = getimage(this@MainActivity,url!!)
        Blurry.with(this@MainActivity).radius(10).sampling(10).from(bitmap).into(imgexp)
        imgtab.setImageBitmap(bitmap)
        songImage = bitmap
        poss = event.position
        songPicked(poss!!)
        previous.setOnClickListener({
            playPrev()
        })
        next.setOnClickListener({
            playNext()
        })
    }
   fun update(){
        val playSong = songList!!.get(poss!!)
        var songTitle = playSong!!.songname
        var songart = playSong!!.songartist
        var image = playSong!!.songimage
        name.setText(songTitle)
        name.font("HeroesAssembleRegular-Kmvl.otf")
        stitle.text = songTitle
        stitle.font("HeroesAssembleRegular-Kmvl.otf")
        artists.text = songart
       artists.font("BlackWidowMovie-d95Rg.ttf")
        snme=songTitle
        srt=songart
        var url = playSong.songurl
        val bitmap = getimage(this,url!!)
        Blurry.with(this@MainActivity).radius(10).sampling(10).from(bitmap).into(imgexp)
        imgtab.setImageBitmap(bitmap)
        songImage = bitmap
    }
    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        musicSrv.pausePlayer()
        notificationbuilder!!.setAutoCancel(true)
        notificationbuilder!!.setOngoing(false)
        notificationManager!!.notify(0, notificationbuilder!!.build())
    }
    fun search() {
        //setContentView(R.layout.view_feed_toolbar);
      /*  searchBar!!.hide()
        searchBar!!.setSpeechMode(true)
        searchBar!!.enableSearch()
        searchBar!!.addTextChangeListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txt = s.toString()
                getfilteredsuggestions()
            }
        })
        searchBar!!.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
            @SuppressLint("WrongConstant")
            override fun onSearchStateChanged(enabled: Boolean) {
                val s = if (enabled) "enabled" else "disabled"
                if (s == "disabled") {
                    Songsfragment.getInstance()!!.songsAdapter = Songsadapter(songList!!,Songsfragment.getInstance()!!.images!!, realm!!,Songsfragment.getInstance()!!.activity!!)
                    val songslayoutManager = LinearLayoutManager(context)
                    songslayoutManager.orientation = LinearLayoutManager.VERTICAL
                    Songsfragment.getInstance()!!.rvsongs!!.layoutManager = songslayoutManager
                    Songsfragment.getInstance()!!.rvsongs!!.setHasFixedSize(true)
                    Songsfragment.getInstance()!!.rvsongs!!.adapter = Songsfragment.getInstance()!!.songsAdapter
                    //searchBar!!.hide()
                    result = null
                    txt = null
                }
            }

            override fun onSearchConfirmed(text: CharSequence) {
               txt = text.toString()
            }

            override fun onButtonClicked(buttonCode: Int) {
                when (buttonCode) {
                  
                }
            }
        })*/
    }
    @SuppressLint("WrongConstant")
    fun getfilteredsuggestions(){
     result = realm!!.where(Songdetails::class.java)
                                 .beginGroup()
                                      .equalTo("songname", txt, Case.INSENSITIVE)
                                      .or()
                                      .contains("songname", txt, Case.INSENSITIVE)
                                 .endGroup()
                                 .findAll()
                                 .sort("songname", Sort.ASCENDING)
        if(!result.isNullOrEmpty())
        {
            Songsfragment.getInstance()!!.songsAdapter = Songsadapter(result!!,
                Songsfragment.getInstance()!!.images!!, realm!!,
                Songsfragment.getInstance()!!.activity!!)
            val songslayoutManager = LinearLayoutManager(context)
            songslayoutManager.orientation = LinearLayoutManager.VERTICAL
            Songsfragment.getInstance()!!.rvsongs!!.layoutManager = songslayoutManager
            Songsfragment.getInstance()!!.rvsongs!!.setHasFixedSize(true)
            Songsfragment.getInstance()!!.rvsongs!!.adapter = Songsfragment.getInstance()!!.songsAdapter
        }
    }

}



