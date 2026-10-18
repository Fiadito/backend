package pe.edu.upc.fiadito.platform.iam.application.internal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.User;
import pe.edu.upc.fiadito.platform.iam.domain.model.valueobjects.Role;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Genera y valida el token JWT firmado (HS256) con id_usuario, rol, id_negocio e id_socio.
 * Vigencia: app.jwt.expiration (8 horas por defecto).
 */
@Component
public class JwtService {

    private final Key key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("uid", user.getId())
                .claim("rol", user.getRole().name())
                .claim("idNegocio", user.getBusinessId())
                .claim("idSocio", user.getMemberId())
                .claim("nombre", user.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public CurrentUser parse(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        var memberId = claims.get("idSocio", Number.class);
        return new CurrentUser(
                claims.get("uid", Number.class).longValue(),
                claims.get("idNegocio", Number.class).longValue(),
                memberId == null ? null : memberId.longValue(),
                Role.valueOf(claims.get("rol", String.class)),
                claims.get("nombre", String.class),
                claims.getSubject());
    }
}
