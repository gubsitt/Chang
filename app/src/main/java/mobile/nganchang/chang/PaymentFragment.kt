package mobile.nganchang.chang.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobile.nganchang.chang.R
import com.google.android.material.button.MaterialButton

class PaymentFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var paymentListContainer: LinearLayout
    private lateinit var tvPaymentInfo: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        db = FirebaseFirestore.getInstance()
        paymentListContainer = view.findViewById(R.id.payment_list_container)
        tvPaymentInfo = view.findViewById(R.id.tv_payment_info)

        // โหลดข้อมูลการชำระเงิน
        loadPaymentDetails()

        return view
    }

    private fun loadPaymentDetails() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        db.collection("bookings")
            .whereEqualTo("customer_id", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                paymentListContainer.removeAllViews()

                if (result.isEmpty) {
                    tvPaymentInfo.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                tvPaymentInfo.visibility = View.GONE

                for (document in result) {
                    val technicianName = document.getString("technician_name") ?: "ไม่ระบุ"
                    val workType = document.getString("work_type") ?: "ไม่ระบุ"
                    val status = document.getString("status") ?: "pending"
                    val price = document.getLong("price") ?: 0L
                    val bookingId = document.id

                    // Inflate payment_item.xml
                    val paymentView = layoutInflater.inflate(R.layout.payment_item, paymentListContainer, false)

                    // ตั้งค่าข้อมูลใน UI
                    paymentView.findViewById<TextView>(R.id.technician_name).text = "ช่าง: $technicianName"
                    paymentView.findViewById<TextView>(R.id.work_type).text = "ประเภท: $workType"
                    paymentView.findViewById<TextView>(R.id.price).text = "ราคา: ฿$price"
                    paymentView.findViewById<TextView>(R.id.status).text = getStatusText(status)

                    val payButton = paymentView.findViewById<MaterialButton>(R.id.pay_button)
                    if (status == "pending") {
                        payButton.visibility = View.VISIBLE
                        payButton.setOnClickListener { initiatePayment(bookingId) }
                    } else {
                        payButton.visibility = View.GONE
                    }

                    // เพิ่ม View ลงใน Container
                    paymentListContainer.addView(paymentView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "โหลดข้อมูลการชำระเงินล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initiatePayment(bookingId: String) {
        db.collection("bookings").document(bookingId)
            .update("status", "paid")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "ชำระเงินสำเร็จ", Toast.LENGTH_SHORT).show()
                loadPaymentDetails()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "การชำระเงินล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getStatusText(status: String): String {
        return when (status) {
            "pending" -> "⏳ รอการยืนยัน"
            "confirmed" -> "✅ ยืนยันแล้ว"
            "paid" -> "💰 ชำระเงินแล้ว"
            "completed" -> "🎉 เสร็จสิ้น"
            "canceled" -> "❌ ถูกยกเลิก"
            else -> status
        }
    }
}
