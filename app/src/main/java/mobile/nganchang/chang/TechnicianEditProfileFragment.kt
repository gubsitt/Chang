package mobile.nganchang.chang.technician

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobile.nganchang.chang.R

class TechnicianEditProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var etName: EditText
    private lateinit var etPrice: EditText  // เพิ่มตัวแปรสำหรับราคา
    private lateinit var spinnerWorkType: Spinner
    private lateinit var switchAvailable: Switch
    private lateinit var btnSave: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_technician_edit_profile, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        etName = view.findViewById(R.id.et_name)
        etPrice = view.findViewById(R.id.et_price)  // เชื่อมโยงช่องกรอกราคา
        spinnerWorkType = view.findViewById(R.id.spinner_work_type)
        switchAvailable = view.findViewById(R.id.switch_available)
        btnSave = view.findViewById(R.id.btn_save)

        loadWorkTypes()
        loadProfile()

        btnSave.setOnClickListener {
            saveProfile()
        }

        return view
    }

    private fun loadWorkTypes() {
        db.collection("work_types").get()
            .addOnSuccessListener { result ->
                val workTypes = mutableListOf<String>()
                for (doc in result) {
                    val name = doc.getString("name")
                    if (!name.isNullOrEmpty()) {
                        workTypes.add(name)
                    }
                }

                if (workTypes.isEmpty()) {
                    Toast.makeText(requireContext(), "ไม่มีประเภทงานในระบบ", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, workTypes)
                spinnerWorkType.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "โหลดประเภทงานล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadProfile() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                etName.setText(doc.getString("name"))
                etPrice.setText(doc.getString("price"))  // โหลดราคาจาก Firebase
                switchAvailable.isChecked = doc.getBoolean("available") ?: false
                val workType = doc.getString("work_type") ?: ""
                val position = (spinnerWorkType.adapter as ArrayAdapter<String>).getPosition(workType)
                spinnerWorkType.setSelection(position)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        val userId = auth.currentUser?.uid ?: return
        val name = etName.text.toString().trim()
        val price = etPrice.text.toString().trim()  // ดึงราคาจาก EditText
        val workType = spinnerWorkType.selectedItem.toString()
        val available = switchAvailable.isChecked

        val data = mapOf(
            "name" to name,
            "price" to price,  // เก็บข้อมูลราคา
            "work_type" to workType,
            "available" to available
        )

        db.collection("users").document(userId).update(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "บันทึกสำเร็จ!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "บันทึกไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
    }
}
