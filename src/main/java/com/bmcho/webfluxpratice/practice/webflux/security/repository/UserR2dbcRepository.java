package com.bmcho.webfluxpratice.practice.webflux.security.repository;


import com.bmcho.webfluxpratice.practice.webflux.security.common.repository.UserEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface UserR2dbcRepository
        extends R2dbcRepository<UserEntity, Long> {
}
