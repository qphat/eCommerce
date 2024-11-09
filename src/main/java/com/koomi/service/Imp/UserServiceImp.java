package com.koomi.service.Imp;

import com.koomi.config.JwtTokenProvider;
import com.koomi.entity.User;
import com.koomi.repository.UserRepository;
import com.koomi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public User findUserByJwtToken(String jwt) {
        String email = jwtTokenProvider.getEmailFromToken(jwt);

        User user = userRepository.findByEmail(email);
        if(user == null) {
            throw new RuntimeException("User not found" + email);
        }

        return user;
    }

    @Override
    public User findUserByEmail(String email) {

        User user = userRepository.findByEmail(email);
        if(user == null) {
            throw new RuntimeException("User not found" + email);
        }

        return user;
    }
}
