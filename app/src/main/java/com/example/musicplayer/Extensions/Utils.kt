package com.example.musicplayer.Extensions

import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.R
import com.example.musicplayer.Service.MusicService
import com.example.musicplayer.Model.Songdetails
import io.realm.Realm
import io.realm.RealmResults

fun mlog(message: String){
    Log.i("Mylogs", message)
}
fun TextView.font(font: String) {
    typeface = Typeface.createFromAsset(context.assets, "fonts/$font")
}


var hulk = arrayOf(R.mipmap.hulk, R.mipmap.hulktheme)
var thor = arrayOf(R.mipmap.thor, R.mipmap.thor1)
var antman = arrayOf(R.mipmap.antman1, R.mipmap.antman2, R.mipmap.antman3, R.mipmap.antman4, R.mipmap.antman5)
var spiderman = arrayOf(R.mipmap.spiderman, R.mipmap.spiderman1, R.mipmap.spiderman2, R.mipmap.spiderman3, R.mipmap.spiderman4)
var captainamerica = arrayOf(R.mipmap.captainamerica, R.mipmap.captain1)

var nextbool = false
var nexturl:Int?=null
var poss: Int? = null
var snme:String?=null
var srt:String?=null
lateinit var musicSrv: MusicService
var txt:String?=null
var songList:RealmResults<Songdetails>?=null
var shuff:Boolean?=false
var prevpl: ArrayList<Int> = ArrayList()
var songImage:Bitmap?=null
var result: RealmResults<Songdetails>?=null
var realm: Realm?=null
var prevpos:Int?=null
lateinit var cxt:Context
var size: Int? = null
 fun Context.playNext()
{
    if(songList!!.size >= poss!!) {
        prevpl.add(poss!!)
        val prefs = this.getSharedPreferences("OURINFO", Service.MODE_PRIVATE)
        val state = prefs.getBoolean("active", false)
        poss = poss!! + 1
        size = songList!!.size
        if (poss!! > size!!) {
            poss = 0
        }
        val res = songList!!.get(poss!!)
        var songTitle = res!!.songname
        var songart = res!!.songartist
        var image = res!!.songimage
        var url = res!!.songurl
        val bitmap = getimage(this, url!!)
        var d = BitmapDrawable(getResources(), bitmap)
        snme = songTitle
        srt = songart
        songImage = bitmap
        songPicked(poss!!)
        MainActivity.getInstance()!!.update()
    }
}

    fun Context.songPicked(pos: Int) {
          if(songList!!.size >= pos) {
              if (nextbool == true) {
                  nextbool = false
                  nexturl = null
              }
              var playSong: Songdetails? = null
              if (nexturl != null) {
                  playSong = songList!!.get(nexturl!!)
              } else {
                  playSong = songList!!.get(poss!!)
              }
              var songTitle = playSong!!.songname
              var songart = playSong!!.songartist
              var image = playSong!!.songimage
              var url = playSong.songurl
              val bitmap = getimage(this, url!!)
              snme = songTitle
              srt = songart
              songImage = bitmap
              musicSrv!!.setSong(pos)
              musicSrv!!.playSong()
              MainActivity.getInstance()!!.update()
          }
}
fun freeMemory() {
    System.runFinalization()
    Runtime.getRuntime().gc()
    System.gc()
}
fun Context.playPrev() {
    if(songList!!.size >= poss!!) {
        val prefs = this.getSharedPreferences("OURINFO", Service.MODE_PRIVATE)
        val state = prefs.getBoolean("active", false)
        if (shuff == true) {
            if (prevpos == null) {
                prevpos = prevpl.size
            }
            prevpos = prevpos!! - 1
            if (prevpos!! >= 0) {
                poss = prevpl.get(prevpos!!)
            }
        } else {
            poss = poss!! - 1
        }
        size = songList!!.size
        if (poss!! < 0) {
            poss = size!! - 1
        }
        val playSong = songList!!.get(poss!!)
        var songTitle = playSong!!.songname
        var songart = playSong!!.songartist
        var image = playSong!!.songimage
        var url = playSong.songurl
        val bitmap = getimage(this, url!!)
        snme = songTitle
        srt = songart
        songImage = bitmap
        songPicked(poss!!)
    }
}
fun View.toggleVisibility() : View {
    if (visibility == View.VISIBLE) {
        visibility = View.INVISIBLE
    } else {
        visibility = View.VISIBLE
    }
    return this
}
fun View.show() : View {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
    return this
}
fun View.hide() : View {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }
    return this
}
fun View.hideKeyboard(): Boolean {
    try {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (ignored: RuntimeException) { }
    return false
}

fun getimage(context: Context, url: String):Bitmap{
    val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val selection = MediaStore.Audio.Media.DATA + "=? "
    var imgUri:Uri?=null
    var bitmap:Bitmap?=null
    val projection = arrayOf<String>(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID)
    val cursor = context.getContentResolver().query(
            audioUri,
            projection,
            selection,
            arrayOf<String>(url), null)
    if (cursor != null && cursor.moveToFirst())
    {
        val albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        imgUri = ContentUris.withAppendedId(sArtworkUri,
                albumId)
        cursor.close()
    }
    try{
    return MediaStore.Images.Media.getBitmap(context.getContentResolver(), imgUri)
    }
    catch (ex: Exception)
    {
         bitmap = BitmapFactory.decodeResource(context.getResources(), getnothumb())
        return bitmap
    }
}
