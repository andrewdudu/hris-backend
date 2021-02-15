package com.bliblifuture.hrisbackend.config;

import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.util.DateUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenUtil implements Serializable {

    public static final long JWT_TOKEN_VALIDITY = 1000L*60*60*1000;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key key;

    @Autowired
    private DateUtil dateUtil;

    private static Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
  
    public String getUsernameFromToken(String token){
        try {
            return getClaimFromToken(token, Claims::getSubject);
        }
        catch (ExpiredJwtException e){
            return e.getClaims().getSubject();
        }
    }

    private  <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver){
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);

    }

    public Claims getAllClaimsFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token){
        try{
            return getExpirationDateFromToken(token).before(dateUtil.getNewDate());
        }
        catch (ExpiredJwtException e){
            return e.getClaims().getExpiration().before(dateUtil.getNewDate());
        }
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public String generateToken(User user){
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, user.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject){
        long currentTimeMillis = new Date().getTime();
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(currentTimeMillis + JWT_TOKEN_VALIDITY))
                .signWith(key).compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
