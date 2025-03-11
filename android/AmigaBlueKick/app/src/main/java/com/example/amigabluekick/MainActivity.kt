package com.example.amigabluekick

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    private lateinit var bleManager: BLEManager
    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btn4: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestBlePermissionsIfNeeded()

        bleManager = BLEManager(this)

        userPreferencesManager = UserPreferencesManager(this)
        val prefs = userPreferencesManager.getPreferences()

        btn1 = findViewById<Button>(R.id.btnkick1)
        btn2 = findViewById<Button>(R.id.btnkick2)
        btn3 = findViewById<Button>(R.id.btnkick3)
        btn4 = findViewById<Button>(R.id.btnkick4)

        btn1.setOnClickListener() { bleManager.send("0") }
        btn2.setOnClickListener() { bleManager.send("1") }
        btn3.setOnClickListener() { bleManager.send("2") }
        btn4.setOnClickListener() { bleManager.send("3") }

        bleManager.listener = { actual ->
            this.runOnUiThread {
                if (actual == null) {
                    findViewById<LinearLayout>(R.id.waitlayout).visibility = View.VISIBLE;
                    findViewById<LinearLayout>(R.id.connectedlayout).visibility = View.GONE;
                    requestBlePermissionsIfNeeded()
                } else {
                    findViewById<LinearLayout>(R.id.waitlayout).visibility = View.GONE;
                    findViewById<LinearLayout>(R.id.connectedlayout).visibility = View.VISIBLE;
                    val disabledTxColor = Color.parseColor("#000000")
                    val disabledBgColor = ColorStateList.valueOf(Color.parseColor("#808080"))
                    val selectedTxColor = Color.parseColor("#FFFFFF")
                    val selectedBgColor = ColorStateList.valueOf(Color.parseColor("#0000FF"))
                    btn1.backgroundTintList = disabledBgColor
                    btn1.setTextColor(disabledTxColor)
                    btn1.text = prefs.btn1Text
                    btn2.backgroundTintList = disabledBgColor
                    btn2.setTextColor(disabledTxColor)
                    btn2.text = prefs.btn2Text
                    btn3.backgroundTintList = disabledBgColor
                    btn3.setTextColor(disabledTxColor)
                    btn3.text = prefs.btn3Text
                    btn4.backgroundTintList = disabledBgColor
                    btn4.setTextColor(disabledTxColor)
                    btn4.text = prefs.btn4Text
                    if (actual == "0") {
                        btn1.backgroundTintList = selectedBgColor
                        btn1.setTextColor(selectedTxColor)
                    }
                    if (actual == "1") {
                        btn2.backgroundTintList = selectedBgColor
                        btn2.setTextColor(selectedTxColor)
                    }
                    if (actual == "2") {
                        btn3.backgroundTintList = selectedBgColor
                        btn3.setTextColor(selectedTxColor)
                    }
                    if (actual == "3") {
                        btn4.backgroundTintList = selectedBgColor
                        btn4.setTextColor(selectedTxColor)
                    }
                }
            }
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener() {
            val intent = Intent(
                this,
                UserPreferencesActivity::class.java
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = userPreferencesManager.getPreferences()
        btn1.text = prefs.btn1Text
        btn2.text = prefs.btn2Text
        btn3.text = prefs.btn3Text
        btn4.text = prefs.btn4Text
    }

    private fun requestBlePermissionsIfNeeded() {
        val neededPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            neededPermissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            neededPermissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            neededPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 1010)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            bleManager.startScan()
        } else {
            finish()
        }
    }
}