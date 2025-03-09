package mobile.nganchang.chang.technician

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import mobile.nganchang.chang.R

class TechnicianBookingsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var bookingsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_technician_bookings, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        bookingsContainer = view.findViewById(R.id.bookings_container)

        loadBookings()
        return view
    }

    private fun loadBookings() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("bookings")
                .whereEqualTo("technician_id", userId)
                .get()
                .addOnSuccessListener { documents ->
                    bookingsContainer.removeAllViews()

                    if (documents.isEmpty) {
                        val noBookingsTextView = TextView(requireContext()).apply {
                            text = "ไม่มีการจองของคุณ"
                            textSize = 18f
                            setPadding(16, 16, 16, 16)
                            setTextColor(resources.getColor(android.R.color.darker_gray, null))
                        }
                        bookingsContainer.addView(noBookingsTextView)
                        return@addOnSuccessListener
                    }

                    for (document in documents) {
                        val customerName = document.getString("customer_name") ?: "ไม่ระบุ"
                        val workType = document.getString("work_type") ?: "ไม่ระบุประเภท"
                        val status = document.getString("status") ?: "pending"
                        val bookingId = document.id

                        // Inflate `item_booking.xml`
                        val bookingView = layoutInflater.inflate(R.layout.item_booking, bookingsContainer, false)

                        // ตั้งค่าข้อมูล
                        bookingView.findViewById<TextView>(R.id.tv_customer_name).text = "ลูกค้า: $customerName"
                        bookingView.findViewById<TextView>(R.id.tv_service_type).text = "บริการ: $workType"
                        bookingView.findViewById<TextView>(R.id.tv_status).text = "สถานะ: ${getStatusText(status)}"

                        val btnCancel = bookingView.findViewById<MaterialButton>(R.id.btn_cancel)
                        val btnPaid = bookingView.findViewById<MaterialButton>(R.id.btn_paid)

                        // ปรับปุ่มให้แสดงตามสถานะ
                        when (status) {
                            "pending" -> {
                                btnCancel.visibility = View.VISIBLE
                                btnPaid.visibility = View.GONE
                                btnCancel.setOnClickListener {
                                    updateBookingStatus(bookingId, "canceled")
                                }
                            }
                            "confirmed" -> {
                                btnCancel.visibility = View.VISIBLE
                                btnPaid.visibility = View.VISIBLE
                                btnCancel.setOnClickListener {
                                    updateBookingStatus(bookingId, "canceled")
                                }
                                btnPaid.setOnClickListener {
                                    updateBookingStatus(bookingId, "completed")
                                }
                            }
                            "completed" -> {
                                btnCancel.visibility = View.GONE
                                btnPaid.visibility = View.GONE
                            }
                        }

                        // เพิ่ม View ลงใน container
                        bookingsContainer.addView(bookingView)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateBookingStatus(bookingId: String, action: String) {
        db.collection("bookings").document(bookingId)
            .update("status", action)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "อัปเดตสถานะเป็น $action สำเร็จ!", Toast.LENGTH_SHORT).show()
                loadBookings()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "อัปเดตสถานะล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getStatusText(status: String): String {
        return when (status) {
            "pending" -> "⏳ รอการยืนยัน"
            "confirmed" -> "✅ ยืนยันแล้ว"
            "completed" -> "🎉 เสร็จสิ้น"
            "canceled" -> "❌ ถูกยกเลิก"
            else -> status
        }
    }
}
