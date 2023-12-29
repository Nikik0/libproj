package com.nikik0.libproj.exceptions

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestControllerAdvice
class ApiExceptionHandler: ResponseEntityExceptionHandler() {
    @Override
    override fun createResponseEntity(
        body: Any?,
        headers: HttpHeaders?,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
//        val problemDetail = body as ProblemDetail
        // todo should somehow catch the exception and pass it to user
        logger.error("error occurred")
        return super.createResponseEntity(body, headers, status, exchange)
    }
}