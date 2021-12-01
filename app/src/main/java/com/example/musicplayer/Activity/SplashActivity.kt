package com.example.musicplayer.Activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.example.musicplayer.Extensions.getSongList
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import io.realm.Realm
import io.realm.RealmConfiguration
import android.content.pm.PackageManager
import android.graphics.Color
import kotlinx.android.synthetic.main.activity_splash.*
import android.media.MediaPlayer.OnPreparedListener
import com.example.musicplayer.Extensions.cancelallwork
import com.example.musicplayer.R


class SplashActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        if(bool)
            checkpermission()
    }
 var bool = false
    override fun onPause() {
        super.onPause()
    }
    private fun checkWriteExternalPermission(): Boolean {
        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        val res = checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }
    private fun checkReadExternalPermission(): Boolean {
        val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        val res = checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }
var dialogShown = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        var mRealmConfiguration = RealmConfiguration.Builder()
                .name("Songdetails.realm")
                .schemaVersion(1) // skip if you are not managing
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.getInstance(mRealmConfiguration)
        Realm.setDefaultConfiguration(mRealmConfiguration)
        setContentView(R.layout.activity_splash)
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.thorlive)
        videovw.setVideoURI(uri)
        videovw.alpha = 0f
        videovw.setBackgroundColor(Color.TRANSPARENT);
        videovw.setZOrderOnTop(true);
        videovw.start()
        videovw.setOnPreparedListener(OnPreparedListener { mp -> mp.isLooping = true })
        checkpermission()
    }
    fun checkpermission() {
        cancelallwork()
        if(checkWriteExternalPermission()&&checkReadExternalPermission()){
            getSongList()
        }
        else{
       val request = permissionsBuilder(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).build()
// Send the request when you want.
        request!!.send()
        request!!.listeners {
            onAccepted {
                getSongList()
            }
            onDenied { }
            onPermanentlyDenied { showalert() }
            onShouldShowRationale { strings, permissionNonce ->
                permissionNonce.use()
            }
        }
        }
    }
    fun showalert() {
        if (dialogShown) {
            return
        } else {
            dialogShown = true
            var alert = AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Please allow Permissions for application to work properly")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            dialogShown = false
                            bool = true
                            var intent = Intent()
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            var uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri)
                            startActivity(intent);
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            finish()
                        }
                    })
            val alertDialog = alert.create()
            alertDialog!!.show()
        }
    }
}
