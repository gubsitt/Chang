package mobile.nganchang.chang.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
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

                for (document in result) {
                    val technicianName = document.getString("technician_name") ?: "ไม่ระบุ"
                    val workType = document.getString("work_type") ?: "ไม่ระบุประเภท"
                    val status = document.getString("status") ?: "pending"
                    val price = document.getLong("price") ?: 0L // เพิ่มการดึงราคา
                    val bookingId = document.id // ID ของเอกสาร Firestore

                    // สร้าง MaterialCardView สำหรับแต่ละการจอง
                    val cardView = MaterialCardView(requireContext()).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(16, 8, 16, 8)
                        }
                        radius = 16f
                        cardElevation = 6f
                        strokeWidth = 2
                        strokeColor = resources.getColor(R.color.teal_700, null)
                        setPadding(24, 16, 24, 16)
                    }

                    // Layout สำหรับข้อความ
                    val layout = LinearLayout(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.VERTICAL
                    }

                    // ข้อความแสดงรายละเอียดการจอง
                    val textView = TextView(requireContext()).apply {
                        text = "ช่าง: $technicianName\nประเภท: $workType\nสถานะ: ${getStatusText(status)}\nราคา: ฿$price"
                        textSize = 16f
                        setTextColor(resources.getColor(android.R.color.black, null))
                        setPadding(8, 4, 8, 4)
                    }

                    layout.addView(textView)

                    // ปุ่มยกเลิกการจอง (ถ้าสถานะยังเป็น pending)
                    if (status == "pending") {
                        val cancelButton = MaterialButton(requireContext()).apply {
                            text = "ยกเลิกการจอง"
                            setBackgroundColor(resources.getColor(R.color.red_700, null))
                            setTextColor(resources.getColor(android.R.color.white, null))
                            setPadding(16, 8, 16, 8)
                            setOnClickListener {
                                cancelBooking(bookingId)
                            }
                        }
                        layout.addView(cancelButton)
                    }

                    cardView.addView(layout)
                    bookingsContainer.addView(cardView)
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
                loadBookings() // โหลดรายการใหม่
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

