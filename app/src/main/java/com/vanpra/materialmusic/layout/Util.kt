package com.vanpra.materialmusic.layout

import android.database.Cursor
import androidx.compose.animation.core.AnimatedFloat
import androidx.compose.animation.core.AnimationClockObservable
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.DpConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

@Composable
fun Dp.toPx(): Int = with(DensityAmbient.current) { this@toPx.toIntPx() }

@Composable
fun Constraints.toDp(): DpConstraints = with(DensityAmbient.current) { DpConstraints(this@toDp) }

fun <T> Cursor.map(mapFunc: (Cursor) -> T?): List<T> {
    return mutableListOf<T>().also { list ->
        if (moveToFirst()) {
            do {
                val item = mapFunc(this)
                if (item != null) {
                    list.add(item)
                }
            } while (moveToNext())
        }
    }
}

class CustomAnimatedFloat(
    initial: Float,
    clock: AnimationClockObservable,
    var onNewValue: (Float) -> Unit,
    var useNewValue: MutableState<Boolean>
) : AnimatedFloat(clock, Spring.DefaultDisplacementThreshold) {

    override var value = initial
        set(value) {
            if (useNewValue.value) {
                onNewValue(value)
                field = value
            }
        }
}