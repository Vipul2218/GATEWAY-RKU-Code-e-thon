package com.example.gateway.activity


import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.gateway.R
import kotlinx.android.synthetic.main.activity_menu.*


class MenuActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        actMenuBtnWifiBluetooth.setOnClickListener(this)
        actMenuBtnRetrofit.setOnClickListener(this)
        actMenuBtnFirebase.setOnClickListener(this)
        actMenuBtnMap.setOnClickListener(this)
        actMenuBtnAccelerometerGyroscope.setOnClickListener(this)
//        val info: PackageInfo
//        try {
//            info = packageManager.getPackageInfo(
//                "com.example.gateway", PackageManager.GET_SIGNATURES
//            )
//            for (signature in info.signatures) {
//                var md: MessageDigest
//                md = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                val something: String = String(Base64.encode(md.digest(), 0))
//                Log.e("Hash key", something)
//                println("Hash key$something")
//            }
//        } catch (e1: NameNotFoundException) {
//            Log.e("name not found", e1.toString())
//        } catch (e: NoSuchAlgorithmException) {
//            Log.e("no such an algorithm", e.toString())
//        } catch (e: Exception) {
//            Log.e("exception", e.toString())
//        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.actMenuBtnWifiBluetooth -> {
                val intent = Intent(this, ConnectionActivity::class.java)
                startActivity(intent)
            }
            R.id.actMenuBtnRetrofit -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.actMenuBtnFirebase -> {
                val intent = Intent(this, FirebaseActivity::class.java)
                startActivity(intent)
            }
            R.id.actMenuBtnMap -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }
            R.id.actMenuBtnAccelerometerGyroscope -> {
                val intent = Intent(this, SensorActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
