package com.koomi.service;

import com.koomi.domain.AccountStatus;
import com.koomi.entity.Seller;

import java.util.List;

public interface SellerService {

    Seller getSellerProfile(String jwt);
    Seller createSeller(Seller seller);
    Seller getSellerById(Long id);
    Seller getSellerByEmail(String email);
    List<Seller> getAllSellers(AccountStatus accountStatus);
    Seller updateSeller(Seller seller);
    void deleteSeller(Long id);
    Seller verifySeller(String email, String otp);
    Seller updateSellerStatus(Long id, AccountStatus accountStatus);

}
