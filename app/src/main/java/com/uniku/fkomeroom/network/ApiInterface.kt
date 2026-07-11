package com.uniku.fkomeroom.network

import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @POST("login")
    fun login(@Body data: HashMap<String, String>): Call<AuthResponse>

    @POST("logout")
    fun logout(@Header("Authorization") token: String): Call<LogoutResponse>

    @GET("rooms")
    fun getRooms(@Header("Authorization") token: String): Call<RoomsResponse>

    @GET("rooms/{id}")
    fun getRoomDetail(@Header("Authorization") token: String, @Path("id") id: Int): Call<RoomDetailResponse>

    @POST("bookings")
    fun createBooking(@Header("Authorization") token: String, @Body data: HashMap<String, Any>): Call<BookingResponse>

    @GET("my-bookings")
    fun getMyBookings(@Header("Authorization") token: String): Call<MyBookingsResponse>

    @GET("all-bookings")
    fun getAllBookings(@Header("Authorization") token: String): Call<MyBookingsResponse>

    @PUT("bookings/{id}/approve")
    fun approveBooking(@Header("Authorization") token: String, @Path("id") id: Int): Call<BookingResponse>

    @PUT("bookings/{id}/reject")
    fun rejectBooking(@Header("Authorization") token: String, @Path("id") id: Int, @Body data: HashMap<String, String>): Call<BookingResponse>

    @GET("notifications")
    fun getNotifications(@Header("Authorization") token: String): Call<NotificationsResponse>
}

// DATA CLASSES

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData?
)

data class AuthData(
    val user: User,
    val token: String,
    val token_type: String
)

data class User(
    val id: Int,
    val nama_lengkap: String,
    val email: String,
    val role: String,
    val nim: String?,
    val nip: String?,
    val no_hp: String?,
    val is_active: Boolean
)

data class LogoutResponse(
    val success: Boolean,
    val message: String
)

data class RoomsResponse(
    val success: Boolean,
    val message: String,
    val data: List<Room>
)

data class Room(
    val id: Int,
    val nama_ruangan: String,
    val kode_ruangan: String,
    val jenis: String,
    val lantai: Int,
    val kapasitas: Int,
    val deskripsi: String?,
    val foto: String?,
    val status: String,
    val facilities: List<Facility>?
)

data class Facility(
    val id: Int,
    val room_id: Int,
    val nama_fasilitas: String
)

data class RoomDetailResponse(
    val success: Boolean,
    val message: String,
    val data: Room
)

data class BookingResponse(
    val success: Boolean,
    val message: String,
    val data: BookingData?
)

data class BookingData(
    val id: Int,
    val kode_booking: String,
    val user_id: Int,
    val room_id: Int,
    val mk_id: Int?,
    val kategori: String,
    val tanggal: String,
    val jam_mulai: String,
    val jam_selesai: String,
    val tujuan: String,
    val jumlah_peserta: Int,
    val status: String
)

data class MyBookingsResponse(
    val success: Boolean,
    val message: String,
    val data: List<MyBooking>
)

data class MyBooking(
    val id: Int,
    val kode_booking: String,
    val room_id: Int,
    val kategori: String,
    val tanggal: String,
    val jam_mulai: String,
    val jam_selesai: String,
    val tujuan: String,
    val jumlah_peserta: Int,
    val status: String,
    val room: RoomSimple?
)

data class RoomSimple(
    val id: Int,
    val nama_ruangan: String,
    val kode_ruangan: String
)

data class NotificationsResponse(
    val success: Boolean,
    val message: String,
    val data: List<NotificationItem>,
    val unread_count: Int
)

data class NotificationItem(
    val id: Int,
    val user_id: Int,
    val judul: String,
    val pesan: String,
    val booking_id: Int?,
    val is_read: Boolean,
    val created_at: String
)