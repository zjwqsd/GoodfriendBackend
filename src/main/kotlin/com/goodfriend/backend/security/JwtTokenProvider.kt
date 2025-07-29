package com.goodfriend.backend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey



@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}") private val jwtSecret: String,
    @Value("\${app.jwt.expiration}") private val jwtExpiration: Long = 86400000
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateToken(id: Long, role: Role): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)
        return Jwts.builder()
            .setSubject(id.toString())
            .claim("role", role.name)
            .claim("id", id)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun parseToken(token: String): Jws<Claims> =
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)

    fun getRoleFromToken(token: String): Role =
        Role.valueOf(parseToken(token).body["role"] as String)

    fun getIdFromToken(token: String): Long =
        (parseToken(token).body["id"] as Int).toLong()
}
