package com.actisys.apigateway.filter;

import com.actisys.apigateway.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

  private final JwtTokenProvider tokenProvider;

  public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    log.debug("Processing request: {}", path);

    if (isOAuthFlow(path) || isPublicEndpoint(path)) {
      log.debug("Public endpoint, skipping JWT validation: {}", path);
      return chain.filter(exchange);
    }

    String token = extractToken(exchange);

    if (token == null) {
      log.warn("Missing JWT token for protected endpoint: {}", path);
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    try {
      Jws<Claims> jws = tokenProvider.verify(token);
      Claims claims = jws.getPayload();

      String username = extractUsername(claims);
      String userId = extractUserId(claims);

      log.info("Authenticated user: {} (ID: {})", username, userId);

      ServerHttpRequest mutatedRequest = request.mutate()
              .header("X-Username", username)
              .header("X-User-Id", userId)
              .header("Authorization", "Bearer " + token)
              .build();

      return chain.filter(exchange.mutate().request(mutatedRequest).build());

    } catch (JwtException ex) {
      log.warn("JWT validation failed for path {}: {}", path, ex.getMessage());
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }
  }

  private String extractUserId(Claims claims) {
    String userId = claims.get("userId", String.class);
    if (userId == null) {
      userId = claims.get("sub", String.class); // subject
    }
    if (userId == null) {
      userId = claims.getSubject();
    }
    return userId;
  }

  private String extractTokenFromCookie(ServerWebExchange exchange) {
    HttpCookie cookie = exchange.getRequest().getCookies().getFirst("auth_token");
    if (cookie != null) {
      log.debug("Found auth_token cookie");
      return cookie.getValue();
    }
    log.debug("No auth_token cookie found");
    return null;
  }

  private String extractToken(ServerWebExchange exchange) {
    HttpCookie cookie = exchange.getRequest().getCookies().getFirst("auth_token");
    if (cookie != null) {
      log.debug("Found auth_token cookie");
      return cookie.getValue();
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      log.debug("Found Authorization header");
      return authHeader.substring(7);
    }

    log.debug("No token found");
    return null;
  }

  private String extractUsername(Claims claims) {
    String username = claims.get("username", String.class);
    if (username == null) {
      username = claims.get("uname", String.class);
    }
    if (username == null) {
      username = claims.getSubject();
    }
    return username;
  }

  private boolean isOAuthFlow(String path) {
    return path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/");
  }

  private boolean isPublicEndpoint(String path) {
    return path.startsWith("/api/v1/auth/login") ||
            path.startsWith("/api/v1/auth/register") ||
            path.startsWith("/api/v1/menu/getAll") ||
            path.startsWith("/api/v1/categories/getAll") ||
            path.startsWith("/api/v1/recipes/getRecipeByMenuId") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/webjars") ||
            path.contains("/v3/api-docs");
  }

  @Override
  public int getOrder() {
    return -100;
  }
}