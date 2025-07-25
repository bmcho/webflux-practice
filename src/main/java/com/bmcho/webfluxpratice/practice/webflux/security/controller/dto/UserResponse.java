package com.bmcho.webfluxpratice.practice.webflux.security.controller.dto;

import lombok.Data;

import java.util.Optional;

@Data
public class UserResponse {
    private final String id;
    private final String name;
    private final int age;
    private final Long followCount;
    private final Optional<ProfileImageResponse> image;
}