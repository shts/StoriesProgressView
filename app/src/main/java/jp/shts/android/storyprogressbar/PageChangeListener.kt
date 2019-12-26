package jp.shts.android.storyprogressbar

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import androidx.viewpager.widget.ViewPager.*
import timber.log.Timber

abstract class PageChangeListener : OnPageChangeListener {

    companion object {
        private const val DEBOUNCE_TIMES = 500L
    }

    private var pageBeforeDragging = 0
    private var currentPage = 0
    private var lastTime = DEBOUNCE_TIMES + 1L

    override fun onPageScrollStateChanged(state: Int) {
        when (state) {
            SCROLL_STATE_IDLE -> {
                Timber.d("onPageScrollStateChanged(): SCROLL_STATE_IDLE")
                // 500ms 以下間隔のリクエストは破棄する
                val now = System.currentTimeMillis()
                if (now - lastTime < DEBOUNCE_TIMES) {
                    return
                }
                lastTime = now
                Handler().postDelayed({
                    if (pageBeforeDragging == currentPage) {
                        onPageScrollCanceled()
                    }
                }, 300L)
            }
            SCROLL_STATE_DRAGGING -> {
                Timber.d("onPageScrollStateChanged(): SCROLL_STATE_DRAGGING")
                pageBeforeDragging = currentPage
            }
            SCROLL_STATE_SETTLING -> {
                Timber.d("onPageScrollStateChanged(): SCROLL_STATE_SETTLING")
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        Timber.d("onPageSelected(): position($position)")
        currentPage = position
    }

    abstract fun onPageScrollCanceled()
}
