package com.example.gateway.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import java.util.*

open class ActivityManagePermission : AppCompatActivity() {
    private val KEY_PERMISSION = 200
    private var permissionResult: PermissionResult? = null
    private lateinit var permissionsAsk: Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * @param context    current Context
     * @param permission String permission to ask
     * @return boolean true/false
     */
    fun isPermissionGranted(
        context: Context?,
        permission: String?
    ): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(
            context!!, permission!!
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * @param context     current Context
     * @param permissions String[] permission to ask
     * @return boolean true/false
     */
    fun isPermissionsGranted(context: Context?, permissions: Array<String?>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        var granted = true
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    context!!,
                    permission!!
                ) != PackageManager.PERMISSION_GRANTED
            ) granted = false
        }
        return granted
    }

    private fun internalRequestPermission(permissionAsk: Array<String>) {
        var arrayPermissionNotGranted: Array<String?>
        val permissionsNotGranted = ArrayList<String>()
        for (i in permissionAsk.indices) {
            if (!isPermissionGranted(this@ActivityManagePermission, permissionAsk[i])) {
                permissionsNotGranted.add(permissionAsk[i])
            }
        }
        if (permissionsNotGranted.isEmpty()) {
            if (permissionResult != null) permissionResult!!.permissionGranted()
        } else {
            arrayPermissionNotGranted = arrayOfNulls(permissionsNotGranted.size)
            arrayPermissionNotGranted = permissionsNotGranted.toArray(arrayPermissionNotGranted)
            ActivityCompat.requestPermissions(
                this@ActivityManagePermission,
                arrayPermissionNotGranted,
                KEY_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != KEY_PERMISSION) {
            return
        }
        val permissionDenied: MutableList<String> = LinkedList()
        var granted = true
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                granted = false
                permissionDenied.add(permissions[i])
            }
        }
        if (permissionResult != null) {
            if (granted) {
                permissionResult!!.permissionGranted()
            } else {
                for (s in permissionDenied) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, s)) {
                        permissionResult!!.permissionForeverDenied()
                        return
                    }
                }
                permissionResult!!.permissionDenied()
            }
        }
    }

    /**
     * @param permission       String permission ask
     * @param permissionResult callback PermissionResult
     */
    fun askCompactPermission(permission: String, permissionResult: PermissionResult?) {
        permissionsAsk = arrayOf(permission)
        this.permissionResult = permissionResult
        internalRequestPermission(permissionsAsk)
    }

    /**
     * @param permissions      String[] permissions ask
     * @param permissionResult callback PermissionResult
     */
    fun askCompactPermissions(permissions: Array<String>, permissionResult: PermissionResult) {
        permissionsAsk = permissions
        this.permissionResult = permissionResult
        internalRequestPermission(permissionsAsk)
    }

    fun openSettingsApp(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            startActivityForResult(intent, REQUEST_PERMISSION_SETTING)
        }
    }

    companion object {
        const val REQUEST_PERMISSION_SETTING = 1000
    }
}