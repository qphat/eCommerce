package com.koomi.service;

import com.koomi.domain.USER_ROLE;
import com.koomi.request.LoginRequest;
import com.koomi.request.SignupRequest;
import com.koomi.response.AuthResponse;
import jakarta.mail.MessagingException;

public interface AuthService {
    String signup(SignupRequest signupRequest);

    AuthResponse signing(LoginRequest loginRequest);

    void sentLoginOtp(String email, USER_ROLE role) throws MessagingException;
}
