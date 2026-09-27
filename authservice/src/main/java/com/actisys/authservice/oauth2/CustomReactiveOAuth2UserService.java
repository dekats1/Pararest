package com.actisys.authservice.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomReactiveOAuth2UserService implements ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultReactiveOAuth2UserService delegate = new DefaultReactiveOAuth2UserService();
    private final OAuth2GithubIntegrationService githubIntegrationService;

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) {
        return delegate.loadUser(userRequest)
                .map(GitHubOAuth2User::new)
                .flatMap(githubUser -> Mono.fromCallable(() -> {
                            githubIntegrationService.ensureGithubAccountLinked(githubUser);
                            return (OAuth2User) githubUser;
                        })
                        .subscribeOn(Schedulers.boundedElastic()));
    }
}
