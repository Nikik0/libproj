package com.nikik0.libproj.filter

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.*

@Component
class LogFilter: WebFilter {
    companion object {
        val logger: Log = LogFactory.getLog(LogFilter::class.java)
    }
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (!exchange.request.path.toString().contains("actuator")){
            val REQUEST_ID = "requestId"
            var requestId = exchange.request.headers[REQUEST_ID]?.get(0)
            if (requestId == null) {
                requestId = UUID.randomUUID().toString()
            }

            MDC.put(REQUEST_ID, requestId)

            try {
                logger.info("Started process request with $REQUEST_ID : $requestId, endpoint is ${exchange.request.path}")
            } finally {
                MDC.clear()
            }
        }
        return chain.filter(exchange)
    }
}