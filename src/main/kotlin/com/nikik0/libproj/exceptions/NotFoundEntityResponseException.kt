package com.nikik0.libproj.exceptions

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

class NotFoundEntityResponseException(
    status: HttpStatusCode,
    body: ProblemDetail,
    cause: Throwable?,
    messageDetailCode: String?,
    messageDetailArguments: Array<out Any>?
): ErrorResponseException(status, body, cause, messageDetailCode, messageDetailArguments) {

    constructor(status: HttpStatusCode, missingMessage: String) : this(
        status,
        ProblemDetail.forStatus(status),
        NotFoundException(),
        missingMessage,
        null
    )
}