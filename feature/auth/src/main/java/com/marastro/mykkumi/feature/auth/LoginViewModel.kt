package com.marastro.mykkumi.feature.auth

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.marastro.mykkumi.domain.datastore.AuthTokenDataStore
import com.marastro.mykkumi.domain.entity.KakaoToken
import com.marastro.mykkumi.domain.entity.UserInfoVO
import com.marastro.mykkumi.domain.exception.ApiException
import com.marastro.mykkumi.domain.usecase.auth.GetUserInfoUseCase
import com.marastro.mykkumi.domain.usecase.auth.KakaoLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val kakaoLoginUseCase: KakaoLoginUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val authTokenDataStore: AuthTokenDataStore,
): ViewModel() {

    private val INVALID_TOKEN = "INVALID_TOKEN"
    private val MAX_RETRIES = 1
    private val KAKAO_INSTALL_BUT_NOT_ACCOUNT = "KakaoTalk is installed but not connected to Kakao account."

    private val _finishLoginUiState = MutableLiveData<Unit>()
    val finishLoginUiState: LiveData<Unit> get() = _finishLoginUiState

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.IDle)
    val loginUiState: StateFlow<LoginUiState> get() = _loginUiState

    lateinit var kakaoCallback: (OAuthToken?, Throwable?) -> Unit

    private val _userInfoUiState = MutableStateFlow<UserInfoVO>(UserInfoVO(null, null, null, null))
    val userInfoUiState: StateFlow<UserInfoVO> get() = _userInfoUiState

    private val _needKakaoAccount = MutableLiveData<Boolean>(false)
    val needKakaoAccount: LiveData<Boolean> get() = _needKakaoAccount


    fun kakaoLogin() {
        _loginUiState.value = LoginUiState.KakaoLogin
    }

    fun kakaoLoginSuccess() {
        _loginUiState.tryEmit(LoginUiState.KakaoLoginSuccess)
    }

    fun kakaoLoginFail() {
        _loginUiState.tryEmit(LoginUiState.KakaoLoginFail)
    }

    fun mykkumiLoginSuccess() {
        _loginUiState.tryEmit(LoginUiState.MykkumiLoginSuccess)
    }

    fun mykkumiLoginFail() {
        _loginUiState.tryEmit(LoginUiState.MykkumiLoginFail)
    }

    fun setDoneLoginProcess() {
        _loginUiState.tryEmit(LoginUiState.DoneProcess)
    }

    // 카카오 로그인 callback 세팅
    fun setKakaoCallback(showToast : (message: String) -> Unit) {
        kakaoCallback = { token, error ->
            if (error != null) { // 에러가 있는 경우
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        showToast("카카오 정보 제공 동의가 취소되었습니다.") // 접근이 거부 됨(동의 취소)
                    }

                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        showToast("유효하지 않은 앱입니다.") // 유효하지 않은 앱
                    }

                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        showToast("인증 수단이 유효하지 않아 인증할 수 없습니다.") // 인증 수단이 유효하지 않아 인증할 수 없는 상태
                    }

                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        showToast("서비스에 문제가 발생했습니다.") // 요청 파라미터 오류
                    }

                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        showToast("에러가 발생했습니다.") // 유효하지 않은 scope ID
                    }

                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        showToast("로그인 서비스에 문제가 발생했습니다") // 설정이 올바르지 않음(android key hash)
                    }

                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        showToast("카카오 서버 내부 에러가 발생했습니다.") // 서버 내부 에러
                    }

                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        showToast("요청 권한이 없는 앱입니다.") // 앱이 요청 권한이 없음
                    }

                    error.message == KAKAO_INSTALL_BUT_NOT_ACCOUNT -> {
                        // 카카오톡 앱이 설치되어 있지만, 로그인은 안 되어있는 경우
                        _needKakaoAccount.setValue(true)
                    }

                    else -> { // Unknown
                        showToast("서비스 에러가 발생했습니다") // 기타 에러
                    }
                }
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
                kakaoLoginFail()
            } else if (token != null) { // 토큰을 받아온 경우
//                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken} / ${token.refreshToken} / ${token.idToken}")
                kakaoLoginSuccess()
                setKakaoToken(token.accessToken, token.refreshToken)
            }
        }
    }

    fun doneKakaoAccount() {
        _needKakaoAccount.setValue(false)
    }

    // 카카오 로그인 token 값
    fun setKakaoToken(
        accessToken: String,
        refreshToken: String
    ) {
        viewModelScope.launch {
            try {
                val isSuccessLogin = kakaoLoginUseCase(
                    KakaoToken(
                        accessToken = accessToken,
                        refreshToken = refreshToken
                    )
                )

                if(isSuccessLogin) {
                    mykkumiLoginSuccess()
                }
                else {
                    mykkumiLoginFail()
                }
            }
            catch (e: Exception) {

            }
        }
    }

    // 다음 화면으로 네비게이션 처리
    fun navigateToNextScreen(navController: NavController, showToast : (message: String) -> Unit) {
        viewModelScope.launch {
            try {
                val userInfo = getUserInfoUseCase()
                _userInfoUiState.emit(userInfo)

                viewModelScope.launch {
                    userInfoUiState.collect { userInfo ->
                        setDoneLoginProcess()

                        // 최초 가입자 -> 추가 정보 입력 페이지로
                        if(userInfo.nickname == null) {
                            navController.navigate(route = LoginScreens.LoginSelectHobbyScreen.name)
                        }
                        // 기존 가입자 -> 홈 화면으로
                        else {
                            // TODO: uuid로 변경
                            // 닉네임 저장
                            authTokenDataStore.saveUserNickname(userInfo.nickname!!)

                            finishLogin()
                        }
                    }
                }
            }
            catch (e: ApiException.UnknownApiException) {
                showToast("서비스 오류가 발생했습니다.")
            }
            catch (e: Exception) {
                showToast("서비스 오류가 발생했습니다.")
            }
        }
    }

    // 로그인 종료
    fun finishLogin() {
        _finishLoginUiState.value = Unit
    }
}