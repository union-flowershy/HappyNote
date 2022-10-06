package com.example.happynote

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


//메모리 읽기, 쓰기 권한은 위험으로 간주하는 권한이기 때문에,
//사용자가 명시적으로 앱 액세스 권한을 부여해야 한다
//런타임에 사용자에게 해당 권한을 요청하기 위한 메시지를 표시해야하는 코드를 작성한다
class LoadActivity : AppCompatActivity() {

    private val permissionCheck : Int = 1
    private val permissionArr = arrayOf<String> (
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!hasPermissions(this, *permissionArr)){
            ActivityCompat.requestPermissions(this, permissionArr, permissionCheck);
        }
        else{
            // 메인 엑티비티 진입
        }
    }
    fun hasPermissions(context: Context?, vararg permissionArr: String?): Boolean {
        if (context != null && permissionArr != null) {
            for (permission in permissionArr) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }
    private fun getPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1000)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Build.VERSION.SDK_INT >= 23) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("permission", permissionArr[0] + "was " + grantResults[0])
                recreate()
            } else {
                Log.d("permission", "denied")
                Toast.makeText(
                    this@LoadActivity,
                    "앱을 사용하기 위해서는 메모리 접근 권한이 필요합니다.",
                    Toast.LENGTH_LONG
                ).show()
                getPermission()
            }
        }
    }


}
