package com.koomi.service;

import com.koomi.entity.User;

public interface UserService {
    User findUserByJwtToken(String jwt);
    User findUserByEmail(String email);
}
