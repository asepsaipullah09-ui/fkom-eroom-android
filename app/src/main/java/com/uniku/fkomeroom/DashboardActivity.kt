package com.uniku.fkomeroom

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.uniku.fkomeroom.network.ApiClient
import com.uniku.fkomeroom.network.ApiInterface
import com.uniku.fkomeroom.network.RoomsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("MISSING_DEPENDENCY_SUPERCLASS_WARNING")
class DashboardActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var rvRooms: RecyclerView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var apiInterface: ApiInterface
    private lateinit var btnRiwayat: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Inisialisasi views
        tvUserName = findViewById(R.id.tvUserName)
        tvUserRole = findViewById(R.id.tvUserRole)
        btnLogout = findViewById(R.id.btnLogout)
        btnRiwayat = findViewById(R.id.btnRiwayat)
        rvRooms = findViewById(R.id.rvRooms)

        // Inisialisasi
        sharedPref = getSharedPreferences("FKOM_eRoom", MODE_PRIVATE)
        apiInterface = ApiClient.createService(ApiInterface::class.java)

        // Setup RecyclerView
        rvRooms.layoutManager = LinearLayoutManager(this)

        // Tampilkan data user
        val nama = sharedPref.getString("nama", "User")
        val role = sharedPref.getString("role", "user")

        tvUserName.text = nama ?: "User"
        tvUserRole.text = when(role) {
            "admin" -> "Administrator"
            "dosen" -> "Dosen"
            "mahasiswa_pjmk" -> "Mahasiswa PJMK"
            "mahasiswa_umum" -> "Mahasiswa Umum"
            else -> "User"
        }

        // Logika Tampilan Berdasarkan Role
        val tvTitle = findViewById<TextView>(R.id.tvTitleDaftarRuangan)

        if (role == "admin") {
            // Jika Admin: Sembunyikan daftar ruangan untuk booking, ubah judul
            tvTitle.text = "Kelola Ruangan"
            // Nanti kita ganti RecyclerView ini jadi tombol Kelola Ruangan
            rvRooms.visibility = View.GONE

            // Tambah tombol Kelola Ruangan (Kita buat dinamis di sini biar cepat)
            val btnKelola = MaterialButton(this).apply {
                text = "️ Kelola Status Ruangan"
                setBackgroundColor(Color.parseColor("#FF9800"))
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(32, 32, 32, 32) }
                setOnClickListener {
                    startActivity(Intent(this@DashboardActivity, ManageRoomsActivity::class.java))
                }
            }
            // Tambahkan tombol ke layout utama
            findViewById<LinearLayout>(R.id.layoutDashboard)?.addView(btnKelola)

        } else {
            // Jika Mahasiswa/Dosen: Tampilkan daftar ruangan seperti biasa
            tvTitle.text = "Daftar Ruangan"
            rvRooms.visibility = View.VISIBLE
            fetchRooms()
        }

        // Logout
        btnLogout.setOnClickListener {
            logout()
        }

        // Riwayat
        btnRiwayat.setOnClickListener {
            val intent = Intent(this, MyBookingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchRooms() {
        val token = sharedPref.getString("token", null)
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            logout()
            return
        }

        apiInterface.getRooms("Bearer $token").enqueue(object : Callback<RoomsResponse> {
            override fun onResponse(call: Call<RoomsResponse>, response: Response<RoomsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val roomsResponse = response.body()!!

                    if (roomsResponse.success && roomsResponse.data.isNotEmpty()) {
                        val adapter = RoomAdapter(roomsResponse.data)
                        rvRooms.adapter = adapter
                        rvRooms.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@DashboardActivity, "Tidak ada data ruangan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@DashboardActivity, "Gagal mengambil data ruangan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RoomsResponse>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun logout() {
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()

        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}