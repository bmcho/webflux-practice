package com.bmcho.springwebflux.controller;

import com.bmcho.springwebflux.dto.PostCreateRequest;
import com.bmcho.springwebflux.dto.PostResponse;
import com.bmcho.springwebflux.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @PostMapping("")
    public Mono<PostResponse> createPost(@RequestBody PostCreateRequest request) {
        return postService.create(request.getUserId(), request.getTitle(), request.getContent())
                .map(PostResponse::of);
    }

    @GetMapping("")
    public Flux<PostResponse> findAllPost() {
        return postService.findAll()
                .map(PostResponse::of);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PostResponse>> findPost(@PathVariable Long id) {
        return postService.findById(id)
                .map(p -> ResponseEntity.ok().body(PostResponse.of(p)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<?>> deletePost(@PathVariable Long id) {
        return postService.deleteById(id).then(Mono.just(ResponseEntity.noContent().build()));
    }
}