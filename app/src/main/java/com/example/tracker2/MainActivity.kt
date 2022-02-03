package com.example.tracker2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.tracker2.ui.main.SectionsPagerAdapter
import com.example.tracker2.databinding.ActivityMainBinding
import com.example.tracker2.ui.main.ItemFragment
import com.example.tracker2.ui.main.ItemFragment.Companion.theAdapter
import com.example.tracker2.ui.main.ItemFragment.Companion.theList
import com.example.tracker2.ui.main.MainFragment
import com.google.android.gms.location.*


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    lateinit var sectionsPagerAdapter : SectionsPagerAdapter
    lateinit var fragmentList: ItemFragment
    lateinit var fragmentMain: MainFragment
    private lateinit var binding: ActivityMainBinding
    var isDark = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isDark = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("isDark", false)
        if (!isDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        LOCATION_REFRESH_TIME = PreferenceManager.getDefaultSharedPreferences(applicationContext).getInt("locationRefreshTime", 500)
        Toast.makeText(applicationContext, "REFRESH TIME "+ LOCATION_REFRESH_TIME, Toast.LENGTH_LONG).show()
        fragmentMain = MainFragment()
        fragmentList = ItemFragment()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, fragmentMain, fragmentList)
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
                for (location in p0.locations){
                    theList.add(0, Pair(theList.size+1, location.toString()))
                    theAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
    }

    fun toggleService() {
        if (!isLocationServiceRunning){
            Toast.makeText(this, "starting location service", Toast.LENGTH_SHORT).show()
            startLocationUpdates()
        } else {
            stopLocationUpdates()
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
        fusedLocationClient.requestLocationUpdates(currentLocationRequest,
            locationCallback,
            Looper.getMainLooper())
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

    companion object {
        var isLocationServiceRunning = false
        var LOCATION_REFRESH_TIME = 500
    }

}