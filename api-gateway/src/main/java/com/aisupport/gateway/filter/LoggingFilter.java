package com.aisupport.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.aisupport.common.constant.HttpHeaders;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		
		String correlationId = exchange.getRequest().getHeaders()
                .getFirst(HttpHeaders.CORRELATION_ID); // read from CorrelationIdFilter

		// String requestId = UUID.randomUUID().toString().substring(0, 8); // commented as we used CorrelationId
		String method    = exchange.getRequest().getMethod().name();
        String path      = exchange.getRequest().getURI().getPath();
        long startTime = System.currentTimeMillis();
        
        log.info("[{}] --> {} {}", correlationId, method, path);

        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                long     duration   = System.currentTimeMillis() - startTime;
                HttpStatusCode status   = exchange.getResponse().getStatusCode();
                log.info("[{}] <-- {} {} | status={} | {}ms",
                		correlationId, method, path, status, duration);
            }));
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 2; // runs after CorrelationIdFilter (+1)
	}
}
