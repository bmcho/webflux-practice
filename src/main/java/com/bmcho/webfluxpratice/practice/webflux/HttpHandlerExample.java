package com.bmcho.webfluxpratice.practice.webflux;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;


@Log4j2
public class HttpHandlerExample {

    public static void main(String[] args) throws InterruptedException {
        log.info("start http handler");

        HttpHandler httpHandler = new HttpHandler() {
            @Override
            public Mono<Void> handle(ServerHttpRequest req, ServerHttpResponse res) {
                String query = req.getQueryParams().getFirst("name");
                String name = query == null ? "UnKnown" : query;

                String content = "Hello " + name;
                log.info("response body: {}", content);

                Mono<DataBuffer> responseBody = Mono.just(
                    res.bufferFactory()
                        .wrap(content.getBytes())
                );

                res.getHeaders().add("Content-Type", "text/plain");
                return res.writeWith(responseBody);
            }
        };

        var adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer.create()
            .host("localhost")
            .port(8080)
            .handle(adapter)
            .bindNow()
            .channel().closeFuture().sync();

    }

}
