package com.bmcho.webfluxpratice.practice.webflux;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Slf4j
public class RouterFunctionExample {

    public static void main(String[] args) throws InterruptedException {
        log.info("start");

        RouterFunction<ServerResponse> router = RouterFunctions.route()
            .path("/greet", b1 -> b1
                .nest(accept(MediaType.TEXT_PLAIN), b2 -> b2
                    .GET("/",
                        queryParam("name", name -> !name.isBlank()),
                        GreetingHandler::greetQueryParam)
                    .GET("/name/{name}", GreetingHandler::greetPathVariable)
                    .GET("/header",
                        headers(h -> {
                            h.firstHeader("X-Custom-Name");
                            return true;
                        }),
                        GreetingHandler::greetHeader)
                    .POST("/json", contentType(MediaType.APPLICATION_JSON),
                        GreetingHandler::greetJsonBody)
                    .POST("/text", GreetingHandler::greetPlainTextBody)
                )
            )
            .build();

        var httpHandler = RouterFunctions.toHttpHandler(router);
        var adapter = new ReactorHttpHandlerAdapter(httpHandler);

        HttpServer.create()
            .host("localhost")
            .port(8080)
            .handle(adapter)
            .bindNow()
            .channel().closeFuture().sync();
    }


    @Slf4j
    public static class GreetingHandler {
        public static Mono<ServerResponse> greetQueryParam(ServerRequest serverRequest) {
            String name = serverRequest.queryParam("name")
                .orElse("world");

            String content = "Hello " + name;
            return ServerResponse.ok().bodyValue(content);
        }

        public static Mono<ServerResponse> greetPathVariable(ServerRequest serverRequest) {
            String name = serverRequest.pathVariable("name");

            String content = "Hello " + name;
            return ServerResponse.ok().bodyValue(content);
        }

        public static Mono<ServerResponse> greetJsonBody(ServerRequest serverRequest) {
            return serverRequest.bodyToMono(NameHolder.class)
                .map(NameHolder::getName)
                .map(name -> "Hello " + name)
                .flatMap(content -> ServerResponse.ok().bodyValue(content))
                .doOnError(throwable -> log.error("error", throwable));
        }

        public static Mono<ServerResponse> greetPlainTextBody(ServerRequest serverRequest) {
            return serverRequest.bodyToMono(String.class)
                .map(name -> "Hello " + name)
                .flatMap(content -> ServerResponse.ok().bodyValue(content));
        }

        public static Mono<ServerResponse> greetHeader(ServerRequest serverRequest) {
            String name = serverRequest.headers().header("X-Custom-Name")
                .stream()
                .findFirst()
                .orElse("world");

            String content = "Hello " + name;
            return ServerResponse.ok().bodyValue(content);
        }
    }


    @Setter
    @Getter
    public static class NameHolder {
        private String name = "";
    }

}
