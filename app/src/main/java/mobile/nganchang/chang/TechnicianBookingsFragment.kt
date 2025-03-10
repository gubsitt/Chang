package mobile.nganchang.chang.technician

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobile.nganchang.chang.R

class TechnicianBookingsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookingAdapter
    private var bookingsList = mutableListOf<Map<String, Any>>()  // ใช้ List เก็บข้อมูล

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_technician_bookings, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.recycler_view_bookings)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = BookingAdapter(bookingsList) { booking, status ->
            updateBookingStatus(booking["id"] as String, status)
        }

        recyclerView.adapter = adapter

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
                    bookingsList.clear() // เคลียร์รายการเก่า

                    if (documents.isEmpty) {
                        Toast.makeText(requireContext(), "ไม่มีการจองของคุณ", Toast.LENGTH_SHORT).show()
                        adapter.notifyDataSetChanged()
                        return@addOnSuccessListener
                    }

                    for (document in documents) {
                        val bookingData = document.data.toMutableMap()
                        bookingData["id"] = document.id // เก็บ ID ของ booking ไว้
                        bookingsList.add(bookingData)
                    }

                    adapter.notifyDataSetChanged() // แจ้ง RecyclerView ว่าข้อมูลเปลี่ยน
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateBookingStatus(bookingId: String, status: String) {
        db.collection("bookings").document(bookingId)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "อัปเดตสถานะเป็น $status สำเร็จ!", Toast.LENGTH_SHORT).show()
                loadBookings()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "อัปเดตสถานะล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }
}
