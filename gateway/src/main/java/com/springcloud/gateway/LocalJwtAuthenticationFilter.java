//package com.springcloud.gateway;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jws;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import javax.crypto.SecretKey;
//@Component
//@Slf4j
//public class LocalJwtAuthenticationFilter implements GlobalFilter {
//    @Value("${JWT_SECRET_KEY}")
//    private String secretKey;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        //로그인, 회원가입의 경우 토큰 검증 패스
//        String path = exchange.getRequest().getURI().getPath();
//        if(path.equals("/api/auth/login")) {}
//
//
//        String token = extractToken(exchange);
//        if(token == null || !validateToken(token)) {
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//        return chain.filter(exchange);
//    }
//
//    private String extractToken(ServerWebExchange exchange) {
//        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
//        if(authHeader != null && authHeader.startsWith("Bearer ")) {
//            return authHeader.substring(7);
//        }
//        return null;
//    }
//    // 토큰을 가져와서 해석하는 것
//    private boolean validateToken(String token) {
//        try {
//            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
//            Jws<Claims> claimsJws = Jwts.parser()
//                    .verifyWith(key)
//                    .build().parseSignedClaims(token);
//            log.info("#####payload :: " + claimsJws.getPayload().toString());
//
//            // 추가적인 검증 로직 (예: 토큰 만료 여부 확인 등)을 여기에 추가할 수 있습니다.
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}
