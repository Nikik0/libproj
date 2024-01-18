package com.nikik0.libproj.exceptions

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
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
        val error = ex.detailMessageArguments.asList().drop(1)[0] as List<*>
        println(ex.detailMessageArguments.asList().drop(1))
        val pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "error in arg + ${error}")
        println("handleWebExchangeBindException happened  ${ex.message}")
        return createResponseEntity(pb, headers, status, exchange)
//        return super.handleWebExchangeBindException(ex, headers, status, exchange)
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