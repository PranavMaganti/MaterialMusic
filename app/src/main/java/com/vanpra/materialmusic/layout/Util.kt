package com.vanpra.materialmusic.layout

import android.database.Cursor

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