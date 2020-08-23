package com.sriharrsha.bubbletrack

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sriharrsha.bubbletrack.adapters.ProductAdapter
import com.sriharrsha.bubbletrack.models.Product
import java.time.Duration
import java.util.concurrent.TimeUnit


const val TAG: String = "Price Guru";


var mProjectionManager: MediaProjectionManager? = null;

class AppActivity : AppCompatActivity() {
    private val CODE_PROJECTION_PERMISSION = 2014
    private var activateDrawSwitch: Switch? = null;
    private var trackList: RecyclerView? = null;

    //Firebase Connection for RecyclerView
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionRef: CollectionReference = db.collection("products")
    private var trackListAdapter: ProductAdapter? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)
        activateDrawSwitch = this.findViewById(R.id.activateDrawSwitch);
        trackList = this.findViewById(R.id.trackList);

        // call for the projection manager
        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?;
        initializeView();
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(Constants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, Constants.PERIODIC_WORK_REQUEST);
    }

    private fun setupRecyclerView() {
        val query: Query = collectionRef

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Product.class instructs the adapter to convert each DocumentSnapshot to a Product object
        val options: FirestoreRecyclerOptions<Product> = FirestoreRecyclerOptions.Builder<Product>()
            .setQuery(query, Product::class.java)
            .build()

        this.trackListAdapter =
            ProductAdapter(options)
        this.trackList?.setHasFixedSize(true)
        this.trackList?.layoutManager=LinearLayoutManager(this)
        this.trackList?.adapter = this.trackListAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == CODE_PROJECTION_PERMISSION){
            if(resultCode ==  RESULT_OK){
                val service = Intent(this, ForegroundService::class.java)
                service.putExtra("code", resultCode)
                service.putExtra("data", data)
                startForegroundService(service)
            }else{
                Toast.makeText(applicationContext, "Cast Permissions are not Given", Toast.LENGTH_SHORT).show()
                activateDrawSwitch?.isChecked = false;
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun initializeView() {
        activateDrawSwitch?.setOnCheckedChangeListener { _, isChecked ->
            val serviceIntent = Intent(this, ForegroundService::class.java)
            serviceIntent.putExtra("inputExtra", "Tap to Start");
            if (isChecked) {
                startActivityForResult(mProjectionManager?.createScreenCaptureIntent(), CODE_PROJECTION_PERMISSION)
            } else {
                stopService(serviceIntent);
            }
        }
        setupRecyclerView();
    }

    override fun onStart() {
        super.onStart()
        trackListAdapter?.startListening();
    }

    override fun onStop() {
        super.onStop()
        trackListAdapter?.stopListening()
    }
}
