package com.marastro.mykkumi.data.repository

import com.google.gson.Gson
import com.marastro.mykkumi.data.datasource.UserInfoDataSource
import com.marastro.mykkumi.data.dto.request.UpdateUserInfoRequestDTO
import com.marastro.mykkumi.domain.exception.ApiException
import com.marastro.mykkumi.domain.exception.ErrorResponse
import com.marastro.mykkumi.domain.entity.UpdateUserInfoRequestVO
import com.marastro.mykkumi.domain.entity.UpdateUserInfoResponseVO
import com.marastro.mykkumi.domain.entity.UserInfoVO
import com.marastro.mykkumi.domain.repository.UserInfoRepository
import retrofit2.HttpException
import javax.inject.Inject

class UserInfoRepositoryImpl @Inject constructor(
    private val userInfoDataSource: UserInfoDataSource,
) :UserInfoRepository {

    private companion object {
        private const val INVALID_TOKEN = "INVALID_TOKEN"
        private const val DUPLICATE_VALUE = "DUPLICATE_VALUE"
        private const val INVALID_VALUE = "INVALID_VALUE"
    }

    override suspend fun getUserInfo(): UserInfoVO {
        return try {
            userInfoDataSource.getUserInfo().toEntity()
        } catch (e: HttpException) {
            handleApiException(e)
        }
    }

    override suspend fun updateUserInfo(userInfo: UpdateUserInfoRequestVO): UpdateUserInfoResponseVO {
        val userInfoDTO = UpdateUserInfoRequestDTO(
            nickname = userInfo.nickname,
            profileImage = userInfo.profileImage,
            introduction = userInfo.introduction,
            categoryIds = userInfo.categoryIds
        )

        return try {
            userInfoDataSource.updateUserInfo(
                params = userInfoDTO
            ).toEntity()
        } catch (e: HttpException) {
            handleApiException(e)
        }
    }

    private fun handleApiException(exception: HttpException): Nothing {
        val errorBody = exception.response()?.errorBody()?.string()
        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

        when (errorResponse.errorCode) {
            INVALID_TOKEN -> throw ApiException.InvalidTokenException() // 만료된 토큰
            DUPLICATE_VALUE -> throw ApiException.DuplicateValueException(errorResponse.message)
            INVALID_VALUE -> throw ApiException.InvalidNickNameValue(errorResponse.message) // 형식에 맞지 않는 닉네임
            else -> throw ApiException.UnknownApiException("An unknown error occurred: ${errorResponse.message}")
        }
    }

}