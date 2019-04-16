package com.anwesh.uiprojects.bulletheadview

/**
 * Created by anweshmishra on 16/04/19.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5
val lines : Int = 2
val parts : Int = 2
val angleDeg : Float = 90f
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap
fun Int.sjf() : Float = 1f - 2 * (this % 2)

fun Canvas.drawBulletLine(size : Float, sc : Float, paint : Paint) {
    drawArc(RectF(-size, -size, size, size), 180f, 180f, true, paint)
    for (j in 0..(lines - 1)) {
        save()
        translate(-size + 2 * size * j, 0f)
        drawLine(0f, 0f, 0f, size * sc.divideScale(j, lines), paint)
        restore()
    }
}

fun Canvas.drawBHNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    paint.style = Paint.Style.STROKE
    save()
    translate(w / 2 + (w / 2 + size) * sc2.divideScale(1, parts) * i.sjf(), gap * (i + 1))
    rotate(angleDeg * sc2.divideScale(0, parts) * i.sjf())
    drawBulletLine(size, sc1, paint)
    restore()
}

class BulletHeadView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)
    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN  -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, parts)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BHNode(var i : Int, val state : State = State()) {

        private var next : BHNode? = null
        private var prev : BHNode? = null

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BHNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBHNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BHNode {
            var curr : BHNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BulletHead(var i : Int) {

        private val root : BHNode = BHNode(0)
        private var curr : BHNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BulletHeadView) {

        private val animator : Animator = Animator(view)
        private var bh : BulletHead = BulletHead(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            bh.draw(canvas, paint)
            animator.animate {
                bh.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bh.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BulletHeadView {
            val view : BulletHeadView = BulletHeadView(activity)
            activity.setContentView(view)
            return view
        }
    }
}