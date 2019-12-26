package jp.shts.android.storyprogressbar

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

import android.widget.ImageView

import jp.shts.android.storiesprogressview.StoriesProgressView
import timber.log.Timber

class StoryFragment : Fragment(), StoriesProgressView.StoriesListener {

    companion object {
        private const val PROGRESS_COUNT = 6
        private const val EXTRA_POSITION = "extra_position"

        fun createIntent(position: Int): StoryFragment {
            return StoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_POSITION, position)
                }
            }
        }
    }

    private var storiesProgressView: StoriesProgressView? = null
    private var image: ImageView? = null

    private var counter = 0
    private val resources = intArrayOf(
            R.drawable.sample1,
            R.drawable.sample2,
            R.drawable.sample3,
            R.drawable.sample4,
            R.drawable.sample5,
            R.drawable.sample6
    )

    private val durations = longArrayOf(500L, 1000L, 1500L, 4000L, 5000L, 1000)

    private var pressTime = 0L
    private var limit = 500L

    private val onTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storiesProgressView?.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                storiesProgressView?.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }

    private var pageViewOperator: PageViewOperator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PageViewOperator) {
            this.pageViewOperator = context
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.v("${arguments?.getInt(EXTRA_POSITION)}: onStart")
        counter = restorePosition()
    }

    override fun onStop() {
        super.onStop()
        Timber.v("${arguments?.getInt(EXTRA_POSITION)}: onStop")
    }

    override fun onResume() {
        super.onResume()
        Timber.d("${arguments?.getInt(EXTRA_POSITION)}: onResume")
        if (counter == 0) {
            // start animation
            storiesProgressView?.startStories()
        } else {
            // restart animation
            counter = MainActivity.progressState.get(arguments?.getInt(EXTRA_POSITION) ?: 0)
            Timber.d("startStories onResume(): currentPage(${arguments?.getInt(EXTRA_POSITION)}) counter($counter)")
            storiesProgressView?.startStories(counter)
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.v("${arguments?.getInt(EXTRA_POSITION)}: onPause")
//        storiesProgressView?.pause()
        storiesProgressView?.abandon()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        counter = restorePosition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_story, container, false)
        storiesProgressView = view.findViewById(R.id.stories)
        storiesProgressView?.setStoriesCount(PROGRESS_COUNT)
        storiesProgressView?.setStoryDuration(3000L)
        storiesProgressView?.setStoriesListener(this)

        // bind image
        image = view.findViewById(R.id.image)
        image?.setImageResource(resources[counter])

        // bind reverse view
        val reverse = view.findViewById<View>(R.id.reverse)
        reverse.setOnClickListener {
            if (counter == 0) {
                pageViewOperator?.backPageView()
            } else {
                storiesProgressView?.reverse()
            }
        }
        reverse.setOnTouchListener(onTouchListener)

        // bind skip view
        val skip = view.findViewById<View>(R.id.skip)
        skip.setOnClickListener {
            Timber.d("counter($counter) resources.size(${resources.size})")
            if (counter == resources.size - 1) {
                pageViewOperator?.nextPageView()
            } else {
                storiesProgressView?.skip()
            }
        }
        skip.setOnTouchListener(onTouchListener)
        return view
    }

    fun resume() {
        Timber.d("${arguments?.getInt(EXTRA_POSITION)}: resume")
        storiesProgressView?.resume()
    }

    override fun onDestroyView() {
        Timber.w("${arguments?.getInt(EXTRA_POSITION)}: onDestroyView")
        savePosition(counter)
        // Very important !
        storiesProgressView?.destroy()
        super.onDestroyView()
    }

    private fun savePosition(position: Int) {
        MainActivity.progressState.put(arguments!!.getInt(EXTRA_POSITION), position)
    }

    private fun restorePosition(): Int {
        return MainActivity.progressState.get(arguments!!.getInt(EXTRA_POSITION))
    }

    /* implement StoriesProgressView.StoriesListener start */
    override fun onNext() {
        Timber.d("onNext(): counter($counter)")
        if (resources.size < counter + 1) {
            return
        }
        savePosition(counter + 1)
        image?.setImageResource(resources[++counter])
    }

    override fun onPrev() {
        if (counter - 1 < 0) {
            return
        }
        savePosition(counter - 1)
        image?.setImageResource(resources[--counter])
    }

    override fun onComplete() {
        pageViewOperator?.nextPageView()
    }
    /* implement StoriesProgressView.StoriesListener end */
}
