package com.uniku.fkomeroom

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.uniku.fkomeroom.network.ApiClient
import com.uniku.fkomeroom.network.ApiInterface
import com.uniku.fkomeroom.network.RoomDetailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomDetailActivity : AppCompatActivity() {

    private lateinit var tvRoomName: TextView
    private lateinit var tvRoomCode: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvJenis: TextView
    private lateinit var tvLantai: TextView
    private lateinit var tvKapasitas: TextView
    private lateinit var tvDeskripsi: TextView
    private lateinit var layoutFasilitas: LinearLayout
    private lateinit var btnBack: ImageButton
    private lateinit var btnBooking: MaterialButton

    private lateinit var sharedPref: SharedPreferences
    private lateinit var apiInterface: ApiInterface
    private var roomId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        // Inisialisasi views
        tvRoomName = findViewById(R.id.tvRoomName)
        tvRoomCode = findViewById(R.id.tvRoomCode)
        tvStatus = findViewById(R.id.tvStatus)
        tvJenis = findViewById(R.id.tvJenis)
        tvLantai = findViewById(R.id.tvLantai)
        tvKapasitas = findViewById(R.id.tvKapasitas)
        tvDeskripsi = findViewById(R.id.tvDeskripsi)
        layoutFasilitas = findViewById(R.id.layoutFasilitas)
        btnBack = findViewById(R.id.btnBack)
        btnBooking = findViewById(R.id.btnBooking)

        // Inisialisasi
        sharedPref = getSharedPreferences("FKOM_eRoom", MODE_PRIVATE)
        apiInterface = ApiClient.createService(ApiInterface::class.java)

        // Ambil roomId dari intent
        roomId = intent.getIntExtra("room_id", 0)

        // Back button
        btnBack.setOnClickListener { finish() }

        // Booking button
        btnBooking.setOnClickListener {
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("room_id", roomId)
            intent.putExtra("room_name", tvRoomName.text.toString())
            startActivity(intent)
        }

        // Fetch detail ruangan
        fetchRoomDetail()
    }

    private fun fetchRoomDetail() {
        val token = sharedPref.getString("token", null)
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        apiInterface.getRoomDetail("Bearer $token", roomId).enqueue(object : Callback<RoomDetailResponse> {
            override fun onResponse(call: Call<RoomDetailResponse>, response: Response<RoomDetailResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val room = response.body()!!.data

                    tvRoomName.text = room.nama_ruangan
                    tvRoomCode.text = "Kode: ${room.kode_ruangan}"
                    tvStatus.text = room.status
                    tvJenis.text = room.jenis.replaceFirstChar { it.uppercase() }
                    tvLantai.text = room.lantai.toString()
                    tvKapasitas.text = "${room.kapasitas} Orang"
                    tvDeskripsi.text = room.deskripsi ?: "Tidak ada deskripsi"

                    // Warna status
                    when(room.status) {
                        "tersedia" -> tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
                        "maintenance" -> tvStatus.setBackgroundColor(Color.parseColor("#FF9800"))
                        "nonaktif" -> tvStatus.setBackgroundColor(Color.parseColor("#F44336"))
                    }

                    // Tampilkan fasilitas
                    layoutFasilitas.removeAllViews()
                    if (room.facilities != null && room.facilities.isNotEmpty()) {
                        for (facility in room.facilities) {
                            val textView = TextView(this@RoomDetailActivity)
                            textView.text = "• ${facility.nama_fasilitas}"
                            textView.textSize = 14f
                            textView.setTextColor(Color.parseColor("#666666"))
                            textView.setPadding(0, 4, 0, 4)
                            layoutFasilitas.addView(textView)
                        }
                    } else {
                        val textView = TextView(this@RoomDetailActivity)
                        textView.text = "Tidak ada fasilitas"
                        textView.textSize = 14f
                        textView.setTextColor(Color.parseColor("#999999"))
                        layoutFasilitas.addView(textView)
                    }
                } else {
                    Toast.makeText(this@RoomDetailActivity, "Gagal mengambil detail ruangan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RoomDetailResponse>, t: Throwable) {
                Toast.makeText(this@RoomDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}