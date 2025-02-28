package mobile.nganchang.chang.technician

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobile.nganchang.chang.R

class TechnicianHomeFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tvTechnicianName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var switchAvailable: SwitchMaterial  // ✅ ใช้ SwitchMaterial แทน Switch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_technician_home, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvTechnicianName = view.findViewById(R.id.tv_technician_name)
        tvStatus = view.findViewById(R.id.tv_status)
        switchAvailable = view.findViewById(R.id.switch_available)  // ✅ ใช้ SwitchMaterial

        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "ไม่ระบุชื่อ"
                        val available = document.getBoolean("available") ?: false

                        tvTechnicianName.text = name
                        switchAvailable.isChecked = available
                        updateStatusText(available)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        switchAvailable.setOnCheckedChangeListener { _, isChecked ->
            updateStatusText(isChecked)

            userId?.let {
                db.collection("users").document(it)
                    .update("available", isChecked)
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "ไม่สามารถอัปเดตสถานะได้: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return view
    }

    private fun updateStatusText(isAvailable: Boolean) {
        tvStatus.text = if (isAvailable) "สถานะ: ว่าง" else "สถานะ: ไม่ว่าง"
        tvStatus.setTextColor(resources.getColor(if (isAvailable) R.color.green_700 else R.color.red_700, null))
    }
}
