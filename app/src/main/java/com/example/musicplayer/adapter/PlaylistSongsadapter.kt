package com.example.musicplayer.adapter

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicplayer.*


import java.util.ArrayList
import android.graphics.Bitmap
import android.graphics.Color
import android.provider.MediaStore
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.musicplayer.Model.Songdetails
import java.util.regex.Pattern
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.realm.RealmResults
//import wseemann.media.FFmpegMediaMetadataRetriever


class PlaylistSongsadapter(result:RealmResults<Songdetails>, internal var mContext: Context) : RecyclerView.Adapter<PlaylistSongsadapter.ProjectViewHolder>(),SectionIndexer {

    var pos : Int?=null
    var mSectionPositions: ArrayList<Int>? = null
    var results = result
    var lastCheckedPosition:Int?=null
    override fun getSections(): Array<out String>? {
        var sections = ArrayList<String>(26)
        mSectionPositions = ArrayList(26)
        var i = 0
        val size = results!!.size
        var section:String?=null
        while (i < size) {
            var t=results!!.get(i)
            var name = t!!.songname!!
            val regex = Pattern.compile("[a-zA-Z]+\\.?")
            if(!regex.matcher(name.get(0).toString()).find())
            {
                 section = "#"
            }
            else
            {
                section = name.get(0).toUpperCase().toString()
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
        val view = layoutInflater.inflate(R.layout.custom_songs, parent, false)
        return ProjectViewHolder(view)
    }
   /* fun setFadeAnimation(view:View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.setDuration(100)
        view.startAnimation(anim)
    }*/
    private var lastPosition = -1
    private fun setAnimation(viewToAnimate:View, position:Int) {
        if (position > lastPosition)
        {
            val animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left)
            animation.setDuration(500)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
        else if (position < lastPosition)
        {
            val animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }
    private fun setclickAnimation(viewToAnimate:View, position:Int) {
        val shrinkSet = AnimatorInflater.loadAnimator(mContext, R.animator.shrink_to_middle) as AnimatorSet
        shrinkSet.setTarget(viewToAnimate)
        shrinkSet.start()
        val growSet = AnimatorInflater.loadAnimator(mContext, R.animator.grow_from_middle) as AnimatorSet
        growSet.setTarget(viewToAnimate)
        growSet.start()
    }



    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        //val t = item!!.get(position)
        //setFadeAnimation(holder.itemView)
        if(position!=lastCheckedPosition) {
            setAnimation(holder.itemView, position)
        }
        else{
            setclickAnimation(holder.itemView, position)
        }
            var t=results!!.get(position)
        var name = t!!.songname
        var artist = t!!.songartist
        var url=t!!.songurl
        var songimg = t!!.songimage
       // val bitmap = ImageSaver(mContext).setFileName(songimg!!).setDirectoryName("images").load()
        loadart(mContext,url!!,name!!,holder)
        holder.tvsname.text = name
        holder.tvsartist.text = artist
        //holder.covimg.setImageBitmap(bitmap)
        holder.playanim.setVisibility(if (position == lastCheckedPosition) View.VISIBLE else View.INVISIBLE)
        holder.tvsname.setTextColor(if (position == lastCheckedPosition)Color.parseColor("#03A9F4") else Color.parseColor("#FCFAFA"))
        holder.menu.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                val bottomSheet = BottomSheetDialog(mContext)
                val view = View.inflate(mContext, R.layout.layout_options, null)
                bottomSheet.setContentView(view)
                val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
                bottomSheetBehavior.peekHeight = 1000
                bottomSheet.show()
            }
        })

    }
    interface OnAdapterItemClickListener {
        fun onItemClicked(id: Int, expll:Int, songslist: List<Songdetails>, pos:Int)
    }
    override fun getItemCount(): Int {
        return results!!.size
    }


    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvsname: TextView
        var tvsartist: TextView
        var covimg: ImageView
        var menu: ImageView
        var playanim:LottieAnimationView
        init {
            tvsname = itemView.findViewById(R.id.sname)
            tvsartist = itemView.findViewById(R.id.sartist)
            playanim = itemView.findViewById(R.id.playinganim)
            covimg = itemView.findViewById(R.id.imgscov) as ImageView
            menu = itemView.findViewById(R.id.imgmenu)
        }

    }
    fun loadart(context:Context,filePath:String,name:String,holder: ProjectViewHolder) {
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
                    .load(imgUri)
                    .error(R.mipmap.icon)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                            holder.covimg.setImageResource( R.mipmap.icon)
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
    companion object {
        var ins: PlaylistSongsadapter? = null
        fun getInstance(): PlaylistSongsadapter? {
            return ins
        }
    }



}

