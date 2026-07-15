package com.uniku.fkomeroom

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.uniku.fkomeroom.network.MyBooking

class BookingAdapter(
    private val bookingList: List<MyBooking>,
    private val userRole: String,
    private val onApprove: (Int) -> Unit,
    private val onReject: (Int) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvKodeBooking: TextView = itemView.findViewById(R.id.tvKodeBooking)
        val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
        val tvDetail: TextView = itemView.findViewById(R.id.tvDetail)
        val tvTujuan: TextView = itemView.findViewById(R.id.tvTujuan)
        val tvPeserta: TextView = itemView.findViewById(R.id.tvPeserta)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val layoutAction: LinearLayout = itemView.findViewById(R.id.layoutAction)
        val btnApprove: MaterialButton = itemView.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]

        holder.tvKodeBooking.text = booking.kode_booking
        holder.tvRoomName.text = booking.room?.nama_ruangan ?: "Ruangan"
        holder.tvDetail.text = "${booking.tanggal} • ${booking.jam_mulai} - ${booking.jam_selesai}"
        holder.tvTujuan.text = booking.tujuan
        holder.tvPeserta.text = "${booking.jumlah_peserta} Peserta • ${booking.kategori}"
        holder.tvStatus.text = booking.status

        // Warna badge
        when(booking.status) {
            "approved" -> holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
            "pending_dosen", "pending_admin" -> holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"))
            "rejected" -> holder.tvStatus.setBackgroundColor(Color.parseColor("#F44336"))
            else -> holder.tvStatus.setBackgroundColor(Color.parseColor("#9E9E9E"))
        }

        // LOGIKA TOMBOL BERURUTAN
        holder.layoutAction.visibility = View.GONE // Default sembunyi

        if (userRole == "dosen" && booking.status == "pending_dosen") {
            // Dosen hanya lihat tombol jika status pending_dosen
            holder.layoutAction.visibility = View.VISIBLE
            holder.btnApprove.text = "Teruskan ke Admin"
            holder.btnApprove.setOnClickListener { onApprove(booking.id) }
            holder.btnReject.setOnClickListener { onReject(booking.id) }
        }
        else if (userRole == "admin" && booking.status == "pending_admin") {
            // Admin hanya lihat tombol jika status pending_admin (Dosen sudah approve)
            holder.layoutAction.visibility = View.VISIBLE
            holder.btnApprove.text = "Setujui Final"
            holder.btnApprove.setOnClickListener { onApprove(booking.id) }
            holder.btnReject.setOnClickListener { onReject(booking.id) }
        }
    }

    private fun showRejectDialog(context: Context, bookingId: Int, onReject: (Int) -> Unit) {
        val editText = EditText(context)
        editText.hint = "Masukkan alasan penolakan"
        editText.setPadding(20, 20, 20, 20)

        AlertDialog.Builder(context)
            .setTitle("Tolak Booking")
            .setMessage("Masukkan alasan penolakan:")
            .setView(editText)
            .setPositiveButton("Tolak") { dialog, _ ->
                val alasan = editText.text.toString()
                if (alasan.isNotEmpty()) {
                    onReject(bookingId)
                } else {
                    Toast.makeText(context, "Alasan harus diisi!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun getItemCount() = bookingList.size
}