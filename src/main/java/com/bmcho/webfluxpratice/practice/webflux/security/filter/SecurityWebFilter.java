package com.bmcho.webfluxpratice.practice.webflux.security.filter;

import com.bmcho.webfluxpratice.practice.webflux.security.auth.IamAuthentication;
import com.bmcho.webfluxpratice.practice.webflux.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@Component
public class SecurityWebFilter implements WebFilter {
    private final AuthService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final ServerHttpResponse resp = exchange.getResponse();
        String iam = exchange.getRequest().getHeaders()
            .getFirst("X-I-AM");

        if (exchange.getRequest().getURI().getPath().equals("/api/users/signup")) {
            return chain.filter(exchange);
        }

        if (iam == null) {
            resp.setStatusCode(HttpStatus.UNAUTHORIZED);
            return resp.setComplete();
        }

        return authService.getNameByToken(iam)
            .flatMap(name -> chain.filter(exchange)
                .contextWrite(context -> context.putAll(ReactiveSecurityContextHolder.withAuthentication(new IamAuthentication(name)))
                ))
            .switchIfEmpty(Mono.defer(() -> {
                resp.setStatusCode(HttpStatus.UNAUTHORIZED);
                return resp.setComplete();
            }));

    }
}
