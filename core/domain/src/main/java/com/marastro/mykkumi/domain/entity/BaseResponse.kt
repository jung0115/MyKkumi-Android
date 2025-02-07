package com.marastro.mykkumi.domain.entity

abstract class BaseResponse <T : Any> {
    lateinit var statusCode: String
    lateinit var responseMessage: String
    var data: T? = null
}