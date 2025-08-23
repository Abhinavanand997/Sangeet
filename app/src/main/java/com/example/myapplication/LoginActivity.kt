package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth



class LoginActivity : AppCompatActivity() {
    lateinit var binding :ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.loginAccountBtn.setOnClickListener {
            val email=binding.emailEdittext.text.toString()
            val password=binding.passwordEdittext.text.toString()

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.emailEdittext.setError("Invalid Email")
                return@setOnClickListener
            }
            if(password.length<8){
                binding.passwordEdittext.setError("Password should contain atleast 8 characters")
                return@setOnClickListener
            }

            loginAccountWithFirebase(email,password)


        }
        binding.gotoSignupBtn.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }

    }
    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().currentUser?.apply {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

    }
    fun loginAccountWithFirebase(email: String,password:String){
        setInProgress(true)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnSuccessListener {
            setInProgress(false)
           startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }.addOnFailureListener {
            setInProgress(false)
            Toast.makeText(applicationContext,"Login Failed", Toast.LENGTH_SHORT).show()
        }
    }
    fun setInProgress(inProgress: Boolean){
        if(inProgress){
            binding.loginAccountBtn.visibility= View.GONE
            binding.progressBar.visibility=View.VISIBLE
        }
        else{
            binding.loginAccountBtn.visibility= View.VISIBLE
            binding.progressBar.visibility=View.GONE
        }
    }
}