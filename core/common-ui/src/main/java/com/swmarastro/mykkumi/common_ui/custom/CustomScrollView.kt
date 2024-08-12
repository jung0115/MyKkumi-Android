package com.swmarastro.mykkumi.common_ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

// 수직 스크롤 제어를 위한 커스텀 스크롤뷰
class CustomScrollView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {
    private var isScrollable = true

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return isScrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isScrollable && super.onInterceptTouchEvent(ev)
    }

    fun setScrollingEnabled(enabled: Boolean) {
        isScrollable = enabled
    }
}