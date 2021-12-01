package com.example.musicplayer.Extensions

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.musicplayer.Model.AlbumDB
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Model.Songdetails
import io.realm.Realm
import io.realm.RealmList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import java.io.File

fun Context.getSongList() = backgroundscope.launch{
    runBlocking {
        var realm = Realm.getDefaultInstance()
        val musicResolver = getContentResolver()
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null, null, null, null)
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val album = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM)
            val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            var url = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA)
            do {
                try {
                    val thisurls: String = musicCursor.getString(url)
                    var myUri = thisurls.toUri()
                    val date = File(myUri.path).lastModified()
                    Log.i("hmmm:", myUri.toString())
                    var thisTitle = musicCursor.getString(titleColumn)
                    var thisArtist = musicCursor.getString(artistColumn).replace(">", "").replace("<", "")
                    var albumname = musicCursor.getString(album)
                    var albumartist = musicCursor.getString(artistColumn)
                    var f = File(thisurls)
                    if (f.exists()) {
                        val query = realm!!.where(Songdetails::class.java).equalTo("songname", thisTitle).findFirst()
                        val checkalb = realm!!.where(AlbumDB::class.java).equalTo("albumname",albumname).findFirst()

                        if (query == null) {
                            realm = Realm.getDefaultInstance()
                            realm!!.executeTransaction(object : Realm.Transaction {

                                override fun execute(realm: Realm) {
                                    // increment index
                                    var num = realm.where(Songdetails::class.java).max("id")
                                    var nextID: Int
                                    if (num == null) {
                                        nextID = 1
                                    } else {
                                        nextID = num.toInt() + 1
                                    }
                                    var songitem = realm!!.createObject(Songdetails::class.java, nextID)
                                    songitem.songname = thisTitle
                                    songitem.songartist = thisArtist
                                    songitem.songurl = thisurls
                                    songitem.date = date

                                    EventBus.getDefault().post("update")
                                }
                            })
                        }
                        realm.executeTransaction {
                            var albums = realm!!.createObject(AlbumDB::class.java)
                            if (checkalb == null) {
                                
                                albums.songurl = thisurls

                                if(albumname==null)
                                {
                                    albums.albumname = "Music"
                                }
                                else
                                {
                                    albums.albumname = albumname
                                }
                                if(albumartist==null)
                                {
                                    albums.albumartist = "No Artist"
                                }
                                else
                                {
                                    albums.albumartist = albumartist
                                }
                                albums.songartist = RealmList()
                                albums.songartist?.add(thisArtist)
                                albums.songname = RealmList()
                                albums.songname?.add(albumname)

                            }
                            else
                            {
                                albums.songartist?.add(thisArtist)
                                albums.songname?.add(albumname)
                                realm.insertOrUpdate(albums)
                            }
                        }

                    }
                    } catch (ex: Exception) {

                    }

            } while (musicCursor.moveToNext())
        }
    }
    openActivity(MainActivity::class.java)


}