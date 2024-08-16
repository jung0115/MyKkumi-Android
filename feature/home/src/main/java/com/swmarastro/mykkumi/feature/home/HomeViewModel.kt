package com.swmarastro.mykkumi.feature.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.swmarastro.mykkumi.domain.datastore.AuthTokenDataStore
import com.swmarastro.mykkumi.domain.entity.BannerItemVO
import com.swmarastro.mykkumi.domain.entity.BannerListVO
import com.swmarastro.mykkumi.domain.entity.HomePostItemVO
import com.swmarastro.mykkumi.domain.usecase.banner.GetBannerListUseCase
import com.swmarastro.mykkumi.domain.usecase.post.GetHomePostListUseCase
import com.swmarastro.mykkumi.feature.home.post.PostListAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getBannerListUseCase: GetBannerListUseCase,
    private val getHomePostListUseCase: GetHomePostListUseCase,
    private val authTokenDataStore: AuthTokenDataStore,
) : ViewModel() {

    // 홈 > 배너 캐러셀
    private val _bannerListUiState = MutableStateFlow<MutableList<BannerItemVO>>(mutableListOf())
    val bannerListUiState: StateFlow<MutableList<BannerItemVO>> get() = _bannerListUiState

    // 포스트 리스트
    private val _postListUiState = MutableStateFlow<MutableList<HomePostItemVO>>(mutableListOf())
    val postListUiState: StateFlow<MutableList<HomePostItemVO>> get() = _postListUiState

    // 선택된 배너
    private val _selectBannerId = MutableLiveData<Int>()
    val selectBannerId: LiveData<Int> get() = _selectBannerId

    // 포스트 리스트 cursor, limit
    private val _postLimit = MutableStateFlow<Int>(5)
    val postLimit: StateFlow<Int> get() = _postLimit
    private var _postCursor = MutableLiveData<String?>(null)
    val postCursor: LiveData<String?> get() = _postCursor

    private var _isPostListLoading = MutableStateFlow<Boolean>(false)
    val isPostListLoading: StateFlow<Boolean> get() = _isPostListLoading

    // 홈 > 배너 캐러셀
    fun setHomeBanner() {
        viewModelScope.launch {
            try {
                val homeBanner = withContext(Dispatchers.IO) {
                    getBannerListUseCase()
                }
                val bannerList = homeBanner.banners.toMutableList()
                bannerList.add(BannerItemVO())
                _bannerListUiState.emit( bannerList )
            } catch (e: Exception) {
                _bannerListUiState.emit( mutableListOf() )
            }
        }
    }

    // 홈 > 배너 캐러셀에서 배너 선택
    fun selectHomeBanner(bannerId: Int) {
        _selectBannerId.value = bannerId
    }

    // 포스트 리스트
    // isCursor의 역할: 처음으로 데이터를 조회해오는 것인지, cursor가 있는 상태로 다음 데이터를 불러오는 것인지
    fun setPostList(isCursor: Boolean) {
        viewModelScope.launch {
            try {
                _isPostListLoading.emit(true) // 스크롤 이벤트가 연속적으로 호출되는 것을 방지
                if(!isCursor) _postCursor.setValue(null)

                val homePostList = withContext(Dispatchers.IO) {
                    getHomePostListUseCase(postCursor.value, postLimit.value)
                }

                if (isCursor) _postListUiState.value.addAll(homePostList.posts)
                else _postListUiState.emit(homePostList.posts.toMutableList())

                // 다음 커서
                _postCursor.setValue( homePostList.cursor )

                _isPostListLoading.emit(false) // 스크롤 이벤트가 연속적으로 호출되는 것을 방지
            } catch (e: Exception) {
                _postListUiState.emit(mutableListOf())
            }
        }
    }

    // 배너 전체 리스트 페이지로 이동
    fun navigateBannerAll(navController: NavController?) {
        navController?.navigate(R.id.action_navigate_fragment_to_home_banner_all)
    }

    // 배너 상세 페이지로 이동
    fun navigateBannerDetail(navController: NavController?) {
        val navigateDeepLink = "mykkumi://banner.detail?bannerId=${selectBannerId.value}"
        //val action = HomeFragmentDirections.actionNavigateFragmentToHomeBannerDetail(bannerId = selectBannerId)
        navController?.navigate(deepLink = navigateDeepLink.toUri())
    }

    // 로그아웃 테스트
    fun logout() {
        authTokenDataStore.deleteToken()
    }
}