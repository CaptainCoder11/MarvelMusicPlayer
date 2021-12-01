package com.example.musicplayer.Model

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class AlbumDB : RealmModel {
    var songname: RealmList<String>? = null
    var songartist: RealmList<String>? = null
    var songurl: String? = null
    var albumname:String?=null
    var albumartist:String?=null

}
