package io.oso.common

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

object Jsons {
    private val moshi = Moshi.Builder().build()

    inline fun <reified T> toJson(obj: T): String =  toJson(T::class.java, obj)

    fun <T> toJson(clz: Class<T>, obj: T): String {
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(clz)
    }
}