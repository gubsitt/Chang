package mobile.nganchang.chang.technician

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
    private val bookingsList = mutableListOf<Map<String, Any>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_technician_bookings, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.recycler_bookings)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = BookingAdapter(bookingsList) { booking, action ->
            updateBookingStatus(booking, action)
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
                    bookingsList.clear()
                    for (document in documents) {
                        val booking = document.data
                        booking["bookingId"] = document.id
                        bookingsList.add(booking)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "โหลดข้อมูลล้มเหลว: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateBookingStatus(booking: Map<String, Any>, action: String) {
        val bookingId = booking["bookingId"] as String

        db.collection("bookings").document(bookingId)
            .update("status", action)
            .addOnSuccessListener {
                Toast.makeText(context, "อัปเดตสถานะเป็น $action สำเร็จ!", Toast.LENGTH_SHORT).show()
                loadBookings()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "อัปเดตสถานะล้มเหลว: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
