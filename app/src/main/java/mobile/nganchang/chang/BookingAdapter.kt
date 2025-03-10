package mobile.nganchang.chang.technician

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mobile.nganchang.chang.R

class BookingAdapter(
    private val bookings: List<Map<String, Any>>,
    private val onStatusChange: (Map<String, Any>, String) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCustomerName: TextView = view.findViewById(R.id.tv_customer_name)
        val tvWorkType: TextView = view.findViewById(R.id.tv_service_type)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val btnCancel: Button = view.findViewById(R.id.btn_cancel)
        val btnPaid: Button = view.findViewById(R.id.btn_paid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        holder.tvCustomerName.text = "ลูกค้า: ${booking["customer_name"] ?: "ไม่ระบุ"}"
        holder.tvWorkType.text = "บริการ: ${booking["work_type"] ?: "ไม่ระบุ"}"
        holder.tvStatus.text = "สถานะ: ${convertStatus(booking["status"] as String)}"

        val status = booking["status"] as String
        holder.btnCancel.visibility = if (status == "pending") View.VISIBLE else View.GONE
        holder.btnPaid.visibility = if (status == "pending") View.VISIBLE else View.GONE

        holder.btnCancel.setOnClickListener { onStatusChange(booking, "canceled") }
        holder.btnPaid.setOnClickListener { onStatusChange(booking, "paid") }
    }

    override fun getItemCount(): Int = bookings.size

    private fun convertStatus(status: String): String {
        return when (status) {
            "pending" -> "⏳ รอการยืนยัน"
            "paid" -> "💰 ชำระเงินแล้ว"
            "canceled" -> "❌ ถูกยกเลิก"
            else -> "⚠️ ไม่ทราบสถานะ"
        }
    }
}
