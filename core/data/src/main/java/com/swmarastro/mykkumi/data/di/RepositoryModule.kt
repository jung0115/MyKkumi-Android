package com.swmarastro.mykkumi.data.di

import android.content.Context
import com.swmarastro.mykkumi.data.repository.BannerRepositoryImpl
import com.swmarastro.mykkumi.data.repository.KakaoLoginRepositoryImpl
import com.swmarastro.mykkumi.data.repository.PostRepositoryImpl
import com.swmarastro.mykkumi.data.repository.ReAccessTokenRepositoryImpl
import com.swmarastro.mykkumi.data.repository.UserInfoRepositoryImpl
import com.swmarastro.mykkumi.domain.repository.BannerRepository
import com.swmarastro.mykkumi.domain.repository.KakaoLoginRepository
import com.swmarastro.mykkumi.domain.repository.PostRepository
import com.swmarastro.mykkumi.domain.repository.ReAccessTokenRepository
import com.swmarastro.mykkumi.domain.repository.UserInfoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindsBannerRepository(
        bannerRepositoryImpl: BannerRepositoryImpl
    ): BannerRepository

    @Binds
    @Singleton
    abstract fun bindsPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindsKakaoLoginRepository(
        kakaoLoginRepositoryImpl: KakaoLoginRepositoryImpl
    ): KakaoLoginRepository


    @Binds
    @Singleton
    abstract fun bindsUserInfoRepository(
//        @ApplicationContext context: Context,
        userInfoRepositoryImpl: UserInfoRepositoryImpl
    ): UserInfoRepository

    @Binds
    @Singleton
    abstract fun bindsReAccessTokenRepository(
        reAccessTokenRepositoryImpl: ReAccessTokenRepositoryImpl
    ): ReAccessTokenRepository
}