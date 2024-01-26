package com.nikik0.libproj.filter

import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class LogFilter: OncePerRequestFilter() {

    override fun doFilterInternal(
        request: jakarta.servlet.http.HttpServletRequest,
        response: jakarta.servlet.http.HttpServletResponse,
        filterChain: jakarta.servlet.FilterChain
    ) {
        //todo this doesnt filter out the actuator. need investigate
        if (!request.contextPath.contains("actuator")){
            val REQUEST_ID = "requestId"
            var requestId = request.getHeader(REQUEST_ID)
            if (requestId == null) {
                requestId = UUID.randomUUID().toString()
            }

            MDC.put(REQUEST_ID, requestId)

            try {
                logger.info("Started process request with $REQUEST_ID : $requestId")
                filterChain.doFilter(request, response)
            } finally {
                MDC.clear()
            }
        }
    }

    override fun shouldNotFilterAsyncDispatch(): Boolean {
        return false
    }

    override fun shouldNotFilterErrorDispatch(): Boolean {
        return false
    }
}