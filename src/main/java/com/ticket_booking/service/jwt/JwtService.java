package com.ticket_booking.service.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
public class JwtService implements IJwtService{
    
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;
    
    private String generateToken(UserDetails userDetails, Long expiration){
        log.info("Generating Token");
        Map<String, Object> claims = new HashMap<>();
        
        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    @Override 
    public String generateAccessToken(UserDetails userDetails){
        log.info("Start generate access token:");
        return generateToken(userDetails, expiration);
    }

    @Override 
    public String generateRefreshToken(UserDetails userDetails){
        log.info("Start generate refesh token:");
        return generateToken(userDetails, refreshExpiration);
    }

    @Override 
    public Long getExpirationTime(String token){
        Claims claims = extractAllClaims(token);
        return claims.getExpiration().getTime();
    }

    @Override 
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }


    // Kiểm tra token có hợp lệ không
    public boolean isTokenValid(String token, UserDetails userDetails){
        try{
            // email trong token phải = email trong db
            // token chưa hết hạn
            return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch(JwtException e){
            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        return claimsResolver.apply(extractAllClaims(token));
    }
    
    private Key getSignKey() {
        
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignKey())  // verify chữ ký
                .build()
                .parseSignedClaims(token)              // parse token
                .getPayload();                         // lấy data
    }

    // kiểm tra token hết hạn chưa
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

}
