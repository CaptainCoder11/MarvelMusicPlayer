package com.example.musicplayer.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.adapter.playlistadapter
import java.util.ArrayList


class Playlist : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        val recvw = view.findViewById<RecyclerView>(R.id.rec_playlist)
        playlistarr.add("Recently Added")
        var adapter = playlistadapter(playlistarr!!,activity!!)
        val songslayoutManager = LinearLayoutManager(context)
        songslayoutManager.orientation = LinearLayoutManager.VERTICAL
        recvw!!.layoutManager = songslayoutManager
        recvw!!.setHasFixedSize(true)
        recvw!!.adapter = adapter

        return view
    }

    // TODO: Rename method, update argument and hook method into UI event


    companion object {
        var playlistarr = ArrayList<String>()
    }
}
