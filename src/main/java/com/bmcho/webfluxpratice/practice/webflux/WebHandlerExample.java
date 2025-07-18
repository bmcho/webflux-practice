package com.bmcho.webfluxpratice.practice.webflux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseCookie;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

import java.nio.charset.StandardCharsets;

@Slf4j
public class WebHandlerExample {

    private enum WebHandlerType {
        QUERY,
        FORM_DATA,
        MULTI_PART,
        MULTI_PART_FILE,
        JSON
    }

    @SneakyThrows
    public static void main(String[] args) {

        WebHandlerType type = WebHandlerType.QUERY;
        log.info("start");

        WebHandler webHandler = switch (type) {
            case QUERY -> webHandlerOnlyQueryParam;
            case FORM_DATA -> webHandlerOnlyFormData;
            case MULTI_PART -> webHandlerOnlyMultiPart;
            case MULTI_PART_FILE -> webHandlerOnlyMultiPartFile;
            case JSON -> webHandlerOnlyJson;
        };

        HttpHandler httpHandler = WebHttpHandlerBuilder
            .webHandler(webHandler)
            .build();

        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer.create()
            .host("localhost")
            .port(8080)
            .handle(adapter)
            .bindNow()
            .channel().closeFuture().sync();

    }

    private static Mono<Void> writeResponse(ServerHttpResponse response, String name) {
        String content = "Hello " + name;
        log.info("response body: {}", content);

        Mono<DataBuffer> responseBody = Mono.just(
            response.bufferFactory()
                .wrap(content.getBytes())
        );
        response.addCookie(ResponseCookie.from("name", name).build());
        response.getHeaders().add("Content-Type", "text/plain");

        return response.writeWith(responseBody);
    }

    private static final WebHandler webHandlerOnlyQueryParam = exchange -> {
        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();

        String query = request.getQueryParams().getFirst("name");
        String name = query == null ? "unknown" : query;
        return writeResponse(response, name);
    };

    private static final WebHandler webHandlerOnlyFormData = exchange -> {
        final ServerHttpResponse response = exchange.getResponse();

        return exchange.getFormData().flatMap(data -> {
            String query = data.getFirst("name");
            String name = query == null ? "unknown" : query;
            return writeResponse(response, name);
        });
    };

    private static final WebHandler webHandlerOnlyMultiPart = exchange -> {
        final ServerHttpResponse response = exchange.getResponse();

        return exchange.getMultipartData().flatMap(data -> {
            String query = ((FormFieldPart) data.getFirst("name")).value();
            String name = query == null ? "unknown" : query;
            return writeResponse(response, name);
        });
    };

    private static final WebHandler webHandlerOnlyMultiPartFile = exchange -> {
        var objectMapper = new ObjectMapper();
        final ServerHttpResponse response = exchange.getResponse();

        return exchange.getMultipartData().flatMap(data ->
                data.getFirst("name").content()
                    .map(dataBuffer -> dataBuffer.toString(StandardCharsets.UTF_8))
                    .reduce((s1, s2) -> s1 + s2))
            .flatMap(json -> {
                String name;
                try {
                    name = objectMapper.readTree(json).get("name").asText();
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                    name = "world";
                }
                return writeResponse(response, name);
            });
    };

    @Data
    private static class NameHolder {
        private String name;
    }

    private static final WebHandler webHandlerOnlyJson = exchange -> {
        final ServerRequest request = ServerRequest.create(
            exchange,
            ServerCodecConfigurer.create().getReaders()
        );
        final ServerHttpResponse response = exchange.getResponse();

        return request.bodyToMono(NameHolder.class)
            .flatMap(nameHolder -> {
                String nameQuery = nameHolder.name;
                String name = nameQuery == null ? "world" : nameQuery;

                return writeResponse(response, name);
            });
    };

}
