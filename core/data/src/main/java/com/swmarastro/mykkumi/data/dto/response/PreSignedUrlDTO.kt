package com.swmarastro.mykkumi.data.dto.response

import com.google.gson.annotations.SerializedName

data class PreSignedUrlDTO(
    @SerializedName("posts")
    val url: String
)
