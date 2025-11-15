package com.gtechsolutions.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Configuration
public class RouteConfig {

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10,20,1);
    }

    @Bean
    public KeyResolver hostNameKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest()
                                .getRemoteAddress())
                                .getAddress()
                                .getHostName()
        );
    }

    @Bean 
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f.circuitBreaker(c -> c
                                                .setName("ecommerce-service")
                                                .setFallbackUri("forward:/product-service/fallback"))
                                .requestRateLimiter(rateLimiterConfig -> rateLimiterConfig
                                                .setRateLimiter(redisRateLimiter())
                                                .setKeyResolver(hostNameKeyResolver()))
                                .retry(retryConfig -> retryConfig.setRetries(3)
                                        .setBackoff(Duration.ofMillis(500), Duration.ofSeconds(2),1, true)
                                        .allMethods())
                        )
                        .uri("lb://products-management-service"))
                .route("order-service", r -> r.path("/api/orders/**","/api/cart/**" ).uri("lb://orders-management-service"))
                .route("user-service", r -> r.path("/api/users/**").uri("lb://user-management-service"))
                .route("eureka-server", r -> r.path("/eureka/main")
                        .filters(f-> f.rewritePath("/eureka/main", "/"))
                        .uri("http://localhost:8761"))
                .route("eureka-server-static", r-> r.path("/eureka/**").uri("http://localhost:8761"))
                .build();
    }
}
