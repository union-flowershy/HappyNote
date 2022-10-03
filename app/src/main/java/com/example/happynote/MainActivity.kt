package com.example.happynote

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var newAppVersion : Long = 0
    private var toolbarImgCount : Long = 0
    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    private val configSettings = remoteConfigSettings {
        //minimumFetchIntervalInSeconds = 3600
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task -> checkVersion(task.isSuccessful)
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                    Toast.makeText(this, "Fetch and activate succeeded",
                        Toast.LENGTH_SHORT).show()
                    Log.e("new_app_version", " = " + remoteConfig.getLong("new_app_version"))
                    Log.e("toolbar_img_count", " = " +  remoteConfig.getLong("toolbar_img_count"))
                } else {
                    Toast.makeText(this, "Fetch failed",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkVersion(successful: Boolean) {

        if(successful) {
            newAppVersion = remoteConfig.getLong("new_app_version")
            toolbarImgCount = remoteConfig.getLong("toolbar_img_count")

            try {
                val pi : PackageInfo = packageManager.getPackageInfo(packageName, 0)
                val appVersion : Long
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    appVersion = pi.longVersionCode
                }else {
                    appVersion = pi.versionCode.toLong()
                }

                if(newAppVersion > appVersion) {
                    val builder = AlertDialog.Builder(this)
                        .setTitle("업데이트 알림.")
                        .setMessage("최신버전이 등록되었습니다. \n업데이트 하세요")
                        .setCancelable(false)
                        .setPositiveButton("업데이트", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setData(Uri.parse("market://details?id=com.example.happynote"))  //플레이 스토어 아이디 등록
                                startActivity(intent)
                                Toast.makeText(applicationContext,"업데이트 버튼 클릭됨",Toast.LENGTH_SHORT).show()
                                dialog?.cancel()
                            }
                        })
                    val alertDialog : AlertDialog = builder.create()
                    alertDialog.show()
                } else {
                    val builder = AlertDialog.Builder(this).setMessage("newAppVersion > appVersion에서 에러 발생")
                    val alertDialog : AlertDialog = builder.create()
                    alertDialog.show()
                }
        }catch (e : PackageManager.NameNotFoundException) {
                    e.printStackTrace()
            }
        } else {
            val builder = AlertDialog.Builder(this).setMessage("successful에서 에러 발생")
            val alertDialog : AlertDialog = builder.create()
            alertDialog.show()
        }
    }
}