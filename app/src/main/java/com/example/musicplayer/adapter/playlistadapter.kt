package com.example.musicplayer.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicplayer.*


import java.util.ArrayList
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Model.Songdetails
import org.greenrobot.eventbus.EventBus

//import wseemann.media.FFmpegMediaMetadataRetriever


class playlistadapter(list: ArrayList<String>, internal var mContext: Context) : RecyclerView.Adapter<playlistadapter.ProjectViewHolder>() {

    var pos : Int?=null
    var list = list
    //var item=items
    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ProjectViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.playlist_item, parent, false)
        return ProjectViewHolder(view)
    }
   /* fun setFadeAnimation(view:View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.setDuration(100)
        view.startAnimation(anim)
    }*/

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.pname.text = list.get(position)
        holder.itemView.setOnClickListener {
            Log.i("hmmm:","OkayEventOccured")
         EventBus.getDefault().post("setplaylistfragment")
        }

    }
    interface OnAdapterItemClickListener {
        fun onItemClicked(id: Int, expll:Int, songslist: List<Songdetails>, pos:Int)
    }
    override fun getItemCount(): Int {
        return list!!.size
    }


    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var pname: TextView
        init {
            pname = itemView.findViewById(R.id.tvplaylist)
        }

    }
    companion object {
        var ins: playlistadapter? = null
        fun getInstance(): playlistadapter? {
            return ins
        }
    }



}

