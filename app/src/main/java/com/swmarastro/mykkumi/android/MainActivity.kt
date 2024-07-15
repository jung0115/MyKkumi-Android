package com.swmarastro.mykkumi.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.swmarastro.mykkumi.android.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/*
@AndroidEntryPoint: Activity, Fragment, View, Service, BroadcastReceiver 같은 Android Component에 사용할 수 있는 어노테이션
이를 적용한 컴포넌트 내에서 @Inject가 달린 필드에 의존성 주입을 함
*/
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    // NavController 선언
    private lateinit var navController: NavController

    private var waitTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // 최상위 화면을 제외하고는 BottomNavigation Bar 없애기
        setBottomNavigation()
    }

    private fun setBottomNavigation() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                com.swmarastro.mykkumi.feature.home.R.id.homeFragment,
                com.swmarastro.mykkumi.feature.around.R.id.aroundFragment,
                com.swmarastro.mykkumi.feature.shopping.R.id.shoppingFragment,
                com.swmarastro.mykkumi.feature.mypage.R.id.mypageFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNav.visibility = View.GONE
                }
            }
        }
    }

    override fun onBackPressed() {
        if(navController.currentDestination?.id == com.swmarastro.mykkumi.feature.home.R.id.homeFragment ||
            navController.currentDestination?.id == com.swmarastro.mykkumi.feature.around.R.id.aroundFragment ||
            navController.currentDestination?.id == com.swmarastro.mykkumi.feature.shopping.R.id.shoppingFragment ||
            navController.currentDestination?.id == com.swmarastro.mykkumi.feature.mypage.R.id.mypageFragment) {
            if(System.currentTimeMillis() - waitTime >= 1500 ) {
                waitTime = System.currentTimeMillis()
                Toast.makeText(this, R.string.back_pressed_toast, Toast.LENGTH_SHORT).show()
            } else {
                finish() // 액티비티 종료
            }
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}