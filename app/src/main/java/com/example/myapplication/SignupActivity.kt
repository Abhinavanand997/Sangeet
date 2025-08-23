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
import com.example.myapplication.LoginActivity
import com.example.myapplication.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import org.intellij.lang.annotations.Pattern

class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivitySignupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.createAccountBtn.setOnClickListener {
            val email=binding.emailEdittext.text.toString()
            val password=binding.passwordEdittext.text.toString()
            val confirmPassword=binding.confirmPasswordEdittext.text.toString()
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.emailEdittext.setError("Invalid Email")
                return@setOnClickListener
            }
            if(password.length<8){
                binding.passwordEdittext.setError("Password should contain atleast 8 characters")
                return@setOnClickListener
            }
            if(!password.equals(confirmPassword)){
                binding.confirmPasswordEdittext.setError("Password not matched")
                return@setOnClickListener
            }
            createAccountWithFirebase(email,password)

        }
        binding.gotoLoginBtn.setOnClickListener {
           finish()
        }
    }
    fun createAccountWithFirebase(email: String,password:String){
        setInProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnSuccessListener {
            setInProgress(false)
            Toast.makeText(applicationContext,"User created Succesfully", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            setInProgress(false)
            Toast.makeText(applicationContext,"Create Account Failed", Toast.LENGTH_SHORT).show()
        }
    }
    fun setInProgress(inProgress: Boolean){
        if(inProgress){
            binding.createAccountBtn.visibility= View.GONE
            binding.progressBar.visibility=View.VISIBLE
        }
        else{
            binding.createAccountBtn.visibility= View.VISIBLE
            binding.progressBar.visibility=View.GONE
        }
    }
}