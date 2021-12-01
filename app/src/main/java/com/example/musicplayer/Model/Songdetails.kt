package com.example.musicplayer.Model

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*
import kotlin.collections.ArrayList

@RealmClass
open class Songdetails : RealmModel {
    @PrimaryKey
    var id: Long = 0
    var songname: String? = null
    var songartist: String? = null
    var songurl: String? = null
    var songimage: String? = null
    var date:Long?=null
    var albumname:String?=null
    var albumartist:String?=null

}
