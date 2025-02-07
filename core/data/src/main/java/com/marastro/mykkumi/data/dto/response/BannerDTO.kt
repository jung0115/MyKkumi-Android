package com.marastro.mykkumi.data.dto.response

import com.google.gson.annotations.SerializedName
import com.marastro.mykkumi.domain.entity.BannerDetailVO
import com.marastro.mykkumi.domain.entity.BannerItemVO
import com.marastro.mykkumi.domain.entity.BannerListVO

data class BannerListDTO(
    @SerializedName("banners")
    val banners: List<BannerItemDTO>
) {
    fun toEntity(): BannerListVO = BannerListVO(
        banners = banners.map { it.toEntity() }
    )

    data class BannerItemDTO(
        @SerializedName("id")
        val id: Int,

        @SerializedName("imageUrl")
        val imageUrl: String
    ) {
        fun toEntity(): BannerItemVO = BannerItemVO(
            id = id,
            imageUrl = imageUrl
        )
    }
}

data class BannerDetailDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("imageUrl")
    val imageUrl: String
) {
    fun toEntity(): BannerDetailVO = BannerDetailVO(
        id = id,
        imageUrl = imageUrl
    )
}

