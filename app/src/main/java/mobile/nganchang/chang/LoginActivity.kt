package mobile.nganchang.chang

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobile.nganchang.chang.customer.CustomerMainActivity
import mobile.nganchang.chang.technician.TechnicianMainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvGoToRegister = findViewById<TextView>(R.id.tv_go_to_register)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        if (userId != null) {
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val role = document.getString("role")

                                        when (role) {
                                            "customer" -> {
                                                Toast.makeText(this, "เข้าสู่ระบบสำเร็จ! (ลูกค้า)", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this, CustomerMainActivity::class.java))
                                            }
                                            "technician" -> {
                                                Toast.makeText(this, "เข้าสู่ระบบสำเร็จ! (ช่าง)", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this, TechnicianMainActivity::class.java))
                                            }
                                            else -> {
                                                Toast.makeText(this, "เกิดข้อผิดพลาด: ไม่สามารถระบุบทบาทได้", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        finish()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "เข้าสู่ระบบล้มเหลว: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
