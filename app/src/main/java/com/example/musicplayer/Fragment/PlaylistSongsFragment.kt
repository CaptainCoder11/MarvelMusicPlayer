package com.example.musicplayer.Fragment

import `in`.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.example.musicplayer.R
import com.example.musicplayer.Model.Songdetails
import com.example.musicplayer.adapter.PlaylistSongsadapter
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
//import wseemann.media.FFmpegMediaMetadataRetriever
import kotlin.collections.ArrayList




// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class PlaylistSongsFragment : Fragment(){

    var rvsongs: IndexFastScrollRecyclerView? = null
    var images = ArrayList<String>()
    var realm:Realm?=null
    val timerHandler = Handler()
    var updater:Runnable?=null
    var prevpos:Int?=null
    var byteArray:ByteArray?=null
    var results:RealmResults<Songdetails>?=null
    var urlsss:String?=null
    internal var songsAdapter: PlaylistSongsadapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ins = this
        Realm.init(context)
        realm = Realm.getDefaultInstance()

    }

    @SuppressLint("WrongConstant")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_playlist_det, container, false)
        realm = Realm.getDefaultInstance()
        rvsongs = view.findViewById(R.id.rvplaylistsongs)
        var query = realm!!.where(Songdetails::class.java)
        results = query.findAll().sort("date", Sort.DESCENDING)
        songsAdapter = PlaylistSongsadapter(results!!,activity!!)
        val songslayoutManager = LinearLayoutManager(context)
        songslayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvsongs!!.layoutManager = songslayoutManager
        rvsongs!!.setHasFixedSize(true)
        rvsongs!!.adapter = songsAdapter
        rvsongs!!.setIndexBarVisibility(false)
        rvsongs!!.setIndexbarHighLateTextColor("#03A9F4")
        rvsongs!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    var updater: Runnable? = null
                    updater = object : Runnable {
                        override fun run() {
                            rvsongs!!.setIndexBarVisibility(false)
                            rvsongs!!.invalidate()
                        }
                    }
                    timerHandler.postDelayed(updater, 11000)
                } else {
                    rvsongs!!.setIndexBarVisibility(true)
                }
            }
        })
   return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {

    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }


    override fun onDestroy() {
        super.onDestroy()
    }
    override fun onDetach()
    {
        super.onDetach()

    }


    companion object
    {
        fun newInstance(param1: String, param2: String): PlaylistSongsFragment {
            val fragment = PlaylistSongsFragment()
            val args = Bundle()
            return fragment
        }
        var ins: PlaylistSongsFragment? = null
        fun getInstance(): PlaylistSongsFragment? {
            return ins
        }
    }

}// Required empty public constructor
