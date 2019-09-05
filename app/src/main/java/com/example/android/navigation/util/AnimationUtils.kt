/*
 * Copyright 2018 Mauricio Colli <mauriciocolli@outlook.com>
 * AnimationUtils.java is part of NewPipe
 *
 * License: GPL-3.0+
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.android.navigation.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

object AnimationUtils {
    private val TAG = "AnimationUtils"
    private val DEBUG = true

    enum class Type {
        ALPHA,
        SCALE_AND_ALPHA, LIGHT_SCALE_AND_ALPHA,
        SLIDE_AND_ALPHA, LIGHT_SLIDE_AND_ALPHA
    }

    fun animateView(view: View, enterOrExit: Boolean, duration: Long) {
        animateView(view, Type.ALPHA, enterOrExit, duration, 0, null)
    }

    fun animateView(view: View, enterOrExit: Boolean, duration: Long, delay: Long) {
        animateView(view, Type.ALPHA, enterOrExit, duration, delay, null)
    }

    fun animateView(view: View, enterOrExit: Boolean, duration: Long, delay: Long, execOnEnd: Runnable) {
        animateView(view, Type.ALPHA, enterOrExit, duration, delay, execOnEnd)
    }

    /**
     * Animate the view
     *
     * @param view          view that will be animated
     * @param animationType [Type] of the animation
     * @param enterOrExit   true to enter, false to exit
     * @param duration      how long the animation will take, in milliseconds
     * @param delay         how long the animation will wait to start, in milliseconds
     * @param execOnEnd     runnable that will be executed when the animation ends
     */
    @JvmOverloads
    fun animateView(view: View, animationType: Type, enterOrExit: Boolean, duration: Long, delay: Long = 0, execOnEnd: Runnable? = null) {
        if (DEBUG) {
            var id: String
            try {
                id = view.resources.getResourceEntryName(view.id)
            } catch (e: Exception) {
                id = view.id.toString() + ""
            }

            val msg = String.format("%8s →  [%s:%s] [%s %s:%s] execOnEnd=%s",
                    enterOrExit, view.javaClass.simpleName, id, animationType, duration, delay, execOnEnd)
            Log.d(TAG, "animateView()$msg")
        }

        if (view.visibility == View.VISIBLE && enterOrExit) {
            if (DEBUG) Log.d(TAG, "animateView() view was already visible > view = [$view]")
            view.animate().setListener(null).cancel()
            view.visibility = View.VISIBLE
            view.alpha = 1f
            execOnEnd?.run()
            return
        } else if ((view.visibility == View.GONE || view.visibility == View.INVISIBLE) && !enterOrExit) {
            if (DEBUG) Log.d(TAG, "animateView() view was already gone > view = [$view]")
            view.animate().setListener(null).cancel()
            view.visibility = View.GONE
            view.alpha = 0f
            execOnEnd?.run()
            return
        }

        view.animate().setListener(null).cancel()
        view.visibility = View.VISIBLE

        when (animationType) {
            AnimationUtils.Type.ALPHA -> animateAlpha(view, enterOrExit, duration, delay, execOnEnd)
            AnimationUtils.Type.SCALE_AND_ALPHA -> animateScaleAndAlpha(view, enterOrExit, duration, delay, execOnEnd)
            AnimationUtils.Type.LIGHT_SCALE_AND_ALPHA -> animateLightScaleAndAlpha(view, enterOrExit, duration, delay, execOnEnd)
            AnimationUtils.Type.SLIDE_AND_ALPHA -> animateSlideAndAlpha(view, enterOrExit, duration, delay, execOnEnd)
            AnimationUtils.Type.LIGHT_SLIDE_AND_ALPHA -> animateLightSlideAndAlpha(view, enterOrExit, duration, delay, execOnEnd)
        }
    }


    /**
     * Animate the background color of a view
     */
    fun animateBackgroundColor(view: View, duration: Long, @ColorInt colorStart: Int, @ColorInt colorEnd: Int) {
        if (DEBUG) {
            Log.d(TAG, "animateBackgroundColor() called with: view = [$view], duration = [$duration], colorStart = [$colorStart], colorEnd = [$colorEnd]")
        }

        val EMPTY = arrayOf(IntArray(0))
        val viewPropertyAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorStart, colorEnd)
        viewPropertyAnimator.interpolator = FastOutSlowInInterpolator()
        viewPropertyAnimator.duration = duration
        viewPropertyAnimator.addUpdateListener { animation -> ViewCompat.setBackgroundTintList(view, ColorStateList(EMPTY, intArrayOf(animation.animatedValue as Int))) }
        viewPropertyAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                ViewCompat.setBackgroundTintList(view, ColorStateList(EMPTY, intArrayOf(colorEnd)))
            }

            override fun onAnimationCancel(animation: Animator) {
                onAnimationEnd(animation)
            }
        })
        viewPropertyAnimator.start()
    }

    /**
     * Animate the text color of any view that extends [TextView] (Buttons, EditText...)
     */
    fun animateTextColor(view: TextView, duration: Long, @ColorInt colorStart: Int, @ColorInt colorEnd: Int) {
        if (DEBUG) {
            Log.d(TAG, "animateTextColor() called with: view = [$view], duration = [$duration], colorStart = [$colorStart], colorEnd = [$colorEnd]")
        }

        val viewPropertyAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorStart, colorEnd)
        viewPropertyAnimator.interpolator = FastOutSlowInInterpolator()
        viewPropertyAnimator.duration = duration
        viewPropertyAnimator.addUpdateListener { animation -> view.setTextColor(animation.animatedValue as Int) }
        viewPropertyAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.setTextColor(colorEnd)
            }

            override fun onAnimationCancel(animation: Animator) {
                view.setTextColor(colorEnd)
            }
        })
        viewPropertyAnimator.start()
    }

    fun animateHeight(view: View, duration: Long, targetHeight: Int): ValueAnimator {
        val height = view.height
        if (DEBUG) {
            Log.d(TAG, "animateHeight: duration = [$duration], from $height to → $targetHeight in: $view")
        }

        val animator = ValueAnimator.ofFloat(height as Float, targetHeight as Float)
        animator.interpolator = FastOutSlowInInterpolator()
        animator.duration = duration
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            view.layoutParams.height = value.toInt()
            view.requestLayout()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.layoutParams.height = targetHeight
                view.requestLayout()
            }

            override fun onAnimationCancel(animation: Animator) {
                view.layoutParams.height = targetHeight
                view.requestLayout()
            }
        })
        animator.start()

        return animator
    }

    fun animateRotation(view: View, duration: Long, targetRotation: Int) {
        if (DEBUG) {
            Log.d(TAG, "animateRotation: duration = [" + duration + "], from " + view.rotation + " to → " + targetRotation + " in: " + view)
        }
        view.animate().setListener(null).cancel()

        view.animate().rotation(targetRotation.toFloat()).setDuration(duration).setInterpolator(FastOutSlowInInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationCancel(animation: Animator) {
                        view.rotation = targetRotation.toFloat()
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        view.rotation = targetRotation.toFloat()
                    }
                }).start()
    }

    //////////////////////////////////////////////////////////////////////////
    // Internals
    //////////////////////////////////////////////////////////////////////////

    private fun animateAlpha(view:View, enterOrExit:Boolean, duration:Long, delay:Long, execOnEnd:Runnable?) {
        if (enterOrExit)
        {
            view.animate().setInterpolator(FastOutSlowInInterpolator()).alpha(1f)
                    .setDuration(duration).setStartDelay(delay).setListener(object:AnimatorListenerAdapter() {
                        public override fun onAnimationEnd(animation:Animator) {
                            if (execOnEnd != null) execOnEnd!!.run()
                        }
                    }).start()
        }
        else
        {
            view.animate().setInterpolator(FastOutSlowInInterpolator()).alpha(0f)
                    .setDuration(duration).setStartDelay(delay).setListener(object:AnimatorListenerAdapter() {
                        public override fun onAnimationEnd(animation:Animator) {
                            view.setVisibility(View.GONE)
                            if (execOnEnd != null) execOnEnd!!.run()
                        }
                    }).start()
        }
    }

    private fun animateScaleAndAlpha(view:View, enterOrExit:Boolean, duration:Long, delay:Long, execOnEnd:Runnable?) {
        if (enterOrExit)
        {
            view.setScaleX(.8f)
            view.setScaleY(.8f)
            view.animate().setInterpolator(FastOutSlowInInterpolator()).alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(duration).setStartDelay(delay).setListener(object:AnimatorListenerAdapter() {
                        public override fun onAnimationEnd(animation:Animator) {
                            if (execOnEnd != null) execOnEnd!!.run()
                        }
                    }).start()
        }
        else
        {
            view.setScaleX(1f)
            view.setScaleY(1f)
            view.animate().setInterpolator(FastOutSlowInInterpolator()).alpha(0f).scaleX(.8f).scaleY(.8f)
                    .setDuration(duration).setStartDelay(delay).setListener(object:AnimatorListenerAdapter() {
                        public override fun onAnimationEnd(animation:Animator) {
                            view.setVisibility(View.GONE)
                            if (execOnEnd != null) execOnEnd!!.run()
                        }
                    }).start()
        }
    }

    private fun animateLightScaleAndAlpha(view:View, enterOrExit:Boolean, duration:Long, delay:Long, execOnEnd:Runnable?) {
        if (enterOrExit)
        {
            view.setAlpha(.5f)
            view.setScaleX(.95f)
            view.setScaleY(.95f)
            view.animate().setInterpolator(FastOutSlowInInterpolator()).alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(duration).setStartDelay(delay).setListener(object:AnimatorListenerAdapter() {
                        public override fun onAnimationEnd(animation:Animator) {
                            if (execOnEnd != null) execOnEnd!!.run()
                        }
                    }).start()
        }
        else
        {
            view.setAlpha(1f)
            view.setScaleX(1f)
            view.setScaleY(1f)
            view.animate().setInterpolator(FastOutSlowInInterpolator()).alpha(0f).scaleX(.95f).scaleY(.95f)
                    .setDuration(duration).setStartDelay(delay).setListener(object:AnimatorListenerAdapter() {
                        public override fun onAnimationEnd(animation:Animator) {
                            view.setVisibility(View.GONE)
                            if (execOnEnd != null) execOnEnd!!.run()
                        }
                    }).start()
        }
    }

    private fun animateSlideAndAlpha(view:View, enterOrExit:Boolean, duration:Long, delay:Long, execOnEnd:Runnable?) {
        if (enterOrExit)
        {
            view.setTranslationY((-view.getHeight()).toFloat())
            view.setAlpha(0f)
            view.animate().setInterpolator(FastOutSlowInInterpolator()).alpha(1f).translationY(0f)
                    .setDuration(duration).setStartDelay(delay).setListener(object:AnimatorListenerAdapter() {
                        public override fun onAnimationEnd(animation:Animator) {
                            if (execOnEnd != null) execOnEnd!!.run()
                        }
                    }).start()
        }
        else
        {
            view.animate().setInterpolator(FastOutSlowInInterpolator()).alpha(0f).translationY((-view.getHeight()).toFloat())
                    .setDuration(duration).setStartDelay(delay).setListener(object:AnimatorListenerAdapter() {
                        public override fun onAnimationEnd(animation:Animator) {
                            view.setVisibility(View.GONE)
                            if (execOnEnd != null) execOnEnd!!.run()
                        }
                    }).start()
        }
    }

    private fun animateLightSlideAndAlpha(view:View, enterOrExit:Boolean, duration:Long, delay:Long, execOnEnd:Runnable?) {
        if (enterOrExit)
        {
            view.setTranslationY((-view.getHeight() / 2).toFloat())
            view.setAlpha(0f)
            view.animate().setInterpolator(FastOutSlowInInterpolator()).alpha(1f).translationY(0f)
                    .setDuration(duration).setStartDelay(delay).setListener(object:AnimatorListenerAdapter() {
                        public override fun onAnimationEnd(animation:Animator) {
                            if (execOnEnd != null) execOnEnd!!.run()
                        }
                    }).start()
        }
        else
        {
            view.animate().setInterpolator(FastOutSlowInInterpolator()).alpha(0f).translationY((-view.getHeight() / 2).toFloat())
                    .setDuration(duration).setStartDelay(delay).setListener(object:AnimatorListenerAdapter() {
                        public override fun onAnimationEnd(animation:Animator) {
                            view.setVisibility(View.GONE)
                            if (execOnEnd != null) execOnEnd!!.run()
                        }
                    }).start()
        }
    }

    fun slideUp(view:View,
                duration:Long,
                delay:Long,
                @FloatRange(from = 0.0, to = 1.0) translationPercent:Float) {
        val translationY = ((view.getResources().getDisplayMetrics().heightPixels * (translationPercent))).toInt()

        view.animate().setListener(null).cancel()
        view.setAlpha(0f)
        view.setTranslationY(translationY.toFloat())
        view.setVisibility(View.VISIBLE)
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(duration)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
    }
}
