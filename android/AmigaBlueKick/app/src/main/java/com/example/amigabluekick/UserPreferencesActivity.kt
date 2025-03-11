package com.example.amigabluekick

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class UserPreferencesActivity : AppCompatActivity() {
    private lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_preferences)
        userPreferencesManager = UserPreferencesManager(this)
        val prefs = userPreferencesManager.getPreferences()

        val edtKick1 = findViewById<EditText>(R.id.edtKick1)
        val edtKick2 = findViewById<EditText>(R.id.edtKick2)
        val edtKick3 = findViewById<EditText>(R.id.edtKick3)
        val edtKick4 = findViewById<EditText>(R.id.edtKick4)
        edtKick1.setText(prefs.btn1Text)
        edtKick2.setText(prefs.btn2Text)
        edtKick3.setText(prefs.btn3Text)
        edtKick4.setText(prefs.btn4Text)

        findViewById<Button>(R.id.btndone).setOnClickListener() {
            prefs.btn1Text = edtKick1.text.toString()
            prefs.btn2Text = edtKick2.text.toString()
            prefs.btn3Text = edtKick3.text.toString()
            prefs.btn4Text = edtKick4.text.toString()
            userPreferencesManager.setPreferences(prefs)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}