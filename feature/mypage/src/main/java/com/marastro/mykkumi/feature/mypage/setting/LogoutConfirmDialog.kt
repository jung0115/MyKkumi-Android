package com.marastro.mykkumi.feature.mypage.setting

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat.getString
import androidx.fragment.app.Fragment
import com.marastro.mykkumi.analytics.AnalyticsHelper
import com.marastro.mykkumi.feature.mypage.databinding.DialogLogoutConfirmBinding

class LogoutConfirmDialog (
    private val context: Fragment,
    private val analyticsHelper: AnalyticsHelper
) {
    private val binding by lazy { DialogLogoutConfirmBinding.inflate(context.layoutInflater) }

    private val dialog = Dialog(context.requireContext())
    private lateinit var listener: LogoutConfirmListener

    interface LogoutConfirmListener {
        fun confirmLogout()
    }

    fun setOnClickListener(listener: () -> Unit) {
        this.listener = object: LogoutConfirmListener {
            override fun confirmLogout() {
                listener()
            }
        }
    }

    fun show() {
        // Firebase Analytics 화면 이름 로깅
        analyticsHelper.logScreenView(getString(context.requireContext(), com.marastro.mykkumi.analytics.R.string.logout_confirm_dialog))

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀바 제거
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게
        dialog.setContentView(binding.root)

        binding.textBtnLogoutConfirm.setOnClickListener(View.OnClickListener {
            listener.confirmLogout()
            dialog.dismiss()
        })

        binding.textBtnLogoutCancel.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        dialog.show()
    }
}