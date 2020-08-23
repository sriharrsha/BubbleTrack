package com.sriharrsha.bubbletrack

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class OverlayPermissionsActivity : AppCompatActivity() {
    private val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084
    private var permGrantButton: Button? = null;
    private var nextButton: Button? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_overlay_permissions)
        permGrantButton = this.findViewById(R.id.permGrantButton)
        nextButton = this.findViewById(R.id.nextButton)
        skipIfPermissionAlreadyGiven()
        setPermissionButton()
    }

    private fun skipIfPermissionAlreadyGiven() {
        if (checkOverlayPermission(this)) {
            startActivity(Intent(this, AppActivity::class.java))
            finish()
        }
    }

    private fun setPermissionButton() {
        if (checkOverlayPermission(this)) {
            permGrantButton?.text = "Success"
            permGrantButton?.setOnClickListener {
                Toast.makeText(
                    this,
                    "Successfully Granted",
                    Toast.LENGTH_SHORT
                )
            }
            nextButton?.setOnClickListener { startActivity(Intent(this, AppActivity::class.java)) }
        } else {
            permGrantButton?.setOnClickListener { setOverlayPermission() }
        }
    }

    //Check if the application has draw over other apps permission or not?
    //you have to ask for the permission in runtime.
    private fun checkOverlayPermission(context: Context): Boolean {
        if (!Settings.canDrawOverlays(context)) {
            return false;
        }
        return true;
    }

    private fun setOverlayPermission() {
        //If the draw over permission is not available open the settings screen
        //to grant the permission.
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            setPermissionButton()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onPause() {
        super.onPause()
        while(!checkOverlayPermission(this)){
            Log.d(TAG, "Overlay Permission Not Given")
        }
        Log.d(TAG, "Overlay Permission Given")
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
        startActivity(intent);
    }

    override fun onResume() {
        super.onResume()
        skipIfPermissionAlreadyGiven();
    }

}
