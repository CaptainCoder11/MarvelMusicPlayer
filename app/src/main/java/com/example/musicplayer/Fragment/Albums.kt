package com.example.musicplayer.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Model.AlbumDB
import com.example.musicplayer.R
import com.example.musicplayer.adapter.Albumadapter
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_albums.view.*

class Albums : Fragment() {

    var realm:Realm?=null
    var albadapter:Albumadapter?=null
    var result: RealmResults<AlbumDB>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(context)
        realm = Realm.getDefaultInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_albums, container, false)
        val songslayoutManager = LinearLayoutManager(context)
        result = realm!!.where(AlbumDB::class.java).findAll()
        albadapter = Albumadapter(result!!, activity!!)
        songslayoutManager.orientation = GridLayoutManager.VERTICAL
        view.rvalb!!.layoutManager = GridLayoutManager(context,2)
        view.rvalb!!.setHasFixedSize(true)
        view.rvalb!!.adapter =  albadapter



        return view
    }

}
