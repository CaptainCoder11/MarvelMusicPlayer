package com.example.musicplayer.Extensions

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import com.example.musicplayer.R
import kotlin.random.Random


fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    var intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}
fun getnothumb():Int{
    return R.mipmap.no3
}
fun Any.log(){
    if(this is String){
        Log.i("Okaycheck:",this)
    }
    else{
        Log.i("Okaycheck:",this.toString())
    }
}
