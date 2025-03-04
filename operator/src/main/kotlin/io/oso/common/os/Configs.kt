package io.oso.common.os

import com.squareup.moshi.Json

data class Meta(
    val type: String,
    @Json(name = "config_version")
    val configVersion: Int = 2,
)
