package com.marastro.mykkumi.feature.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.marastro.mykkumi.domain.datastore.AuthTokenDataStore
import com.marastro.mykkumi.domain.entity.BannerItemVO
import com.marastro.mykkumi.domain.entity.HomePostItemVO
import com.marastro.mykkumi.domain.entity.HomePostProductVO
import com.marastro.mykkumi.domain.exception.ApiException
import com.marastro.mykkumi.domain.usecase.banner.GetBannerListUseCase
import com.marastro.mykkumi.domain.usecase.post.GetHomePostListUseCase
import com.marastro.mykkumi.domain.usecase.report.ReportPostUseCase
import com.marastro.mykkumi.domain.usecase.report.ReportUserUseCase
import com.marastro.mykkumi.common_ui.post.ViewProductInfoBottomSheet
import com.marastro.mykkumi.domain.usecase.auth.GetUserInfoUseCase
import com.marastro.mykkumi.domain.usecase.post.DeletePostUseCase
import com.marastro.mykkumi.feature.home.report.ChooseReportBottomSheet
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
    private val deletePostUseCase: DeletePostUseCase,
    private val authTokenDataStore: AuthTokenDataStore,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val reportPostUseCase: ReportPostUseCase,
    private val reportUserUseCase: ReportUserUseCase,
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

    private var _isLogined = MutableStateFlow<Boolean>(false)
    val isLogined: StateFlow<Boolean> get() = _isLogined

    // TODO: 나증에 uuid로 바꿀 것
    private var _userNickname = MutableStateFlow<String>("")
    val userNickname: StateFlow<String> get() = _userNickname

    private var _isDeletePostDone = MutableLiveData<Boolean>(false)
    val isDeletePostDone: LiveData<Boolean> get() = _isDeletePostDone

    // 유저 정보 세팅
    fun initUserInfo() {
        //_userNickname.value = authTokenDataStore.getUserNickname() ?: ""
        viewModelScope.launch {
            try {
                val userInfo = getUserInfoUseCase()
                _userNickname.value = userInfo.nickname ?: ""
            }
            catch (e: ApiException.UnknownApiException) {
                //showToast("서비스 오류가 발생했습니다.")
            }
            catch (e: Exception) {
                //showToast("서비스 오류가 발생했습니다.")
            }
        }
    }

    // 홈 > 배너 캐러셀
    fun setHomeBanner() {
        viewModelScope.launch {
            try {
                val homeBanner = withContext(Dispatchers.IO) {
                    getBannerListUseCase()
                }

                // 로그인 유무
                _isLogined.emit(authTokenDataStore.isLogin())

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
                if(!isCursor) {
                    _postCursor.setValue(null)
                    _postListUiState.emit(mutableListOf())
                }

                val homePostList = getHomePostListUseCase(postCursor.value, postLimit.value)

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

    // 로그인 페이지 이동
    fun navigateLogin() : Intent? {
        if(authTokenDataStore.isLogin()) return null

        val loginDeepLink = "mykkumi://mykkumi.signin"

        val intent = Intent()
        intent.setAction(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(loginDeepLink))

        return intent
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

    // 포스트 신고
    fun reportPost(
        postId: Long,
        showToast: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val reportResult = reportPostUseCase(postId)
                showToast(reportResult.result)
            } catch (e: ApiException.DuplicateReportException) {
                e.message?.let { showToast(it) }
            } catch (e: ApiException.NotFoundException) {
                e.message?.let { showToast(it) }
            }
        }
    }


    // 유저 신고
    fun reportWriter(
        userUuid: String,
        showToast: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val reportResult = reportUserUseCase(userUuid)
                showToast(reportResult.result)
            } catch (e: ApiException.DuplicateReportException) {
                e.message?.let { showToast(it) }
            } catch (e: ApiException.NotFoundException) {
                e.message?.let { showToast(it) }
            }
        }
    }

    // 포스트 삭제
    fun deletePost(
        postId: Int,
        showToast: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                deletePostUseCase(postId)

                showToast("게시물이 삭제되었습니다")
                _isDeletePostDone.setValue(true)
            } catch (e: ApiException.AccessDeniedUserForPost) {
                e.message?.let { showToast(it) }
            } catch (e: ApiException.InvalidPostValue) {
                e.message?.let { showToast(it) }
            }
        }
    }

    fun doneResume() {
        _isDeletePostDone.setValue(false)
    }
}