package com.pyd.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "pyd.jwt")
public class JwtUtils {
    private String secret;
    private long expire;
    private String header;
    //生成jwttoken
    public String generateToken(Long userId){
        Date nowdate = new Date();
        Date expireDate =new Date(nowdate.getTime() + expire * 1000);
        return Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setSubject(userId+"")
                .setIssuedAt(nowdate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    //获取jwt信息
    public Claims getClaimByToken(String token){
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (Exception e){
            log.debug("validate is token error ", e);
            return null;
        }
    }
    //检查token是否过期
    public boolean isTokenExpired(Date expiration){return expiration.before(new Date());}
}
