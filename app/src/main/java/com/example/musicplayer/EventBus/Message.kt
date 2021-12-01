package com.example.musicplayer.EventBus

import com.example.musicplayer.Model.Songdetails
import io.realm.RealmResults

class Message(val id: Int, val id2: Int, val results:RealmResults<Songdetails>, val position:Int)
class DelMessage(val pos: Int)