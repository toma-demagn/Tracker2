package com.example.tracker2

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.example.tracker2.databinding.ActivityMainBinding
import com.example.tracker2.ui.main.ItemFragment
import com.example.tracker2.ui.main.ItemFragment.Companion.theAdapter
import com.example.tracker2.ui.main.ItemFragment.Companion.theList
import com.example.tracker2.ui.main.MainFragment
import com.example.tracker2.ui.main.SectionsPagerAdapter
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    lateinit var fragmentList: ItemFragment
    lateinit var fragmentMain: MainFragment
    lateinit var fragmentCode: TrapsFragment
    private lateinit var binding: ActivityMainBinding
    var isDark = false
    lateinit var handlerUpdate: Handler
    var locationCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlerUpdate = Handler()
        UNIQUE_ID = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getInt("UNIQUE_ID", -1)
        if (UNIQUE_ID == -1) {
            UNIQUE_ID = generateUniqueId()
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                .putInt("UNIQUE_ID", UNIQUE_ID).apply()
        }
        isDark = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getBoolean("isDark", false)
        if (!isDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        LOCATION_UPDATE_TIME = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getInt("locationRefreshTime", 500)
        //Toast.makeText(applicationContext, "REFRESH TIME "+ LOCATION_REFRESH_TIME, Toast.LENGTH_LONG).show()
        Toast.makeText(applicationContext, "UNIQUE ID " + UNIQUE_ID, Toast.LENGTH_LONG).show()
        fragmentMain = MainFragment()
        fragmentList = ItemFragment()
        fragmentCode = TrapsFragment()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sectionsPagerAdapter =
            SectionsPagerAdapter(this, supportFragmentManager, fragmentMain, fragmentList, fragmentCode)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val view = findViewById<Button>(R.id.settings_button)
        view.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, SettingsActivity::class.java)
            intent.putExtra("isDark", isDark)
            startActivity(intent)
        })
        val textView = findViewById<TextView>(R.id.title)
        textView.setOnClickListener(View.OnClickListener {
            //fragmentList.addItem()
            theList.add(0, Pair(2, "zero"))
            theAdapter.notifyDataSetChanged()
        })
        if (!checkPermission()) {
            requestPermission()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    latitude = location.latitude
                    longitude = location.longitude
                    theList.add(0, Pair(locationCount, location.toString()))
                    if (theList.size > 40) {
                        theList.removeAt(theList.size - 1)
                    }
                    theAdapter.notifyDataSetChanged()
                    //LocationUpdateTask().execute()
                    locationCount++
                }
            }
        }
        val filter = IntentFilter("show")
        val br = MyBroadcastReceiver()
        registerReceiver(br, filter)
    }

    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            //Toast.makeText(applicationContext, "Ok service is running", Toast.LENGTH_SHORT).show()
            updateLocationRequest()
            handlerUpdate.postDelayed(this, LOCATION_UPDATE_TIME.toLong())
        }
    }

    private fun generateUniqueId(): Int {
        return Random.nextInt(0, Int.MAX_VALUE)
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val result1 = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
    }

    fun toggleService() {
        if (!isLocationServiceRunning) {
            connect()
            Toast.makeText(this, "starting location service", Toast.LENGTH_SHORT).show()
            startLocationUpdates()
            handlerUpdate.post(runnableCode)
        } else {
            disconnect()
            stopLocationUpdates()
            handlerUpdate.removeCallbacks(runnableCode)
        }
        isLocationServiceRunning = !isLocationServiceRunning
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val currentLocationRequest = LocationRequest()
        currentLocationRequest.setInterval(LOCATION_REFRESH_TIME.toLong())
            .setFastestInterval(0)
            .setMaxWaitTime(0)
            .setSmallestDisplacement(0f)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        fusedLocationClient.requestLocationUpdates(
            currentLocationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onResume() {
        super.onResume()
        if (isLocationServiceRunning)
            startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }


    private fun connect(){
        val json = "{\"uniqueDeviceId\":\"" + UNIQUE_ID + "\"}"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .method("POST", requestBody)
            .url(urlAPI)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("LOCATION_FAIL", "This is a failure")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                try {
                    var json1 = JSONObject(responseData)
                    println("Request Successful!!")
                    Log.d("CONNECTION_SUCCESS", json1.toString())
                    Log.d("CONNECTION_SUCCESS", json1.getInt("id").toString())
                    id = json1.getInt("id")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun disconnect(){
        val json = "{}"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toRequestBody(mediaType)
        val url = urlAPI + id + "/"
        val request = Request.Builder()
            .method("DELETE", requestBody)
            .url(url)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("LOCATION_FAIL", "This is a failure")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                try {
                    var json1 = JSONObject(responseData)
                    println("Request Successful!!")
                    Log.d("LOGOUT_SUCCESS", json1.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun updateLocationRequest() {
        val id = id
        if (id>0){
            val url = urlAPI + id + "/location"
            val json = "{\"latitude\":$latitude, \"longitude\":$longitude}"
            Log.d("JSON Location", json)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toRequestBody(mediaType)
            val request = Request.Builder()
                .method("POST", requestBody)
                .url(url)
                .build()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("LOCATION_FAIL", "This is a failure")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    try {
                        var json = JSONObject(responseData)
                        println("Location Request Successful!!")
                        Log.d("LOCATION_SUCCESS", json.toString())
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            })
        }
    }

    private val TAG = "MyBroadcastReceiver"

    class MyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Toast.makeText(context, intent.getStringExtra("msg"), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        val okHttpClient = OkHttpClient()
        var isLocationServiceRunning = false
        var LOCATION_UPDATE_TIME = 500
        var LOCATION_REFRESH_TIME = 100
        var id = 0
        var UNIQUE_ID = -1
        val urlAPI = "https://rtqtybnff0.execute-api.eu-west-3.amazonaws.com/dev/trackers/"
        var latitude = 0.0
        var longitude = 0.0
    }

}



