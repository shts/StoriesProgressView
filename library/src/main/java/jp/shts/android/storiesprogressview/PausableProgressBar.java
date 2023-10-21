package jp.shts.android.storiesprogressview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

final class PausableProgressBar extends FrameLayout {

    /***
     * progress満了タイマーのデフォルト時間
     */
    private static final int DEFAULT_PROGRESS_DURATION = 2000;

    private final View frontProgressView;
    private final View maxProgressView;

    private PausableScaleAnimation animation;
    private long duration = DEFAULT_PROGRESS_DURATION;
    private Callback callback;

    interface Callback {
        void onStartProgress();

        void onFinishProgress();
    }

    public PausableProgressBar(Context context) {
        this(context, null);
    }

    public PausableProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PausableProgressBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.pausable_progress, this);
        frontProgressView = findViewById(R.id.front_progress);
        maxProgressView = findViewById(R.id.max_progress); // work around
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCallback(@NonNull Callback callback) {
        this.callback = callback;
    }

    void setMax() {
        finishProgress(true);
    }

    void setMin() {
        finishProgress(false);
    }

    void setMinWithoutCallback() {
        maxProgressView.setBackgroundResource(R.color.progress_secondary);

        maxProgressView.setVisibility(VISIBLE);
        if (animation != null) {
            animation.removeAllListeners();
            animation.cancel();
        }
    }

    void setMaxWithoutCallback() {
        maxProgressView.setBackgroundResource(R.color.progress_max_active);

        maxProgressView.setVisibility(VISIBLE);
        if (animation != null) {
            animation.removeAllListeners();
            animation.cancel();
        }
    }

    private void finishProgress(boolean isMax) {
        if (isMax) maxProgressView.setBackgroundResource(R.color.progress_max_active);
        frontProgressView.setScaleX(isMax ? 1f : 0f);
        maxProgressView.setVisibility(isMax ? VISIBLE : GONE);
        if (animation != null) {
            animation.removeAllListeners();
            animation.cancel();
            if (callback != null) {
                callback.onFinishProgress();
            }
        }
    }

    public void startProgress() {
        maxProgressView.setVisibility(GONE);
        animation = new PausableScaleAnimation(frontProgressView, 0F, 1F, 0F, 0F);
        animation.setDuration(duration);
        animation.setInterpolator(new LinearInterpolator());
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                frontProgressView.setVisibility(View.VISIBLE);
                if (callback != null) callback.onStartProgress();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (callback != null) callback.onFinishProgress();
            }
        });
        animation.start();
    }

    public void pauseProgress() {
        if (animation != null) {
            animation.pause();
        }
    }

    public void resumeProgress() {
        if (animation != null) {
            animation.resume();
        }
    }

    void clear() {
        if (animation != null) {
            animation.removeAllListeners();
            animation.cancel();
            animation = null;
        }
    }

    private static class PausableScaleAnimation {
        private boolean paused = false;
        private final ObjectAnimator animator;
        private ArrayList<Animator.AnimatorListener> listeners;
        private long currentPlayTime = 0;

        public PausableScaleAnimation(View view, float fromXScale,
                                      float toXScale, float pivotX, float pivotY) {
            animator = createPausableScaleAnimator(view, fromXScale, toXScale, pivotX, pivotY);
        }

        private ObjectAnimator createPausableScaleAnimator(View view, float fromXScale,
                                                           float toXScale, float pivotX, float pivotY) {
            view.setPivotX(pivotX);
            view.setPivotY(pivotY);
            return ObjectAnimator.ofFloat(view, View.SCALE_X, fromXScale, toXScale);
        }

        public void start() {
            animator.start();
        }

        public void pause() {
            if (paused) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animator.pause();
            } else {
                // save currentPlayTime and clear listeners to avoid sending events
                currentPlayTime = animator.getCurrentPlayTime();
                clearAnimatorListeners();
            }
            paused = true;
        }

        public void resume() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animator.resume();
            } else {
                // start animation, set currentPlayTime
                // and reinitialize listeners
                start();
                animator.setCurrentPlayTime(currentPlayTime);
                setUpListeners();
            }
            paused = false;
        }

        private void clearAnimatorListeners() {
            listeners = new ArrayList<>();
            listeners.addAll(animator.getListeners());
            for (Animator.AnimatorListener listener : listeners) {
                animator.removeListener(listener);
            }
            cancel();
        }

        private void setUpListeners() {
            for (Animator.AnimatorListener listener : listeners) {
                animator.addListener(listener);
            }
        }

        public void setDuration(long duration) {
            animator.setDuration(duration);
        }

        public void setInterpolator(LinearInterpolator interpolator) {
            animator.setInterpolator(interpolator);
        }

        public void addListener(AnimatorListenerAdapter listener) {
            animator.addListener(listener);
        }

        public void removeAllListeners() {
            animator.removeAllListeners();
        }

        public void cancel() {
            animator.cancel();
        }
    }
}
