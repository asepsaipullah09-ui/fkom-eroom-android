package com.uniku.fkomeroom

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uniku.fkomeroom.network.ApiClient
import com.uniku.fkomeroom.network.ApiInterface
import com.uniku.fkomeroom.network.BookingResponse
import com.uniku.fkomeroom.network.MyBookingsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var rvBookings: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var sharedPref: SharedPreferences
    private lateinit var apiInterface: ApiInterface
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        // Inisialisasi views
        rvBookings = findViewById(R.id.rvBookings)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnBack = findViewById(R.id.btnBack)

        // Inisialisasi
        sharedPref = getSharedPreferences("FKOM_eRoom", MODE_PRIVATE)
        apiInterface = ApiClient.createService(ApiInterface::class.java)
        userRole = sharedPref.getString("role", "") ?: ""

        // Setup RecyclerView
        rvBookings.layoutManager = LinearLayoutManager(this)

        // Back button
        btnBack.setOnClickListener { finish() }

        // Fetch bookings
        fetchBookings()
    }

    private fun fetchBookings() {
        val token = sharedPref.getString("token", null)
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        // Jika admin/dosen, ambil SEMUA booking. Jika tidak, hanya booking sendiri.
        val call = if (userRole == "admin" || userRole == "dosen") {
            apiInterface.getAllBookings("Bearer $token")
        } else {
            apiInterface.getMyBookings("Bearer $token")
        }

        call.enqueue(object : Callback<MyBookingsResponse> {
            override fun onResponse(call: Call<MyBookingsResponse>, response: Response<MyBookingsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bookings = response.body()!!.data

                    if (bookings.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        rvBookings.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvBookings.visibility = View.VISIBLE

                        val adapter = BookingAdapter(
                            bookings,
                            userRole,
                            onApprove = { bookingId -> approveBooking(bookingId, token) },
                            onReject = { bookingId -> showRejectDialog(bookingId, token) }
                        )
                        rvBookings.adapter = adapter
                    }
                } else {
                    Toast.makeText(this@MyBookingsActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyBookingsResponse>, t: Throwable) {
                Toast.makeText(this@MyBookingsActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun approveBooking(bookingId: Int, token: String) {
        apiInterface.approveBooking("Bearer $token", bookingId).enqueue(object : Callback<BookingResponse> {
            override fun onResponse(call: Call<BookingResponse>, response: Response<BookingResponse>) {
                if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                    Toast.makeText(this@MyBookingsActivity, "Booking disetujui!", Toast.LENGTH_SHORT).show()
                    fetchBookings() // Refresh data
                } else {
                    Toast.makeText(this@MyBookingsActivity, "Gagal menyetujui booking", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                Toast.makeText(this@MyBookingsActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showRejectDialog(bookingId: Int, token: String) {
        val editText = android.widget.EditText(this)
        editText.hint = "Masukkan alasan penolakan"
        editText.setPadding(20, 20, 20, 20)

        AlertDialog.Builder(this)
            .setTitle("Tolak Booking")
            .setMessage("Masukkan alasan penolakan:")
            .setView(editText)
            .setPositiveButton("Tolak") { dialog, _ ->
                val alasan = editText.text.toString()
                if (alasan.isNotEmpty()) {
                    rejectBooking(bookingId, token, alasan)
                } else {
                    Toast.makeText(this, "Alasan harus diisi!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun rejectBooking(bookingId: Int, token: String, alasan: String) {
        val data = HashMap<String, String>()
        data["alasan"] = alasan

        apiInterface.rejectBooking("Bearer $token", bookingId, data).enqueue(object : Callback<BookingResponse> {
            override fun onResponse(call: Call<BookingResponse>, response: Response<BookingResponse>) {
                if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                    Toast.makeText(this@MyBookingsActivity, "Booking ditolak!", Toast.LENGTH_SHORT).show()
                    fetchBookings() // Refresh data
                } else {
                    Toast.makeText(this@MyBookingsActivity, "Gagal menolak booking", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                Toast.makeText(this@MyBookingsActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}