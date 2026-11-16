package com.ticket_booking.utils.ratelimit;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ticket_booking.enums.RateLimitPolicy;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor{

    private static String API_PREFIX = "/api/v1";
    
    @Autowired
    private ProxyManager<byte[]> proxyManager;

    @Autowired
    private RateLimitProperties rateLimitProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        
        String policyName = resolvepolicyName(request.getRequestURI());
        
        RateLimitProperties.Rule rule = rateLimitProperties.getRule().get(policyName);
        String clientKey = resolveClientKey(request);

        byte[] bucketKey = (policyName + ":" + clientKey).getBytes(StandardCharsets.UTF_8);

        BucketConfiguration bucketConfiguration = buildConfiguration(rule);

        var probe = proxyManager.builder()
                                .build(bucketKey, ()-> bucketConfiguration)
                                .tryConsumeAndReturnRemaining(1);

        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            return true;
        }

        long retryAfterSeconds = Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds() + 1 ;
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\":\"Too many requests\",\"retryAfterSeconds\":" + retryAfterSeconds + "}");

        return false;
    }

    private BucketConfiguration buildConfiguration(RateLimitProperties.Rule rule){
        Bandwidth limit = Bandwidth.builder()
                                    .capacity(rule.getCapacity())
                                    .refillGreedy(rule.getRefillToken(), Duration.ofSeconds(rule.getRefillPeriodSeconds()))
                                    .build();

        return BucketConfiguration.builder().addLimit(limit).build();
    }

    private String resolvepolicyName(String path){
        
        if(path.startsWith(API_PREFIX + "/auth/login")){
            return RateLimitPolicy.LOGIN.getName();
        }

        if(path.startsWith(API_PREFIX + "/booking") || path.startsWith(API_PREFIX + "/payment")){
            return RateLimitPolicy.BOOKING.getName();
        }
        
        return RateLimitPolicy.DEFAULT.getName();
    }

    private String resolveClientKey(HttpServletRequest request){
        
        String userId =  Optional.ofNullable(request.getUserPrincipal()).map(Principal::getName)
                            .orElse(null);
        return userId != null ? "user: " + userId : "ip: " + request.getRemoteAddr();
    }
}
