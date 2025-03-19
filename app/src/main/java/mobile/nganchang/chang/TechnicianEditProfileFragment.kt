package mobile.nganchang.chang.technician

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobile.nganchang.chang.R
import java.text.SimpleDateFormat
import java.util.*

class TechnicianEditProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var etName: EditText
    private lateinit var etPrice: EditText
    private lateinit var etAvailableDate: EditText
    private lateinit var etAvailableTime: EditText
    private lateinit var spinnerWorkType: Spinner
    private lateinit var switchAvailable: Switch
    private lateinit var btnSave: Button
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_technician_edit_profile, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        etName = view.findViewById(R.id.et_name)
        etPrice = view.findViewById(R.id.et_price)
        etAvailableDate = view.findViewById(R.id.et_available_date)
        etAvailableTime = view.findViewById(R.id.et_available_time)
        spinnerWorkType = view.findViewById(R.id.spinner_work_type)
        switchAvailable = view.findViewById(R.id.switch_available)
        btnSave = view.findViewById(R.id.btn_save)

        loadWorkTypes()
        loadProfile()

        etAvailableDate.setOnClickListener {
            showDatePicker()
        }

        etAvailableTime.setOnClickListener {
            showTimePicker()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        return view
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                etAvailableDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker() {
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                etAvailableTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
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

                // ใช้ ArrayAdapter ในการตั้งค่าให้กับ Spinner
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
                etPrice.setText(doc.getString("price"))
                etAvailableDate.setText(doc.getString("available_date"))
                etAvailableTime.setText(doc.getString("available_time"))
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
        val price = etPrice.text.toString().trim()
        val workType = spinnerWorkType.selectedItem.toString()
        val available = switchAvailable.isChecked
        val availableDate = etAvailableDate.text.toString().trim()
        val availableTime = etAvailableTime.text.toString().trim()

        val data = mapOf(
            "name" to name,
            "price" to price,
            "work_type" to workType,
            "available" to available,
            "available_date" to availableDate,
            "available_time" to availableTime
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
