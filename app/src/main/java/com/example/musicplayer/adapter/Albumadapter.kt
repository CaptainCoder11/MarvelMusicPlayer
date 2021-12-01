package com.example.musicplayer.adapter

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicplayer.*


import java.util.ArrayList
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.musicplayer.Extensions.font
import com.example.musicplayer.Extensions.getnothumb
import com.example.musicplayer.Model.AlbumDB
import java.util.regex.Pattern
import io.realm.RealmResults

class Albumadapter(result:RealmResults<AlbumDB>, internal var mContext: Activity) : RecyclerView.Adapter<Albumadapter.ProjectViewHolder>(),SectionIndexer {

    var mSectionPositions: ArrayList<Int>? = null
    var results = result
    override fun getSections(): Array<out String>? {
        var sections = ArrayList<String>(26)
        mSectionPositions = ArrayList(26)
        var i = 0
        val size = results!!.size
        var section:String?=null
        while (i < size) {
            var t=results!!.get(i)
            var name = t!!.albumname
            val regex = Pattern.compile("[a-zA-Z]+\\.?")
            if(!regex.matcher(name?.get(0).toString()).find())
            {
                 section = "#"
            }
            else
            {
                section = name?.get(0)?.toUpperCase().toString()
            }
            if (!sections.contains<String>(section.toString())) {
                sections.add(section.toString())
                mSectionPositions!!.add(i)
            }
            i++
        }
        return sections.toArray(arrayOf<String>(0.toString()))
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0;
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return mSectionPositions!!.get(sectionIndex);
    }


    //var item=items
    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ProjectViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ins = this
        val view = layoutInflater.inflate(R.layout.item_album, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {

        var t=results!!.get(position)
        var name = t!!.albumname
        var artist = t!!.albumartist
        var url=t?.songurl

        loadart(mContext,url!!,holder)


        holder.tvalbname.font("HeroesAssembleRegular-Kmvl.otf")
        holder.tvalbartist.font("BlackWidowMovie-d95Rg.ttf")
        holder.tvalbname.text = name
        holder.tvalbartist.text = artist

    }

    override fun getItemCount(): Int {
        return results!!.size
    }


    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvalbname: TextView
        var tvalbartist: TextView
        var covimg: ImageView
        init {
            tvalbartist = itemView.findViewById(R.id.tv_album_artist)
            tvalbname = itemView.findViewById(R.id.tv_album)
            covimg = itemView.findViewById(R.id.img_alb) as ImageView
        }

    }
    companion object {
        var ins: Albumadapter? = null
        fun getInstance(): Albumadapter? {
            return ins
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
            val imgUri = ContentUris.withAppendedId(sArtworkUri,
                    albumId)
            Glide.with(context)
                    .asBitmap()
                    .placeholder(R.mipmap.avengerswallp)
                    .load(imgUri)
                    .error(getnothumb())
                    .listener(object : RequestListener<Bitmap>{
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                            holder.covimg.setImageResource(getnothumb())
                            return true
                        }

                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }
                    })
                    .into(holder.covimg)
            cursor.close()
        }
    }



}

