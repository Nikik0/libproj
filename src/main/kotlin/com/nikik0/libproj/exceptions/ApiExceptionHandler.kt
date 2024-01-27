package com.nikik0.libproj.exceptions

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
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
    ): Mono<ResponseEntity<Any>> = createResponseEntity(
        ValidationErrorBody(
            "Error validating supplied entity",
            ex.fieldErrors.map { it.defaultMessage }
        ),
        headers,
        status,
        exchange
    )


    @Override
    override fun createResponseEntity(
        body: Any?,
        headers: HttpHeaders?,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        logger.error("Error occurred $body")
        return super.createResponseEntity(body, headers, status, exchange)
    }
}