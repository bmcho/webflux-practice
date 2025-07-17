package com.bmcho.webfluxpratice.basic.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<User> save(User user);

    Flux<User> findAll();

    Mono<User> findById(Long Iid);

    Mono<Integer> deleteById(Long id);

}
