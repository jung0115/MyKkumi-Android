package com.swmarastro.mykkumi.common_ui.report

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import com.swmarastro.mykkumi.common_ui.databinding.DialogPostReportConfirmBinding

class PostReportConfirmDialog(private val context: Fragment) {
    private val binding by lazy { DialogPostReportConfirmBinding.inflate(context.layoutInflater) }

    private val dialog = Dialog(context.requireContext())
    private lateinit var listener: PostReportConfirmListener

    interface PostReportConfirmListener {
        fun confirmPostReport(postId: Int)
    }

    fun setOnClickListener(listener: (Int) -> Unit) {
        this.listener = object: PostReportConfirmListener {
            override fun confirmPostReport(postId: Int) {
                listener(postId)
            }
        }
    }

    fun show(postId: Int) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀바 제거
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게
        dialog.setContentView(binding.root)

        binding.textBtnPostReportConfirm.setOnClickListener(View.OnClickListener {
            listener.confirmPostReport(postId)
            dialog.dismiss()
        })

        binding.textBtnPostReportCancel.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        dialog.show()
    }
}