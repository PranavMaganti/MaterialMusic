package com.vanpra.materialmusic

import android.database.Cursor
import androidx.animation.AnimatedFloat
import androidx.animation.AnimationClockObservable
import androidx.animation.Spring
import androidx.compose.Composable
import androidx.ui.core.Constraints
import androidx.ui.core.DensityAmbient
import androidx.ui.layout.DpConstraints
import androidx.ui.unit.Dp

@Composable
fun Dp.toPx(): Int = with(DensityAmbient.current) { this@toPx.toIntPx() }

@Composable
fun Constraints.toDp(): DpConstraints = with(DensityAmbient.current) { DpConstraints(this@toDp) }

fun <T> Cursor.map(mapFunc: (Cursor) -> T?) : List<T> {
    return mutableListOf<T>().also { list ->
        if (moveToFirst()) {
            do {
                val item = mapFunc(this)
                if(item != null) {
                    list.add(item)
                }
            } while (moveToNext())
        }
    }
}

class CustomAnimatedFloat(
    initial: Float,
    clock: AnimationClockObservable,
    var onNewValue: (Float) -> Boolean
) : AnimatedFloat(clock, Spring.DefaultDisplacementThreshold) {

    override var value = initial
        set(value) {
            if (onNewValue(value)) {
                field = value
            }
        }
}