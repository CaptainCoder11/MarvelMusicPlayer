package com.example.musicplayer.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicplayer.*


import java.util.ArrayList
import android.graphics.Bitmap
import android.graphics.Color
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.EventBus.DelMessage
import com.example.musicplayer.EventBus.Message
import com.example.musicplayer.Extensions.font
import com.example.musicplayer.Extensions.getnothumb
import com.example.musicplayer.Extensions.mlog
import com.example.musicplayer.Extensions.nexturl
import com.example.musicplayer.Model.Songdetails
import java.util.regex.Pattern
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.realm.Realm
import io.realm.RealmResults
import org.greenrobot.eventbus.EventBus
import java.io.File

class Songsadapter(result:RealmResults<Songdetails>, images: ArrayList<String>, realm:Realm, internal var mContext: Activity) : RecyclerView.Adapter<Songsadapter.ProjectViewHolder>(),SectionIndexer {

    var pos : Int?=null
    var mSectionPositions: ArrayList<Int>? = null
    var results = result
    var realm:Realm?=realm
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

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ProjectViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ins = this
        val view = layoutInflater.inflate(R.layout.custom_songs, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {

        if (position == lastCheckedPosition){
            holder.itemView.setBackgroundColor(mContext.resources.getColor(R.color.transblue))
        }
        else{
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
        var t=results!!.get(position)
        var name = t!!.songname
        var artist = t!!.songartist
        var url=t!!.songurl
        loadart(mContext,url!!,name!!,holder)
        holder.tvsname.font("HeroesAssembleRegular-Kmvl.otf")
        holder.tvsartist.font("BlackWidowMovie-d95Rg.ttf")
        holder.tvsname.text = name
        holder.tvsartist.text = artist
        //holder.covimg.setImageBitmap(bitmap)
        holder.playanim.setVisibility(if (position == lastCheckedPosition) View.VISIBLE else View.GONE)
        holder.tvsname.setTextColor(if (position == lastCheckedPosition)Color.parseColor("#03A9F4") else Color.parseColor("#FCFAFA"))
        holder.itemView.setOnClickListener(View.OnClickListener {
            EventBus.getDefault().post(Message(R.id.name,R.id.expndedll, results!!,position))
           /* clickListener.onItemClicked(R.id.name,R.id.expndedll, results!!,position)*/
        })
        holder.menu.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                val bottomSheet = BottomSheetDialog(mContext)
                val view = View.inflate(mContext, R.layout.layout_options, null)
                bottomSheet.setContentView(view)
                val trackname = view.findViewById<TextView>(R.id.trackname)
                val playnext = view.findViewById<LinearLayout>(R.id.playnext)
                val delete = view.findViewById<LinearLayout>(R.id.delete)
                trackname.setText(name)
                playnext.setOnClickListener {

                    nexturl = holder.adapterPosition
                    bottomSheet.dismiss()


                }
                delete.setOnClickListener {
                    val builder = AlertDialog.Builder(mContext)
                    val positiveButtonClick = { dialog: DialogInterface, which: Int ->
                        mlog("Delete Clicked!")
                        if (url.startsWith("content://")) {
                        var contentResolver = mContext.getContentResolver();
                        contentResolver.delete(Uri.parse(url), null, null);
                    } else {
                        var file = File(url);
                        if (file.exists()) {
                            if (file.delete()) {
                                Log.e("Okay", "File deleted.")
                                EventBus.getDefault().post(DelMessage(position))
                                realm!!.executeTransaction(object : Realm.Transaction {
                                    override fun execute(realm: Realm) {
                                        var result = realm.where(Songdetails::class.java).equalTo("songurl", url).findAll()
                                        result.deleteAllFromRealm()
                                        MainActivity.getInstance()!!.refreshlist()
                                        EventBus.getDefault().post(DelMessage(position))
                                        notifyDataSetChanged()
                                    }
                                })
                                bottomSheet.dismiss()
                            } else {
                                Log.e("Okay", "Failed to delete file!");
                                bottomSheet.dismiss()
                            }
                        } else {
                            Log.e("okay", "File not exist!");
                            bottomSheet.dismiss()
                        }
                    }
                    }
                    val negativeButtonClick = { dialog: DialogInterface, which: Int ->
                        dialog.dismiss()
                    }
                    with(builder)
                    {
                        setTitle("Delete")
                        setMessage("Are you sure you want to delete this track?")
                        setPositiveButton("Yes", DialogInterface.OnClickListener(function = positiveButtonClick as (DialogInterface, Int) -> Unit))
                        setNegativeButton("Cancel", negativeButtonClick)
                        show()
                    }

                }
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
    companion object {
        var ins: Songsadapter? = null
        fun getInstance(): Songsadapter? {
            return ins
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

