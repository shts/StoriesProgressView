package jp.shts.android.storyprogressbar

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.SparseIntArray
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import com.ToxicBakery.viewpager.transforms.CubeOutTransformer
import timber.log.Timber

interface PageViewOperator {
    fun backPageView()
    fun nextPageView()
}

class MainActivity : AppCompatActivity(), PageViewOperator {

    companion object {
        private const val PAGE_COUNT = 5
        /* key: page-count, value: story-count */
        val progressState = SparseIntArray()
    }

    private lateinit var viewPager: ViewPager
    private lateinit var pageAdapter: PageAdapter

    private var currentPage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        Timber.plant(Timber.DebugTree())

        pageAdapter = PageAdapter(supportFragmentManager)
        viewPager = findViewById(R.id.viewpager)
        viewPager.adapter = pageAdapter
        viewPager.setPageTransformer(true, CubeOutTransformer())
        viewPager.addOnPageChangeListener(object : PageChangeListener() {
            override fun onPageScrollCanceled() {
                currentFragment()?.resume()
            }
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
//                currentFragment()?.resume()
            }
        })
    }

    private fun currentFragment(): StoryFragment? {
        return pageAdapter.findFragmentByPosition(viewPager, currentPage) as StoryFragment
    }

    override fun backPageView() {
        if (viewPager.currentItem > 0) {
            fakeDrag(false)
        }
    }

    override fun nextPageView() {
        if (viewPager.currentItem + 1 < viewPager.adapter?.count ?: 0) {
            fakeDrag(true)
        }
    }

    private var prevDragPosition = 0

    /**
     * Change ViewPage sliding programmatically(not using reflection).
     * https://tech.dely.jp/entry/2018/12/13/110000
     * What for?
     * setCurrentItem(int, boolean) changes too fast. And it cannot set animation duration.
     */
    private fun fakeDrag(forward: Boolean) {
        if (prevDragPosition == 0 && viewPager.beginFakeDrag()) {
            ValueAnimator.ofInt(0, viewPager.width).apply {
                duration = 500L
                interpolator = FastOutSlowInInterpolator()
                addListener(object : Animator.AnimatorListener {

                    override fun onAnimationStart(animation: Animator?) = Unit

                    override fun onAnimationEnd(animation: Animator?) {
                        removeAllUpdateListeners()
                        if (viewPager.isFakeDragging) {
                            viewPager.endFakeDrag()
                        }
                        prevDragPosition = 0
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        removeAllUpdateListeners()
                        if (viewPager.isFakeDragging) {
                            viewPager.endFakeDrag()
                        }
                        prevDragPosition = 0
                    }

                    override fun onAnimationRepeat(animation: Animator?) = Unit
                })
                addUpdateListener {
                    if (!viewPager.isFakeDragging) {
                        return@addUpdateListener
                    }
                    val dragPosition: Int = it.animatedValue as Int
                    val dragOffset: Float = ((dragPosition - prevDragPosition) * if (forward) -1 else 1).toFloat()
                    prevDragPosition = dragPosition
                    viewPager.fakeDragBy(dragOffset)
                }
            }.start()
        }
    }

    private class PageAdapter internal constructor(fragmentManager: FragmentManager) :
            FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment = StoryFragment.createIntent(position)
        override fun getCount(): Int {
            return PAGE_COUNT
        }
        /**
         * https://qiita.com/chooblarin/items/88b4accac0cbb6944d4b#%E6%96%B9%E6%B3%953-instantiateitem%E3%82%92%E4%BD%BF%E3%81%86
         */
        fun findFragmentByPosition(viewPager: ViewPager, position: Int): Fragment? {
            try {
                val f = instantiateItem(viewPager, position)
                return f as? Fragment
            } finally {
                finishUpdate(viewPager)
            }
        }
    }
}