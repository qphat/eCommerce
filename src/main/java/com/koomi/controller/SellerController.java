package com.koomi.controller;

import com.koomi.config.JwtTokenProvider;
import com.koomi.domain.AccountStatus;
import com.koomi.entity.Seller;
import com.koomi.entity.SellerReport;
import com.koomi.entity.VerificationCode;
import com.koomi.exception.SellerExeption;
import com.koomi.repository.VerificationCodeRepository;
import com.koomi.request.LoginRequest;
import com.koomi.response.AuthResponse;
import com.koomi.service.AuthService;
import com.koomi.service.EmailService;
import com.koomi.service.SellerService;
import com.koomi.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final SellerService sellerService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final AuthService authService;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginSeller (@RequestBody LoginRequest req) {

        String otp = req.getOtp();
        String email = req.getEmail();

        req.setEmail("seller_" + email);
        AuthResponse authResponse = authService.signing(req);


        return ResponseEntity.ok(authResponse);
    }

    @PatchMapping("/verify/{otp}")
    public ResponseEntity<Seller> verifySellerEmail(@PathVariable String otp) throws SellerExeption {

        VerificationCode verificationCode = verificationCodeRepository.findByOtp(otp);
        if(verificationCode == null || verificationCode.getOtp().equals(otp)) {
            throw new SellerExeption("Invalid OTP");
        }
        Seller seller = sellerService.verifySeller(verificationCode.getEmail(), otp);
        return ResponseEntity.ok(seller);
    }

    @PostMapping
    public ResponseEntity<Seller> createSeller(@RequestBody Seller seller) throws Exception {
        Seller newSeller = sellerService.createSeller(seller);

        String otp = OtpUtil.generateOtp();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(seller.getEmail());
        verificationCode.setOtp(otp);
        verificationCodeRepository.save(verificationCode);

        String subject = "Email Verification";
        String message = "verify your account using this link: ";
        String frontEndUrl = "http://localhost:3000/verify-seller/";
        emailService.sendVerificationOtpEmail(seller.getEmail(), verificationCode.getOtp(), subject, message + frontEndUrl);


        return new ResponseEntity<>(newSeller, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSeller(@PathVariable Long id) {
        Seller seller = sellerService.getSellerById(id);
        return ResponseEntity.ok(seller);
    }

    @GetMapping("/profile")
    public ResponseEntity<Seller> getSellerByJwt(@RequestHeader("Authorization") String jwt) {
        Seller seller  = sellerService.getSellerProfile(jwt);

        return ResponseEntity.ok(seller);
    }

//    @GetMapping("/report")
//    public ResponseEntity<SellerReport> getSellerReport(
//            @RequestHeader("Authorization") String jwt) {
//
//        String email = jwtTokenProvider.getEmailFromToken(jwt);
//        Seller seller = sellerService.getSellerByEmail(email);
//        SellerReport report = sellerService.getSellerReport(seller);
//
//        return ResponseEntity.ok("Report");
//    }

    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers(@RequestParam(required = false) AccountStatus accountStatus) {
        List<Seller> sellers = sellerService.getAllSellers(accountStatus);
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping()
    public ResponseEntity<Seller> updateSeller(
            @RequestHeader("Authorization") String jwt,
            @RequestBody Seller seller) {

        Seller profile = sellerService.getSellerProfile(jwt);

        Seller updatedSeller = sellerService.updateSeller(profile.getId(), seller);
        return ResponseEntity.ok(updatedSeller);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) {
        sellerService.deleteSeller(id);
        return ResponseEntity.noContent().build();
    }


}
