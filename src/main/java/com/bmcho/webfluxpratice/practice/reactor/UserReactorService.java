package com.bmcho.webfluxpratice.practice.reactor;

import com.bmcho.webfluxpratice.practice.reactor.domain.Article;
import com.bmcho.webfluxpratice.practice.reactor.domain.EmptyImage;
import com.bmcho.webfluxpratice.practice.reactor.domain.Image;
import com.bmcho.webfluxpratice.practice.reactor.domain.User;
import com.bmcho.webfluxpratice.practice.reactor.entity.UserEntity;
import com.bmcho.webfluxpratice.practice.reactor.repository.ArticleReactorRepository;
import com.bmcho.webfluxpratice.practice.reactor.repository.FollowReactorRepository;
import com.bmcho.webfluxpratice.practice.reactor.repository.ImageReactorRepository;
import com.bmcho.webfluxpratice.practice.reactor.repository.UserReactorRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserReactorService {

    private final UserReactorRepository userRepository;
    private final ArticleReactorRepository articleRepository;
    private final ImageReactorRepository imageRepository;
    private final FollowReactorRepository followRepository;


    @SneakyThrows
    public Mono<User> getUserById(String id) {
        return userRepository.findById(id)
            .flatMap(this::getUser);
    }

    @SneakyThrows
    private Mono<User> getUser(UserEntity userEntity) {
        Context context = Context.of("user", userEntity);

        var imageMono = imageRepository.findWithContext()
            .map(imageEntity ->
                new Image(imageEntity.getId(), imageEntity.getName(), imageEntity.getUrl())
            ).onErrorReturn(new EmptyImage())
            .contextWrite(context);

        var articlesMono = articleRepository.findAllWithContext()
            .skip(5)
            .take(2)
            .map(articleEntity ->
                new Article(articleEntity.getId(), articleEntity.getTitle(), articleEntity.getContent())
            ).collectList()
            .contextWrite(context);

        var followCountMono = followRepository.countWithContext()
            .contextWrite(context);

        return Mono.zip(imageMono, articlesMono, followCountMono)
            .map(resultTuple -> {
                Image image = resultTuple.getT1();
                List<Article> articles = resultTuple.getT2();
                Long followCount = resultTuple.getT3();

                Optional<Image> imageOptional = Optional.empty();
                if (!(image instanceof EmptyImage)) {
                    imageOptional = Optional.of(image);
                }

                return new User(
                    userEntity.getId(),
                    userEntity.getName(),
                    userEntity.getAge(),
                    imageOptional,
                    articles,
                    followCount
                );
            });
    }
}
