package mobile.nganchang.chang.customer

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
import mobile.nganchang.chang.R

class BookingFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var bookingsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_booking, container, false)
        db = FirebaseFirestore.getInstance()
        bookingsContainer = view.findViewById(R.id.bookings_container)

        loadBookings()
        return view
    }

    private fun loadBookings() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("bookings").whereEqualTo("customer_id", userId)
            .get()
            .addOnSuccessListener { result ->
                bookingsContainer.removeAllViews()

                // ถ้าไม่มีข้อมูล ให้แสดงข้อความ
                if (result.isEmpty) {
                    val noBookingsTextView = TextView(requireContext()).apply {
                        text = "ไม่มีการจองของคุณ"
                        textSize = 18f
                        setPadding(16, 16, 16, 16)
                        setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    }
                    bookingsContainer.addView(noBookingsTextView)
                    return@addOnSuccessListener
                }

                // วนลูปดึงข้อมูลจาก Firestore และใส่ลงใน Layout
                for (document in result) {
                    val technicianName = document.getString("technician_name") ?: "ไม่ระบุ"
                    val workType = document.getString("work_type") ?: "ไม่ระบุประเภท"
                    val status = document.getString("status") ?: "pending"
                    val price = document.getLong("price") ?: 0L
                    val bookingId = document.id

                    // ใช้ item_bookingc.xml แทนการสร้าง CardView ในโค้ด
                    val bookingView = layoutInflater.inflate(R.layout.item_bookingc, bookingsContainer, false)

                    // กำหนดค่าจาก Firestore ให้ View
                    bookingView.findViewById<TextView>(R.id.technician_name).text = "ช่าง: $technicianName"
                    bookingView.findViewById<TextView>(R.id.work_type).text = "ประเภท: $workType"
                    bookingView.findViewById<TextView>(R.id.price).text = "ราคา: ฿$price"
                    bookingView.findViewById<TextView>(R.id.status).text = getStatusText(status)

                    val cancelButton = bookingView.findViewById<MaterialButton>(R.id.cancel_button)
                    if (status == "pending") {
                        cancelButton.visibility = View.VISIBLE
                        cancelButton.setOnClickListener { cancelBooking(bookingId) }
                    } else {
                        cancelButton.visibility = View.GONE
                    }

                    // เพิ่ม View ลงใน bookingsContainer
                    bookingsContainer.addView(bookingView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "โหลดการจองล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cancelBooking(bookingId: String) {
        db.collection("bookings").document(bookingId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "ยกเลิกการจองสำเร็จ", Toast.LENGTH_SHORT).show()
                loadBookings()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "ไม่สามารถยกเลิกการจองได้", Toast.LENGTH_SHORT).show()
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
