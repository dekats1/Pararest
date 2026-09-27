package com.actisys.authservice.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class GitHubOAuth2User implements OAuth2User {

    private final OAuth2User delegate;

    public GitHubOAuth2User(OAuth2User delegate) {
        this.delegate = delegate;
    }

    public String getProviderId() {
        Object id = delegate.getAttributes().get("id");
        return id != null ? String.valueOf(id) : null;
    }

    public String getGithubLogin() {
        return (String) delegate.getAttributes().get("login");
    }

    public String getDisplayName() {
        return (String) delegate.getAttributes().get("name");
    }

    public String getEmail() {
        return (String) delegate.getAttributes().get("email");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}