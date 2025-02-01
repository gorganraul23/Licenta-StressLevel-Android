package book.kotlinforandroid.hr.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import book.kotlinforandroid.hr.ApiService
import book.kotlinforandroid.hr.R
import book.kotlinforandroid.hr.RetrofitInstance
import book.kotlinforandroid.hr.Utils
import book.kotlinforandroid.hr.databinding.ActivityLoginBinding
import book.kotlinforandroid.hr.model.LoginResponse
import book.kotlinforandroid.hr.model.UserCredentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : Activity() {

    private val APP_TAG = "LoginActivity"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginBtn: Button

    private val retrofit = RetrofitInstance.getRetrofitInstance()
    private val apiService = retrofit.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Utils.clearEmail()
        Utils.userId = 0

        // finding the button
        loginBtn = findViewById(R.id.butLogin)
        // finding the edit text email
        emailInput = findViewById(R.id.emailInput)
        // finding the edit text password
        passwordInput = findViewById(R.id.passwordInput)

        // Setting On Click Listener
        loginBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val pass = passwordInput.text.toString()

            apiService.login(UserCredentials(email, pass))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        // handle the response
                        if (response.message() == "Unauthorized")
                            Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        else {
                            println(response.body()!!.user_id)
                            Utils.userId = response.body()!!.user_id
                            Utils.setEmail(email)
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

}