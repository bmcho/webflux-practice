package com.bmcho.webfluxpratice.practice.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Slf4j
public class RouterFunctionExample {

    public static void main(String[] args) {
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

    }

}
