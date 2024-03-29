package com.nikik0.libproj.exceptions

import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

class AlreadyPresentResponseException(
    status: HttpStatusCode,
    body: ProblemDetail,
    cause: Throwable?,
    messageDetailCode: String?,
    messageDetailArguments: Array<out Any>?
):ErrorResponseException(status, body, cause, messageDetailCode, messageDetailArguments) {
    constructor(status: HttpStatusCode, message: String): this(
        status,
        ProblemDetail.forStatusAndDetail(status, message),
        AlreadyPresentException(),
        message,
        null
    )
}