package mobile.nganchang.chang.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import mobile.nganchang.chang.R

class CustomerMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // เปิดหน้าแรกเป็น HomeFragment
        replaceFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_booking -> replaceFragment(BookingFragment())
                R.id.nav_payment -> replaceFragment(PaymentFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }
}
