package com.bmcho.springwebflux.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class BasicController {

    @GetMapping("/hello-controller")
    public Mono<String> getHello(){
        return Mono.just("hello from rest controller");
    }

}
