package br.com.hyperativa.service.application.config.security.jwt;

import br.com.hyperativa.service.application.config.JwtPropertyConfig;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    public static final String JWT_ALGORITHM = "HS512";

    private final JwtPropertyConfig jwtPropertyConfig;

    JwtUtil(JwtPropertyConfig jwtPropertyConfig) {
        this.jwtPropertyConfig = jwtPropertyConfig;
    }

    public String generateToken(final String username) {
        return Jwts.builder()
                .setIssuer("Hyperativa")
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtPropertyConfig.getExpiration()))
                .signWith(SignatureAlgorithm.HS512, jwtPropertyConfig.getSecret())
                .compact();
    }

    public String getUsernameFromToken(final String token) {
        return Jwts.parser()
                .setSigningKey(jwtPropertyConfig.getSecret())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(final String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtPropertyConfig.getSecret()).build().parseClaimsJws(authToken);
            return true;
        } catch (final JwtException e) {
            throw new JwtException("Error on validating JWT token: ", e);
        }
    }
}
