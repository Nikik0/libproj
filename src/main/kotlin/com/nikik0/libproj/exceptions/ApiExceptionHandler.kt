package com.nikik0.libproj.exceptions

import org.springframework.boot.context.properties.bind.validation.ValidationErrors
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.ErrorResponse
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.server.MethodNotAllowedException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestControllerAdvice
class ApiExceptionHandler: ResponseEntityExceptionHandler() {
    @Override
    override fun handleWebExchangeBindException(
        ex: WebExchangeBindException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        val smth =
            ex.fieldErrors.joinToString { "${it.field} has error ${it.defaultMessage} invalid value is ${it.rejectedValue}" }
        return createResponseEntity(smth, headers, status, exchange)
    }

    @Override
    override fun handleMethodNotAllowedException(
        ex: MethodNotAllowedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        println("handleMethodNotAllowedException")
        return super.handleMethodNotAllowedException(ex, headers, status, exchange)
    }

    @Override
    override fun handleErrorResponseException(
        ex: ErrorResponseException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        println("handleErrorResponseException")
        return super.handleErrorResponseException(ex, headers, status, exchange)
    }

    @Override
    override fun createResponseEntity(
        body: Any?,
        headers: HttpHeaders?,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
//        val problemDetail = body as ProblemDetail
        // todo should somehow catch the exception and pass it to user
        logger.error("error occurred $body")
        return super.createResponseEntity(body, headers, status, exchange)
    }
}