package mobile.nganchang.chang

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobile.nganchang.chang.technician.TechnicianMainActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etName = findViewById<EditText>(R.id.et_name)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoToLogin = findViewById<TextView>(R.id.tv_go_to_login)
        val spinnerRole = findViewById<Spinner>(R.id.spinner_role)

        val roles = arrayOf("ลูกค้า", "ช่าง")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRole.adapter = adapter

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val name = etName.text.toString().trim()
            val role = if (spinnerRole.selectedItem == "ลูกค้า") "customer" else "technician"

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val user = hashMapOf(
                                "uid" to userId,
                                "name" to name,
                                "email" to email,
                                "role" to role
                            )

                            db.collection("users").document(userId).set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "สมัครสมาชิกสำเร็จ!", Toast.LENGTH_SHORT).show()

                                    if (role == "technician") {
                                        // ✅ ถ้าเป็นช่าง ให้ไปหน้าแก้ไขโปรไฟล์
                                        startActivity(Intent(this, TechnicianMainActivity::class.java))
                                    } else {
                                        startActivity(Intent(this, LoginActivity::class.java))
                                    }
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "เกิดข้อผิดพลาด: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
