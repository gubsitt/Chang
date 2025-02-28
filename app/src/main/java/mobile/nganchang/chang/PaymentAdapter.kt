import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import mobile.nganchang.chang.R


class PaymentAdapter(context: Context, private val data: List<Map<String, String>>, private val paymentCallback: PaymentCallback) : ArrayAdapter<Map<String, String>>(context, 0, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.payment_item, parent, false)

        val booking = data[position]
        val technicianName = view.findViewById<TextView>(R.id.technician_name)
        val workType = view.findViewById<TextView>(R.id.work_type)
        val price = view.findViewById<TextView>(R.id.price)
        val payButton = view.findViewById<MaterialButton>(R.id.pay_button)
        val statusTextView = view.findViewById<TextView>(R.id.status) // TextView ใหม่สำหรับแสดงสถานะ

        technicianName.text = "ช่าง: ${booking["technician_name"]}"
        workType.text = "ประเภท: ${booking["work_type"]}"
        price.text = "ราคา: ${booking["price"]}"

        // ตรวจสอบสถานะและแสดงข้อความ "ชำระแล้ว" หากสถานะเป็น "paid"
        val status = booking["status"]
        if (status == "paid") {
            statusTextView.text = "ชำระแล้ว"
            statusTextView.setTextColor(context.resources.getColor(R.color.green_700)) // เปลี่ยนสีถ้าเป็นสถานะ "paid"
            payButton.isEnabled = false // ปิดปุ่มชำระเงินเมื่อชำระแล้ว
        } else {
            statusTextView.text = "รอดำเนินการ"
            statusTextView.setTextColor(context.resources.getColor(R.color.red_700))
            payButton.isEnabled = true
        }

        payButton.setOnClickListener {
            // เรียกใช้ฟังก์ชัน initiatePayment และส่ง booking_id
            val bookingId = booking["booking_id"] ?: ""
            paymentCallback.initiatePayment(bookingId)
        }

        return view
    }

    // สร้าง interface สำหรับ callback
    interface PaymentCallback {
        fun initiatePayment(bookingId: String)
    }
}
