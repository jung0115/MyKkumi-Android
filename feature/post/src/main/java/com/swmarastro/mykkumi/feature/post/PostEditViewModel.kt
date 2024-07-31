package com.swmarastro.mykkumi.feature.post

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PostEditViewModel  @Inject constructor(
) : ViewModel() {
    private final val MAX_IMAGE_COUNT = 10

    private val _postEditUiState = MutableLiveData<MutableList<Uri>>(mutableListOf())
    val postEditUiState: LiveData<MutableList<Uri>> get() = _postEditUiState

    // 카메라로 촬영할 이미지를 저장할 path
    private val _cameraImagePath = MutableStateFlow<Uri?>(null)
    val cameraImagePath : StateFlow<Uri?> get() = _cameraImagePath

    fun selectPostImage(uri: Uri) {
        val addPostImages = _postEditUiState.value
        addPostImages?.add(uri)
        _postEditUiState.value = addPostImages
        resetCameraImagePath()
    }

    // 카메라로 촬영한 이미지가 저장될 경로
    fun setCameraImagePath(path: Uri) {
        _cameraImagePath.value = path
    }

    // 경로 사용하면 리셋
    fun resetCameraImagePath() {
        _cameraImagePath.value = null
    }
}