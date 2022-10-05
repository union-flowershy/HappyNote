package com.example.happynote

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Firebase 익명 계정
    private var newAppVersion : Long = 1
    private var toolbarImgCount : Long = 15
    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    private val configSettings = remoteConfigSettings {
        //minimumFetchIntervalInSeconds = 3600
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        onStart()
        signInAnonymously()
        FirebaseApp.initializeApp(this)
        getRemoteConfig()

    }

//    // [START on_start_check_user]
//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        updateUI(currentUser)
//    }
//    // [END on_start_check_user]

        public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
//            Toast.makeText(this, "if에서 성공", Toast.LENGTH_LONG).show()
//            signInAnonymously()
        }else {
            Toast.makeText(this, "if에서 실패", Toast.LENGTH_LONG).show()
            updateUI(currentUser)
            signInAnonymously()
        }



    }

    private fun getRemoteConfig() {

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

            //2022-10-04 툴바 이미지 다운로드 메소드 정의
            checkToolbarImages()
        }catch (e : PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun checkToolbarImages() {
        val file : File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/toolbar_images")    //디렉토리 경로 불러오는 메소드
        if(file?.isDirectory!!) {   //isDirectory가 false이면
            file.mkdir();   //디렉토리 생성
        }

        val toolbarImgList : MutableList<File> = ArrayList()
        toolbarImgList.addAll(ArrayList(Arrays.asList(*file.listFiles()!!)))    // 모든 이미지를 불러와서 툴바 이미지에 저장함

        if(toolbarImgList.size < toolbarImgCount) {
            val storage = Firebase.storage
            val storageRef = storage.reference
            downloadToolbarImg(storageRef)
        }
    }

    private fun downloadToolbarImg(storageRef: StorageReference) {

        val fileName : String = "toolbar_" + "0" + ".jpg" // toolbar_0.jpg
        val fileDir : File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/toolbar_images") // 파일 디렉토리 저장 경로
        val downloadFile : File = File(fileDir, fileName)// 실제로 다운로드 받을 파일 생성, 예) 탐색기에서 새로운 파일을 만드는걸로 생각하면 된다
        val downloadRef : StorageReference = storageRef
            .child("toolbar_images/" + "toolbar_" + "0" + ".jpg")   //스토리지에 어떤 파일을 내려 받을지 설정, toolbar_images/toolbar_0.jpg

        // 스토라지 로컬 데이터 다운로드 소스
        storageRef.getFile(downloadFile).addOnSuccessListener {

           fun onSuccess(taskSnapshot : FileDownloadTask.TaskSnapshot) {
                Log.e("onSuccess", downloadFile.path)
        }

        }.addOnFailureListener {

            fun onFailure(exception : Exception) {

            }
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

    private fun signInAnonymously() {
        // signInAnonymously를 호출하여 익명 사용자로 로그인
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "익명 인증 성공", Toast.LENGTH_LONG).show()
                    val user = task.result?.user
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "익명 인증 실패", Toast.LENGTH_LONG).show()
                }
            }
    }

//    private fun updateUI(user: FirebaseUser?) {
//        //No-op
//    }

    private fun updateUI(user: FirebaseUser?) { //update ui code here
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val TAG = "AnonymousAuth"
    }

}
