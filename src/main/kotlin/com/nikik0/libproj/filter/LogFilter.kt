package com.nikik0.libproj.filter

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.apache.commons.logging.LogFactory
import org.reactivestreams.Subscription
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.core.scheduler.Schedulers
import java.util.*

@Component
class LogFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        try {
            if (!exchange.request.path.toString().contains("actuator")) {
                val requestId = exchange.request.headers[REQUEST_ID]?.first()
                MDC.put(REQUEST_ID, requestId ?: UUID.randomUUID().toString())
                logger.info("Started processing request")
            }
            return chain.filter(exchange)
        }finally {
        }

    }
//    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
//        if (!exchange.request.path.toString().contains("actuator")){
//            var requestId = exchange.request.headers[REQUEST_ID]?.get(0)
//            if (requestId == null) {
//                requestId = UUID.randomUUID().toString()
//            }
//            return chain.filter(exchange)
//                .doOnSubscribe { subscription: Subscription? ->
//                    MDC.put(REQUEST_ID, requestId)
//                    logger.info("requestId has been set: $requestId")
//                }
//
//                .doFinally { whatever: SignalType? ->
//                    MDC.remove(REQUEST_ID)
//                    logger.info("requestId has been removed: $requestId")
//                }
//        }else
//            return chain.filter(exchange)
//    }
//
//    @PostConstruct
//    fun setupReactorThreadsDecorator() {
//        Schedulers.onScheduleHook("mdc") { runnable: Runnable ->
//            val mdc = MDC.getCopyOfContextMap() // can be narrowed down to RID only if necessary
//            Runnable {
//                MDC.setContextMap(mdc)
//                try {
//                    runnable.run()
//                } finally {
//                    MDC.clear()
//                }
//            }
//        }
//    }
//
//    @PreDestroy
//    fun shutdownThreadsDecorator() {
//        Schedulers.resetOnScheduleHook("mdc")
//    }

    companion object {
        const val REQUEST_ID = "requestId"
        private val logger = LogFactory.getLog(LogFilter::class.java)
    }
}