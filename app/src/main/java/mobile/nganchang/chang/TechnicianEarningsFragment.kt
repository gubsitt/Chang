package mobile.nganchang.chang.technician

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobile.nganchang.chang.R

class TechnicianEarningsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tvTotalEarnings: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_technician_earnings, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        tvTotalEarnings = view.findViewById(R.id.tv_total_earnings)

        loadEarnings()

        return view
    }

    private fun loadEarnings() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("bookings")
                .whereEqualTo("technician_id", userId)  // 🔹 ต้องตรงกับฟิลด์ใน Firestore (เดิมเป็น "technicianId")
                .whereEqualTo("status", "paid")         // กรองเฉพาะที่จ่ายเงินแล้ว
                .get()
                .addOnSuccessListener { documents ->
                    var totalEarnings = 0.0
                    for (document in documents) {
                        val price = document.getDouble("price") ?: 0.0
                        totalEarnings += price
                    }
                    tvTotalEarnings.text = "฿${totalEarnings}"
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "โหลดข้อมูลรายได้ล้มเหลว: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


}
