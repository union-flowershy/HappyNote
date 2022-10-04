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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private var newAppVersion : Long = 1
    private var toolbarImgCount : Long = 15
    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    private val configSettings = remoteConfigSettings {
        //minimumFetchIntervalInSeconds = 3600
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getRemoteConfig()

    }
    private fun getRemoteConfig() {

        FirebaseApp.initializeApp(this)

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this, object : OnCompleteListener<Boolean> {
                override fun onComplete(task: Task<Boolean>) {
                    newAppVersion = remoteConfig.getLong("new_app_version")
                    toolbarImgCount = remoteConfig.getLong("toolbar_img_count")
                    checkVersion()
                }
            })
    }

    private fun checkVersion() {
        try {
            val pi : PackageInfo = packageManager.getPackageInfo(packageName, 0)
            val appVersion : Long
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appVersion = pi.longVersionCode
            }else {
                appVersion = pi.versionCode.toLong()
            }

            if(newAppVersion > appVersion) {
                updateDialog()
                return
            }
//            else {
//                val builder = AlertDialog.Builder(this).setMessage("newAppVersion > appVersion에서 에러 발생")
//                val alertDialog : AlertDialog = builder.create()
//                alertDialog.show()
//            }

            //2022-10-04 툴바 ㅣ미지 다운로드 메소드 정의
//            checkToolbarImages()

        }catch (e : PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun updateDialog() {
        val builder = AlertDialog.Builder(this)
            .setTitle("업데이트 알림.")
            .setMessage("최신버전이 등록되었습니다. \n업데이트 하세요")
            .setCancelable(false)
            .setPositiveButton("업데이트", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(Uri.parse("market://details?id=com.example.happynote"))  //플레이 스토어 아이디 등록
                    startActivity(intent)
                    Toast.makeText(applicationContext, "업데이트 버튼 클릭됨", Toast.LENGTH_SHORT).show()
                    dialog?.cancel()
                }
            })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

//    private fun checkToolbarImages() {
//        val file : File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/toolbar_images")    //디렉토리 경로 불러오는 메소드
//        if(file?.isDirectory!!) {   //isDirectory가 false이면
//            file.mkdir();   //디렉토리 생성
//        }
//
//        val toolbarImgList : MutableList<File?> = ArrayList()
//        toolbarImgList.addAll(object : ArrayList(Arrays.asList(file.listFiles()))    // 모든 이미지를 불러와서 툴바 이미지에 저장함
//
//        }


}
