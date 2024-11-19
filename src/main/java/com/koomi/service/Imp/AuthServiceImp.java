package com.koomi.service.Imp;

import com.koomi.config.JwtTokenProvider;
import com.koomi.domain.USER_ROLE;
import com.koomi.entity.Cart;
import com.koomi.entity.Seller;
import com.koomi.entity.User;
import com.koomi.entity.VerificationCode;
import com.koomi.repository.CartRepository;
import com.koomi.repository.SellerRepository;
import com.koomi.repository.UserRepository;
import com.koomi.repository.VerificationCodeRepository;
import com.koomi.request.LoginRequest;
import com.koomi.request.SignupRequest;
import com.koomi.response.AuthResponse;
import com.koomi.service.AuthService;
import com.koomi.service.EmailService;
import com.koomi.utils.OtpUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final CustomerServiceImp customerServiceImp;
    private final SellerRepository sellerRepository;

    @Override
    public void sentLoginOtp(String email, USER_ROLE role) throws MessagingException {
        String SIGN_PREFIX = "signing_";

        if(email.startsWith(SIGN_PREFIX)) {
            email = email.substring(SIGN_PREFIX.length());

            if(role.equals(USER_ROLE.SELLER)) {
                Seller seller = sellerRepository.findByEmail(email);
                if(seller == null) {
                    throw new RuntimeException("Seller not found");
                }
            }
            else {
                User user = userRepository.findByEmail(email);
                if(user == null) {
                    throw new RuntimeException("user not found");
                }
            }
        }

        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email);

        if(verificationCode != null) {
            verificationCodeRepository.delete(verificationCode);
        }

        String otp = OtpUtil.generateOtp();
        VerificationCode newVerificationCode = new VerificationCode();
        newVerificationCode.setEmail(email);
        newVerificationCode.setOtp(otp);

        verificationCodeRepository.save(newVerificationCode);

        String subject = "Login/Signup OTP";
        String message = "Your OTP is " + otp;

        emailService.sendVerificationOtpEmail(email, otp, subject, message);
    }


    @Override
    public String signup(SignupRequest signupRequest) {

        VerificationCode verificationCode = verificationCodeRepository.findByEmail(signupRequest.getEmail());
        if(verificationCode == null || !verificationCode.getOtp().equals(signupRequest.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(signupRequest.getEmail());

        if(user == null) {
            User createUser = new User();
            createUser.setEmail(signupRequest.getEmail());
            createUser.setFullName(signupRequest.getFullName());
            createUser.setRole(USER_ROLE.CUSTOMER);
            createUser.setMobile("0888738167");
            createUser.setPassword(passwordEncoder.encode(signupRequest.getOtp()));

            userRepository.save(createUser);

            Cart cart = new Cart();
            cart.setUser(createUser);
            cartRepository.save(cart);

        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(USER_ROLE.CUSTOMER::toString);

        Authentication auth = new UsernamePasswordAuthenticationToken(signupRequest.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        return jwtTokenProvider.generateToken(auth);
    }

    @Override
    public AuthResponse signing(LoginRequest loginRequest) {
        String username = loginRequest.getEmail();
        String otp = loginRequest.getOtp();

        Authentication auth = authenticate(username, otp);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String token = jwtTokenProvider.generateToken(auth);

        AuthResponse response = new AuthResponse();
        response.setJwt(token);
        response.setMessage("Login successfully");

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        String role = authorities.isEmpty()?null:authorities.iterator().next().getAuthority();

        response.setRole(USER_ROLE.valueOf(role));

        return response;

    }

    private Authentication authenticate(String username, String otp) {
        UserDetails userDetails = customerServiceImp.loadUserByUsername(username);
        if(userDetails == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

         VerificationCode verificationCode = verificationCodeRepository.findByEmail(username);

        if(verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            throw new BadCredentialsException("Invalid OTP");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }


}
