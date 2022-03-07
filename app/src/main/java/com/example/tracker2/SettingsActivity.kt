package com.example.tracker2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.tracker2.MainActivity.Companion.LOCATION_UPDATE_TIME
import com.example.tracker2.MainActivity.Companion.UNIQUE_ID

class SettingsActivity : AppCompatActivity() {

    var uniqueId: String = UNIQUE_ID
    var isDark = false
    lateinit var editText : EditText
    lateinit var editText2: EditText
    var locationRefreshTime: Int = LOCATION_UPDATE_TIME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val button = findViewById<Button>(R.id.validate)
        val button2 = findViewById<Button>(R.id.validateId)
        button.isClickable = false
        button2.isClickable = false
        editText = findViewById(R.id.editTextNumber)
        editText.setText(locationRefreshTime.toString())
        editText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                var nb = 0
                try {
                    nb = editText.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    // handler
                } finally {
                    if (nb>0 && nb != LOCATION_UPDATE_TIME){
                        locationRefreshTime = nb
                        button.isClickable = true
                    } else
                        button.isClickable = false
                }

            }
        })
        editText.clearFocus()
        editText2 = findViewById(R.id.editTextId)
        editText2.setText(UNIQUE_ID)
        editText2.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                val str = editText2.text.toString()
                if (str.length > 0 && str != UNIQUE_ID) {
                    uniqueId = str
                    button2.isClickable = true
                } else
                    button2.isClickable = false
            }
        })
        editText2.clearFocus()
        isDark = intent.getBooleanExtra("isDark", false)
        val sw1 = findViewById<Switch>(R.id.switch1)
        sw1.isChecked = isDark
        sw1?.setOnCheckedChangeListener { _, _ ->
            toggleDarkMode()
        }
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    fun toggleDarkMode() {
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
            .putBoolean("isDark", !isDark).apply()
        Toast.makeText(
            applicationContext,
            "Dark mode will update on next launch",
            Toast.LENGTH_SHORT
        ).show()
        if (!isDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        isDark = !isDark

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun changeValue(v: View){
        LOCATION_UPDATE_TIME = locationRefreshTime
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putInt("locationRefreshTime", locationRefreshTime).apply()
        v.isClickable = false
        Toast.makeText(this, "Location refresh time updated", Toast.LENGTH_SHORT).show()
    }

    fun changeId(v: View){
        UNIQUE_ID = uniqueId
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putString("UNIQUE_ID", UNIQUE_ID).apply()
        v.isClickable = false
        Toast.makeText(this, "Unique ID updated", Toast.LENGTH_SHORT).show()
    }

}