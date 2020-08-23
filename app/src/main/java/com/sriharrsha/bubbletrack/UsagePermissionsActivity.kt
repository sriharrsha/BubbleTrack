package com.sriharrsha.bubbletrack

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast

class UsagePermissionsActivity : AppCompatActivity() {
    private val CODE_USAGE_ACCESS_PERMISSION = 3
    private var permGrantButton: Button? = null;
    private var nextButton: Button? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_usage_permissions)
        permGrantButton = this.findViewById(R.id.permGrantButton)
        nextButton = this.findViewById(R.id.nextButton)
        skipIfPermissionAlreadyGiven()
        setPermissionButton()
    }

    private fun skipIfPermissionAlreadyGiven() {
        if (checkUsageAccessPermission(this)) {
            startActivity(Intent(this, OverlayPermissionsActivity::class.java))
            finish()
        }
    }

    private fun setPermissionButton() {
        if (checkUsageAccessPermission(this)) {
            permGrantButton?.text = "Success"
            permGrantButton?.setOnClickListener {
                Toast.makeText(
                    this,
                    "Successfully Granted",
                    Toast.LENGTH_SHORT
                )
            }
            nextButton?.setOnClickListener { startActivity(Intent(this, OverlayPermissionsActivity::class.java)) }
        } else {
            permGrantButton?.setOnClickListener { setUsageAccessPermission() }
        }
    }

    //Check if the application has Usage Access to Detect the Shop App?
    //you have to ask for the permission in runtime.
    private fun checkUsageAccessPermission(context: Context): Boolean {
        try {
            val packageManager = context.packageManager
            val applicationInfo =
                packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager =
                context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOpsManager.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName)
            } else {
                appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName)
            }

            if(mode != AppOpsManager.MODE_ALLOWED){
                return false;
            }
            return true;
        } catch (e: PackageManager.NameNotFoundException) {
            return false;
        }
    }

    private fun setUsageAccessPermission()
    {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivityForResult(intent, CODE_USAGE_ACCESS_PERMISSION);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       if (requestCode == CODE_USAGE_ACCESS_PERMISSION) {
                setPermissionButton()
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    override fun onPause() {
        super.onPause()
        while(!checkUsageAccessPermission(this)){
            Log.d(TAG, "Usage Permission Not Given")
        }
        Log.d(TAG, "Usage Permission Given")
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
        startActivity(intent);
    }

    override fun onResume() {
        super.onResume()
        skipIfPermissionAlreadyGiven();
    }

}
