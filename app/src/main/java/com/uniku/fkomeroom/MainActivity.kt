package com.uniku.fkomeroom

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.uniku.fkomeroom.network.ApiClient
import com.uniku.fkomeroom.network.ApiInterface
import com.uniku.fkomeroom.network.AuthResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var apiInterface: ApiInterface
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        sharedPref = getSharedPreferences("FKOM_eRoom", MODE_PRIVATE)
        apiInterface = ApiClient.createService(ApiInterface::class.java)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        btnLogin.isEnabled = false
        btnLogin.text = "Loading..."

        val data = HashMap<String, String>()
        data["email"] = email
        data["password"] = password

        apiInterface.login(data).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                btnLogin.isEnabled = true
                btnLogin.text = "Masuk"

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    if (authResponse.success) {
                        val editor = sharedPref.edit()
                        editor.putString("token", authResponse.data?.token)
                        editor.putString("nama", authResponse.data?.user?.nama_lengkap)
                        editor.putString("role", authResponse.data?.user?.role)
                        editor.apply()

                        Toast.makeText(
                            this@MainActivity,
                            "Login berhasil! Selamat datang ${authResponse.data?.user?.nama_lengkap}",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this@MainActivity, authResponse.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Login gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                btnLogin.isEnabled = true
                btnLogin.text = "Masuk"

                Toast.makeText(
                    this@MainActivity,
                    "Gagal terhubung ke server: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}