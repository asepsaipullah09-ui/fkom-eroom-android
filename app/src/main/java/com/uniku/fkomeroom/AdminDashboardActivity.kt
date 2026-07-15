package com.uniku.fkomeroom

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        sharedPref = getSharedPreferences("FKOM_eRoom", MODE_PRIVATE)
        val nama = sharedPref.getString("nama", "Admin")
        findViewById<TextView>(R.id.tvAdminName).text = nama

        // Tombol Kelola Ruangan
        findViewById<MaterialButton>(R.id.btnKelolaRuangan).setOnClickListener {
            startActivity(Intent(this, ManageRoomsActivity::class.java))
        }

        // Tombol Riwayat (untuk approval)
        findViewById<MaterialButton>(R.id.btnRiwayatBooking).setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        // Logout
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}