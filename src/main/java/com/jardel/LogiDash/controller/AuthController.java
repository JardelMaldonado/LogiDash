package com.jardel.LogiDash.controller;

import com.jardel.LogiDash.dto.auth.LoginRequest;
import com.jardel.LogiDash.dto.auth.LoginResponse;
import com.jardel.LogiDash.dto.auth.LoginResponsePublico;
import com.jardel.LogiDash.service.AuthService;
import com.jardel.LogiDash.utils.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Value("${jwt.expiration}")
    private long expiration;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponsePublico> login(@Valid @RequestBody LoginRequest request,
                                                      HttpServletResponse response) {

        LoginResponse loginResponse = authService.login(request);

        var cookie = cookieUtil.criarTokenCookie(
                loginResponse.token(),
                expiration
        );

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(
                new LoginResponsePublico(
                        loginResponse.nome(),
                        loginResponse.email(),
                        loginResponse.role()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {

        var cookie = cookieUtil.limparTokenCookie();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }
}
