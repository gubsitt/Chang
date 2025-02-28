package mobile.nganchang.chang.technician

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import mobile.nganchang.chang.R

class TechnicianMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_technician_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(TechnicianHomeFragment())
                R.id.nav_booking -> loadFragment(TechnicianBookingsFragment())
                R.id.nav_earnings -> loadFragment(TechnicianEarningsFragment())
                R.id.nav_edit_profile -> loadFragment(TechnicianEditProfileFragment()) // ✅ เพิ่มหน้าแก้ไขโปรไฟล์
            }
            true
        }

        // โหลดหน้าแรกเป็นหน้า Home ก่อน
        loadFragment(TechnicianHomeFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
