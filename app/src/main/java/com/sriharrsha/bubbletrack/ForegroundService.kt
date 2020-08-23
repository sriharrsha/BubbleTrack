package com.sriharrsha.bubbletrack

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.nio.ByteBuffer
import java.util.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition


var mWidth = 0
var mHeight = 0
var mImageReader: ImageReader? = null
var mProjection: MediaProjection? = null
var detector = TextRecognition.getClient()
private lateinit var cloudFunctions: FirebaseFunctions


class ForegroundService : Service() {
    var mWindowManager: WindowManager? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mDensity = 0
    private val mRotation = 0
    private val SCREENCAP_NAME = " Bubble Tracker Screen Capture"
    private val VIRTUAL_DISPLAY_FLAGS: Int =
        android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
    private var mFloatingView: View? = null
    private val CHANNEL_ID = "Foreground Service Bubble Tracker"


    fun startService(context: Context, message: String) {
        val startIntent = Intent(context, ForegroundService::class.java)
        startIntent.putExtra("inputExtra", message)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { context.startForegroundService(startIntent);} else { context.startService(startIntent); }
    }
    fun stopService(context: Context) {
        val stopIntent = Intent(context, ForegroundService::class.java)
        context.stopService(stopIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }

    override fun onCreate() {
        super.onCreate()

        // [START initialize_functions_instance]
        cloudFunctions = Firebase.functions("asia-east2")
        // [END initialize_functions_instance]

        //Inflate the floating view layout we created
        mFloatingView = LayoutInflater.from(this).inflate(
            R.layout.layout_floating_view,
            null
        )

        val layoutFlag: Int = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        params.gravity =
            Gravity.TOP or Gravity.START //Initially view will be added to top-left corner

        params.x = 0
        params.y = 200

        //Add the view to the window
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mFloatingView, params)

        //The root element of the collapsed view layout
        val collapsedView =
            mFloatingView!!.findViewById<View>(R.id.collapse_view)

        //The root element of the expanded view layout
        val expandedView =
            mFloatingView!!.findViewById<View>(R.id.expanded_container)


        //Set the close button
        val closeButtonCollapsed =
            mFloatingView!!.findViewById<View>(R.id.close_btn) as ImageView
        closeButtonCollapsed.setOnClickListener { //close the service and remove the from from the window
            stopSelf()
        }

        val trackButton =
            mFloatingView!!.findViewById<View>(R.id.thirty_button) as Button
                trackButton.setOnClickListener {
                    setDetector()
                    scanDisplay()
                    Toast.makeText(
                        applicationContext,
                        "Scanning this product to track",
                        Toast.LENGTH_SHORT
                    ).show()
                    //scrapeSite("https://www.flipkart.com/")
                    //Call this everytime to refresh the Producer-Consumer Issue for BufferReader.
                    stopDisplay()
                }

        //Drag and move floating view using user's touch action.
        mFloatingView!!.findViewById<View>(R.id.root_container)
            .setOnTouchListener(object : OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {

                            //remember the initial position.
                            initialX = params.x
                            initialY = params.y

                            //get the touch location
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            val Xdiff = (event.rawX - initialTouchX).toInt()
                            val Ydiff = (event.rawY - initialTouchY).toInt()
                            //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                            //So that is click event.
                            if (Xdiff < 10 && Ydiff < 10) {
                                if (isViewCollapsed()) {
                                    //When user clicks on the image view of the collapsed layout,
                                    //visibility of the collapsed layout will be changed to "View.GONE"
                                    //and expanded view will become visible.
                                    collapsedView.visibility = View.GONE
                                    expandedView.visibility = View.VISIBLE
                                } else {
                                    collapsedView.visibility = View.VISIBLE
                                    expandedView.visibility = View.GONE
                                }
                            }
                            //get the touch location
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            //Calculate the X and Y coordinates of the view.
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()


                            //Update the layout with new X & Y coordinate
                            mWindowManager!!.updateViewLayout(mFloatingView, params)
                            return true
                        }
                    }
                    return false
                }
            })
    }

    private fun setDetector() {
        //Detect Current App
        val currentAppPackageName = retriveAppInForeground();
        Toast.makeText(applicationContext, "Top App Name : $currentAppPackageName", Toast.LENGTH_SHORT).show()
    }

    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private fun isViewCollapsed(): Boolean {
        return mFloatingView == null || mFloatingView!!.findViewById<View>(R.id.collapse_view)
            .visibility == View.VISIBLE
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) mWindowManager!!.removeView(mFloatingView)

        if(mProjection!=null){
            mProjection!!.stop()
            mProjection = null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //do heavy work on a background thread
        val input = intent?.getStringExtra("inputExtra")
        val mResultCode = intent?.getIntExtra("code", -1)
        val mResultData = intent?.getParcelableExtra<Intent>("data")

        createNotificationChannel()
        val pendingIntent = PendingIntent.getForegroundService(
            this,
            0, Intent(this, ForegroundService::class.java), 0
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Open Any Product Page you Like.")
            .setContentText(input)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)


        if (mResultCode != null && mResultData != null) {
            mProjection = mProjectionManager?.getMediaProjection(mResultCode, mResultData)
            Log.e(TAG, "mMediaProjection created: $mProjection")
        }
        //stopSelf();
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    /****************************************** Factoring Virtual Display creation  */
    @SuppressLint("WrongConstant")
    private fun scanDisplay() {
        if(mProjection != null){
            mWindowManager =
                getSystemService(Context.WINDOW_SERVICE) as WindowManager
            // display metrics
            val metrics: DisplayMetrics = resources.displayMetrics
            mDensity = metrics.densityDpi

            // get width and height
            if (Build.VERSION.SDK_INT >= 30) {
                mWidth = mWindowManager!!.currentWindowMetrics.bounds.width()
                mHeight = mWindowManager!!.currentWindowMetrics.bounds.height()
            }else{
                mWidth = metrics.widthPixels
                mHeight = metrics.heightPixels
            }

            Log.i(TAG,"mWidth ${mWidth} mHeight ${mHeight}")

            // start capture reader
            mImageReader =
                ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2)

            mVirtualDisplay = mProjection?.createVirtualDisplay(
                SCREENCAP_NAME,
                mWidth,
                mHeight,
                mDensity,
                VIRTUAL_DISPLAY_FLAGS,
                mImageReader!!.surface,
                null,
                null
            )

            mImageReader!!.setOnImageAvailableListener(ImageAvailableListener(), null)
        }
    }

    fun scrapeSite(siteUrl:String){
        val doc: org.jsoup.nodes.Document? = Jsoup.connect("https://en.wikipedia.org/").get()
        Log.i(TAG, "Doc Title: " + doc!!.title())
        val newsHeadlines: Elements = doc!!.select("#mp-itn b a")
        for (headline in newsHeadlines) {
            Log.i(TAG, "Headline Title: " +headline.attr("title"))
        }
    }

    //Call this everytime to refresh the Producer-Consumer Issue for BufferReader.
    private fun stopDisplay() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        mVirtualDisplay = null
    }


    private fun retriveAppInForeground(): String? {
        var currentApp: String? = null
        val usm = this.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val appList: List<UsageStats>?
        appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time)
        if (appList != null && appList.isNotEmpty()) {
            val sortedMap = TreeMap<Long, UsageStats>()
            for (usageStats in appList) {
                sortedMap.put(usageStats.lastTimeUsed, usageStats)
            }
            currentApp = sortedMap.takeIf { it.isNotEmpty() }?.lastEntry()?.value?.packageName
        }
        Log.i("ActivityTAG", "Application in foreground: " + currentApp)
        return currentApp
    }

    class ImageAvailableListener : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(mImageReader: ImageReader) {
            Log.i(TAG,"Image Available: imageReader $mImageReader")
            takeScreenshot(mImageReader)
            mImageReader.setOnImageAvailableListener(null, null)
        }

        //private fun searchForProduct(text: String): Task<String> {
            // Create the arguments to the callable function.
            //            val data = hashMapOf(
            //                "text" to text
            //            )
            //
            //            return cloudFunctions
            //                .getHttpsCallable("search")
            //                .call(data)
            //                .continueWith { task ->
            //                    // This continuation runs on either success or failure, but if the task
            //                    // has failed then result will throw an Exception which will be
            //                    // propagated down.
            //                    val result = task.result?.data as String
            //                    result
            //                }
        //}

        private fun takeScreenshot(mImageReader:ImageReader){
            val image = mImageReader.acquireLatestImage()
            try{
                val planes: Array<Image.Plane> = image.planes
                val buffer: ByteBuffer = planes[0].buffer
                val pixelStride: Int = planes[0].pixelStride
                val rowStride: Int = planes[0].rowStride
                val rowPadding: Int = rowStride - pixelStride * mWidth
                // create bitmap
                val bmp = Bitmap.createBitmap(
                    mWidth + rowPadding / pixelStride,
                    mHeight,
                    Bitmap.Config.ARGB_8888
                )
                bmp.copyPixelsFromBuffer(buffer)
                Log.i(TAG,"BitMap Available: bitmap $bmp")

                val visionImage = InputImage.fromBitmap(bmp, 0)
                //Extracting Text from Image
                detector.process(visionImage)
                    .addOnSuccessListener { firebaseVisionText ->
                        // Task completed successfully
                        Log.i(TAG, "Result After Processing: ${firebaseVisionText.text}")
//                        searchForProduct(firebaseVisionText.text.trim()).addOnCompleteListener(
//                                OnCompleteListener { task ->
//                                if (!task.isSuccessful) {
//                                    val e = task.exception
//                                    if (e is FirebaseFunctionsException) {
//                                        val code = e.code
//                                        val details = e.details
//                                    }
//                                }
//                                val result = task.result
//                                Log.i(TAG, "Search Results From Cloud: $result");
//                            });
                    }
                    .addOnFailureListener { error ->
                        // Task failed with an exception
                        Log.e(TAG, "Unable to Extract Text : $error")
                    }
                image.close()
            }catch (e: Exception){
                Log.e(TAG, "$e")
            }
        }
    }
}