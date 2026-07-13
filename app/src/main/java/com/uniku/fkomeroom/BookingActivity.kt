package com.uniku.fkomeroom

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.uniku.fkomeroom.network.ApiClient
import com.uniku.fkomeroom.network.ApiInterface
import com.uniku.fkomeroom.network.BookingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class BookingActivity : AppCompatActivity() {

    private lateinit var tvRoomInfo: View
    private lateinit var spinnerKategori: Spinner
    private lateinit var etTanggal: TextInputEditText
    private lateinit var etJamMulai: TextInputEditText
    private lateinit var etJamSelesai: TextInputEditText
    private lateinit var etJumlahPeserta: TextInputEditText
    private lateinit var etTujuan: TextInputEditText
    private lateinit var btnSubmit: MaterialButton
    private lateinit var btnBack: ImageButton

    private lateinit var sharedPref: SharedPreferences
    private lateinit var apiInterface: ApiInterface
    private var roomId: Int = 0
    private var roomName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        // Inisialisasi views
        tvRoomInfo = findViewById(R.id.tvRoomInfo)
        spinnerKategori = findViewById(R.id.spinnerKategori)
        etTanggal = findViewById(R.id.etTanggal)
        etJamMulai = findViewById(R.id.etJamMulai)
        etJamSelesai = findViewById(R.id.etJamSelesai)
        etJumlahPeserta = findViewById(R.id.etJumlahPeserta)
        etTujuan = findViewById(R.id.etTujuan)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)

        // Inisialisasi
        sharedPref = getSharedPreferences("FKOM_eRoom", MODE_PRIVATE)
        apiInterface = ApiClient.createService(ApiInterface::class.java)

        // Ambil data dari intent
        roomId = intent.getIntExtra("room_id", 0)
        roomName = intent.getStringExtra("room_name") ?: "Ruangan"

        tvRoomInfo.findViewById<TextView>(R.id.tvRoomInfo).text = roomName

        // Setup Spinner Kategori
        val kategoriList = arrayOf("perkuliahan", "praktikum", "rapat", "seminar", "lainnya")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKategori.adapter = adapter

        // Back button
        btnBack.setOnClickListener { finish() }

        // Date Picker
        etTanggal.setOnClickListener { showDatePicker() }

        // Time Picker Jam Mulai
        etJamMulai.setOnClickListener { showTimePicker(etJamMulai) }

        // Time Picker Jam Selesai
        etJamSelesai.setOnClickListener { showTimePicker(etJamSelesai) }

        // Submit button
        btnSubmit.setOnClickListener { submitBooking() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
            etTanggal.setText(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hourOfDay, minute ->
            val time = String.format("%02d:00", hourOfDay)
            editText.setText(time)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun submitBooking() {
        val tanggal = etTanggal.text.toString().trim()
        val jamMulai = etJamMulai.text.toString().trim()
        val jamSelesai = etJamSelesai.text.toString().trim()
        val jumlahPeserta = etJumlahPeserta.text.toString().trim()
        val tujuan = etTujuan.text.toString().trim()
        val kategori = spinnerKategori.selectedItem.toString()

        // Validasi
        if (tanggal.isEmpty()) {
            Toast.makeText(this, "Pilih tanggal!", Toast.LENGTH_SHORT).show()
            return
        }

        if (jamMulai.isEmpty()) {
            Toast.makeText(this, "Pilih jam mulai!", Toast.LENGTH_SHORT).show()
            return
        }

        if (jamSelesai.isEmpty()) {
            Toast.makeText(this, "Pilih jam selesai!", Toast.LENGTH_SHORT).show()
            return
        }

        if (jumlahPeserta.isEmpty()) {
            Toast.makeText(this, "Masukkan jumlah peserta!", Toast.LENGTH_SHORT).show()
            return
        }

        if (tujuan.isEmpty()) {
            Toast.makeText(this, "Masukkan tujuan!", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "Loading..."

        val token = sharedPref.getString("token", null)
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val data = HashMap<String, Any>()
        data["room_id"] = roomId
        data["kategori"] = kategori
        data["tanggal"] = tanggal
        data["jam_mulai"] = jamMulai
        data["jam_selesai"] = jamSelesai
        data["tujuan"] = tujuan
        data["jumlah_peserta"] = jumlahPeserta.toInt()

        apiInterface.createBooking("Bearer $token", data).enqueue(object : Callback<BookingResponse> {
            override fun onResponse(call: Call<BookingResponse>, response: Response<BookingResponse>) {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Ajukan Booking"

                if (response.isSuccessful && response.body() != null) {
                    val bookingResponse = response.body()!!

                    if (bookingResponse.success) {
                        Toast.makeText(
                            this@BookingActivity,
                            "Booking berhasil diajukan! Status: pending",
                            Toast.LENGTH_LONG
                        ).show()

                        // Kembali ke Dashboard
                        val intent = Intent(this@BookingActivity, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@BookingActivity, bookingResponse.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@BookingActivity, "Gagal membuat booking", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Ajukan Booking"

                Toast.makeText(this@BookingActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}