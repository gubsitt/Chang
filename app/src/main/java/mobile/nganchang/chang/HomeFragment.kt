package mobile.nganchang.chang.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobile.nganchang.chang.LoginActivity
import mobile.nganchang.chang.R

class HomeFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var spinnerWorkType: Spinner
    private lateinit var techniciansContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        spinnerWorkType = view.findViewById(R.id.spinner_work_type)
        techniciansContainer = view.findViewById(R.id.technicians_container)

        val btnLogout = view.findViewById<MaterialButton>(R.id.btn_logout)
        btnLogout.setOnClickListener { logoutUser() }

        loadWorkTypes()

        spinnerWorkType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedWorkType = parent?.getItemAtPosition(position).toString()
                loadTechnicians(selectedWorkType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return view
    }

    private fun logoutUser() {
        val sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        auth.signOut()
        Log.d("Logout", "User signed out: ${auth.currentUser}") // ตรวจสอบค่า auth

        Toast.makeText(requireContext(), "ออกจากระบบสำเร็จ", Toast.LENGTH_SHORT).show()

        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }


    private fun loadWorkTypes() {
        db.collection("work_types").get()
            .addOnSuccessListener { result ->
                val workTypes = result.mapNotNull { it.getString("name") }
                if (workTypes.isNotEmpty()) {
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, workTypes)
                    spinnerWorkType.adapter = adapter
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "โหลดประเภทงานล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTechnicians(workType: String) {
        techniciansContainer.removeAllViews()

        db.collection("users")
            .whereEqualTo("role", "technician")
            .whereEqualTo("work_type", workType)
            .whereEqualTo("available", true)
            .get()
            .addOnSuccessListener { result ->
                techniciansContainer.removeAllViews()
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "ไม่พบช่างในประเภทนี้", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                for (doc in result) {
                    val name = doc.getString("name") ?: "ไม่ระบุ"
                    val technicianId = doc.id
                    val price = doc.get("price")?.toString()?.toLongOrNull() ?: 0L // ✅ ป้องกัน error `price` ไม่ใช่ `Long`

                    val cardView = CardView(requireContext()).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(16, 8, 16, 8)
                        }
                        radius = 16f
                        cardElevation = 4f
                    }

                    val layout = LinearLayout(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.VERTICAL
                        setPadding(24, 16, 24, 16)
                    }

                    val nameTextView = TextView(requireContext()).apply {
                        text = name
                        textSize = 18f
                        setTextColor(resources.getColor(android.R.color.black, null))
                    }

                    val priceTextView = TextView(requireContext()).apply {
                        text = "ราคา: ฿$price"
                        textSize = 16f
                        setTextColor(resources.getColor(android.R.color.black, null))
                        setPadding(0, 8, 0, 0)
                    }

                    val selectButton = MaterialButton(requireContext()).apply {
                        text = "เลือกช่าง"
                        setPadding(16, 8, 16, 8)
                        setBackgroundColor(resources.getColor(R.color.blue_500, null))
                        setTextColor(resources.getColor(android.R.color.white, null))
                        cornerRadius = 12
                        strokeColor = resources.getColorStateList(R.color.blue_500, null)
                        strokeWidth = 2
                        setOnClickListener {
                            selectTechnician(technicianId, name, workType, price)
                        }
                    }

                    layout.addView(nameTextView)
                    layout.addView(priceTextView)
                    layout.addView(selectButton)
                    cardView.addView(layout)

                    techniciansContainer.addView(cardView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "โหลดข้อมูลช่างล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectTechnician(technicianId: String, name: String, workType: String, price: Long) {
        val customerId = auth.currentUser?.uid
        if (customerId == null) {
            Toast.makeText(requireContext(), "เกิดข้อผิดพลาด: ไม่พบผู้ใช้", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(customerId).get().addOnSuccessListener { customerDoc ->
            if (!customerDoc.exists()) {
                Toast.makeText(requireContext(), "ไม่พบข้อมูลลูกค้า", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val customerName = customerDoc.getString("name") ?: "ไม่ระบุชื่อ"

            db.collection("users").document(technicianId).get().addOnSuccessListener { technicianDoc ->
                if (!technicianDoc.exists()) {
                    Toast.makeText(requireContext(), "ไม่พบข้อมูลช่าง", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val technicianName = technicianDoc.getString("name") ?: "ไม่ระบุชื่อ"

                val booking = hashMapOf(
                    "customer_id" to customerId,
                    "customer_name" to customerName,
                    "technician_id" to technicianId,
                    "technician_name" to technicianName,
                    "work_type" to workType,
                    "price" to price,
                    "status" to "pending"
                )

                db.collection("bookings").add(booking)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "เลือกช่างสำเร็จ!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "บันทึกการจองล้มเหลว", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "ดึงข้อมูลช่างล้มเหลว", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "ดึงข้อมูลลูกค้าล้มเหลว", Toast.LENGTH_SHORT).show()
        }
    }
}
