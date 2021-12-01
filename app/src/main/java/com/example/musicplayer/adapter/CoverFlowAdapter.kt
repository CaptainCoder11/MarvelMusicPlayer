package com.example.musicplayer.adapter

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.*
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Extensions.getnothumb
import com.example.musicplayer.Extensions.poss
import com.example.musicplayer.Extensions.songPicked
import com.example.musicplayer.Model.Songdetails
import java.util.*


class CoverFlowAdapter(mcontext:Context,result:List<Songdetails>) : RecyclerView.Adapter<CoverFlowAdapter.ProjectViewHolder>() {
    var images=result
    var name=ArrayList<String>()
    var context=mcontext

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_coverflow, parent, false)
        return ProjectViewHolder(view)
    }

    override fun getItemCount(): Int {
     return images.size
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        var t = images.get(position)
        var url = t!!.songurl
        loadart(context, url!!, holder)
        holder.img.setOnClickListener {
            poss = position
            MainActivity.getInstance()!!.songPicked(position)
        }
    }
    fun loadart(context:Context,filePath:String,holder: ProjectViewHolder) {
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.DATA + "=? "
        val projection = arrayOf<String>(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID)
        val cursor = context.getContentResolver().query(
                audioUri,
                projection,
                selection,
                arrayOf<String>(filePath), null)
        if (cursor != null && cursor.moveToFirst())
        {
            val albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
            val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
            val imgUri = ContentUris.withAppendedId(sArtworkUri, albumId)
            Glide.with(context)
                    .asBitmap()
                    .load(imgUri)
                    .error(getnothumb())
                    .into(holder.img!!)
            cursor.close()
        }
    }

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

       var img:ImageView
        init {
            img = itemView.findViewById(R.id.image)
        }

    }
}