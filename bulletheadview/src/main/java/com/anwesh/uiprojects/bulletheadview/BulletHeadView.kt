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
    save()
    translate(w / 2 + (w / 2 + size) * sc2.divideScale(1, parts) * i.sjf(), gap * (i + 1))
    rotate(90f * sc2.divideScale(0, parts))
    drawBulletLine(size, sc1, paint)
    restore()
}

class BulletHeadView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN  -> {

            }
        }
        return true
    }
}