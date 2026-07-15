package com.uniku.fkomeroom

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uniku.fkomeroom.network.ApiClient
import com.uniku.fkomeroom.network.ApiInterface
import com.uniku.fkomeroom.network.RoomsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageRoomsActivity : AppCompatActivity() {

    private lateinit var rvManageRooms: RecyclerView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var apiInterface: ApiInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_rooms)

        rvManageRooms = findViewById(R.id.rvManageRooms)
        rvManageRooms.layoutManager = LinearLayoutManager(this)

        sharedPref = getSharedPreferences("FKOM_eRoom", MODE_PRIVATE)
        apiInterface = ApiClient.createService(ApiInterface::class.java)

        fetchRooms()
    }

    private fun fetchRooms() {
        val token = sharedPref.getString("token", null) ?: return

        apiInterface.getRooms("Bearer $token").enqueue(object : Callback<RoomsResponse> {
            override fun onResponse(call: Call<RoomsResponse>, response: Response<RoomsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    // Kita pakai adapter yang sama, tapi nanti bisa dikustomisasi
                    // Untuk sementara tampilkan list ruangan saja
                    val adapter = RoomAdapter(response.body()!!.data)
                    rvManageRooms.adapter = adapter
                }
            }
            override fun onFailure(call: Call<RoomsResponse>, t: Throwable) {
                Toast.makeText(this@ManageRoomsActivity, "Gagal load ruangan", Toast.LENGTH_SHORT).show()
            }
        })
    }
}