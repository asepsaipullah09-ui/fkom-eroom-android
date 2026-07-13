package com.uniku.fkomeroom

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RoomAdapter(private val roomList: List<Room>) :
    RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
        val tvRoomCode: TextView = itemView.findViewById(R.id.tvRoomCode)
        val tvRoomDetails: TextView = itemView.findViewById(R.id.tvRoomDetails)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]

        holder.tvRoomName.text = room.nama_ruangan
        holder.tvRoomCode.text = "Kode: ${room.kode_ruangan}"
        holder.tvRoomDetails.text = "${room.jenis} • Lantai ${room.lantai} • ${room.kapasitas} Orang"
        holder.tvStatus.text = room.status

        // Ubah warna badge status
        when(room.status) {
            "tersedia" -> holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
            "maintenance" -> holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"))
            "nonaktif" -> holder.tvStatus.setBackgroundColor(Color.parseColor("#F44336"))
        }

        // Tambah klik listener
        holder.itemView.setOnClickListener {
            val context: Context = holder.itemView.context
            val intent = Intent(context, RoomDetailActivity::class.java)
            intent.putExtra("room_id", room.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = roomList.size
}