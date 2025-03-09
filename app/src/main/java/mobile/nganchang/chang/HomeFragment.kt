package mobile.nganchang.chang.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobile.nganchang.chang.R

class HomeFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var spinnerWorkType: Spinner
    private lateinit var techniciansContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        spinnerWorkType = view.findViewById(R.id.spinner_work_type)
        techniciansContainer = view.findViewById(R.id.technicians_container)

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

    // ✅ โหลดประเภทงานจาก Firestore
    private fun loadWorkTypes() {
        db.collection("work_types").get()
            .addOnSuccessListener { result ->
                val workTypes = mutableListOf<String>()
                for (doc in result) {
                    workTypes.add(doc.getString("name") ?: "")
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, workTypes)
                spinnerWorkType.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "โหลดประเภทงานล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

    // ✅ โหลดช่างที่มีทักษะตรงกับประเภทงานที่เลือก
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
                    val price = doc.getString("price")?.toLong() ?: 0L // ใช้การแปลงค่า

                    // สร้าง CardView สำหรับแต่ละช่าง
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

                    // สร้าง Layout สำหรับปุ่ม
                    val layout = LinearLayout(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.VERTICAL
                        setPadding(24, 16, 24, 16)
                    }

                    // เพิ่มชื่อช่าง
                    val nameTextView = TextView(requireContext()).apply {
                        text = name
                        textSize = 18f
                        setTextColor(resources.getColor(android.R.color.black, null))
                    }

                    // เพิ่มราคาของช่าง
                    val priceTextView = TextView(requireContext()).apply {
                        text = "ราคา: ฿$price"
                        textSize = 16f
                        setTextColor(resources.getColor(android.R.color.black, null))
                        setPadding(0, 8, 0, 0)  // ตั้งค่า padding: (left, top, right, bottom)
                    }


                    // ปุ่มเลือกช่าง (ใช้ MaterialButton)
                    val selectButton = MaterialButton(requireContext()).apply {
                        text = "เลือกช่าง"
                        setPadding(16, 8, 16, 8)
                        setBackgroundColor(resources.getColor(R.color.blue_500, null)) // ตั้งค่าสีปุ่ม
                        setTextColor(resources.getColor(android.R.color.white, null)) // ตั้งค่าสีตัวอักษร
                        cornerRadius = 12 // เพิ่มความโค้งมนของปุ่ม
                        strokeColor = resources.getColorStateList(R.color.blue_500, null) // สีขอบปุ่ม
                        strokeWidth = 2
                        setOnClickListener {
                            selectTechnician(technicianId, name, workType, price)
                        }
                    }

                    // เพิ่ม View เข้า Layout
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
        val customerId = FirebaseAuth.getInstance().currentUser?.uid
        if (customerId == null) {
            Toast.makeText(requireContext(), "เกิดข้อผิดพลาด: ไม่พบผู้ใช้", Toast.LENGTH_SHORT).show()
            return
        }

        val booking = hashMapOf(
            "customer_id" to customerId,
            "technician_id" to technicianId,
            "technician_name" to name,
            "work_type" to workType,  // ✅ บันทึกประเภทงานของช่าง
            "price" to price, // ✅ บันทึกราคา
            "status" to "pending"
        )

        db.collection("bookings").add(booking)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "เลือกช่างสำเร็จ!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "บันทึกการจองล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

}
