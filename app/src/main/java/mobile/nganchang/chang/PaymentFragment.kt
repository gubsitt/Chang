package mobile.nganchang.chang.customer

import PaymentAdapter
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



class PaymentFragment : Fragment(), PaymentAdapter.PaymentCallback {

    private lateinit var db: FirebaseFirestore
    private lateinit var bookingList: ListView
    private var price: Long = 0L
    private lateinit var technicianId: String
    private lateinit var bookingId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        db = FirebaseFirestore.getInstance()
        bookingList = view.findViewById(R.id.booking_list)

        // ใช้ FirebaseAuth เพื่อตรวจสอบว่า user คนนี้ได้ทำการจองหรือไม่
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            bookingId = currentUser.uid
        }

        // โหลดข้อมูลการจ่ายเงิน
        loadPaymentDetails()

        return view
    }

    private fun loadPaymentDetails() {
        // ดึงข้อมูลจาก Firestore โดยตรง
        db.collection("bookings")
            .whereEqualTo("customer_id", FirebaseAuth.getInstance().currentUser?.uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "ไม่พบข้อมูลการจอง", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val bookingDataList = mutableListOf<Map<String, String>>()
                for (document in result) {
                    val technicianName = document.getString("technician_name") ?: "ไม่ระบุ"
                    val workType = document.getString("work_type") ?: "ไม่ระบุ"
                    val status = document.getString("status") ?: "pending"
                    price = document.getLong("price") ?: 0L
                    technicianId = document.getString("technician_id") ?: ""
                    val bookingDetails = mapOf(
                        "technician_name" to technicianName,
                        "work_type" to workType,
                        "status" to status,
                        "price" to "฿$price",
                        "booking_id" to document.id
                    )
                    bookingDataList.add(bookingDetails)
                }

                // ใช้ PaymentAdapter เพื่อนำข้อมูลมาแสดงใน ListView
                val adapter = PaymentAdapter(requireContext(), bookingDataList, this)
                bookingList.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "ไม่สามารถโหลดข้อมูลได้", Toast.LENGTH_SHORT).show()
            }
    }

    override fun initiatePayment(selectedBookingId: String) {
        // อัปเดตสถานะการจองเป็น 'paid'
        db.collection("bookings").document(selectedBookingId)
            .update("status", "paid")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "ชำระเงินสำเร็จ", Toast.LENGTH_SHORT).show()
                updateTechnicianBalance(selectedBookingId)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "การชำระเงินล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTechnicianBalance(selectedBookingId: String) {
        // ดึงข้อมูล technicianId จากการจอง
        db.collection("bookings").document(selectedBookingId)
            .get()
            .addOnSuccessListener { document ->
                technicianId = document.getString("technician_id") ?: ""
                db.collection("users").document(technicianId)
                    .update("balance", price)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "ยอดเงินของช่างถูกอัปเดต", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "ไม่สามารถอัปเดตยอดเงินของช่างได้", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "ไม่สามารถดึงข้อมูลการจองได้", Toast.LENGTH_SHORT).show()
            }
    }
}
