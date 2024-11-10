package com.koomi.service.Imp;

import com.koomi.config.JwtTokenProvider;
import com.koomi.domain.AccountStatus;
import com.koomi.domain.USER_ROLE;
import com.koomi.entity.Address;
import com.koomi.entity.BankDetails;
import com.koomi.entity.Seller;
import com.koomi.repository.AddressRepository;
import com.koomi.repository.SellerRepository;
import com.koomi.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class SellerServiceImp implements SellerService {

    private final SellerRepository sellerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;

    @Override
    public Seller getSellerProfile(String jwt) {

        String email = jwtTokenProvider.getEmailFromToken(jwt);

        return sellerRepository.findByEmail(email);
    }

    @Override
    public Seller createSeller(Seller seller) {

        Seller sellerExists = sellerRepository.findByEmail(seller.getEmail());
        if(sellerExists != null) {
            throw new RuntimeException("Seller already exists, used different email");
        }
        Address saveAddress = addressRepository.save(seller.getPickupAddress());

        Seller newSeller = new Seller();
        newSeller.setEmail(seller.getEmail());
        newSeller.setPassword(passwordEncoder.encode(seller.getPassword()));
        newSeller.setSellerName(seller.getSellerName());
        newSeller.setPickupAddress(saveAddress);
        newSeller.setGSTIN(seller.getGSTIN());
        newSeller.setRole(USER_ROLE.SELLER);
        newSeller.setMobile(seller.getMobile());
        newSeller.setBankDetails(seller.getBankDetails());
        newSeller.setBusinessDetails(seller.getBusinessDetails());

        return sellerRepository.save(newSeller);
    }

    @Override
    public Seller getSellerById(Long id) {

        return sellerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seller not found with id: " + id));
    }

    @Override
    public Seller getSellerByEmail(String email) {

        Seller seller = sellerRepository.findByEmail(email);
        if(seller == null) {
            throw new RuntimeException("Seller not found");
        }
        return seller;

    }

    @Override
    public List<Seller> getAllSellers(AccountStatus accountStatus) {
        return sellerRepository.findByAccountStatus(accountStatus);
    }

    @Override
    public Seller updateSeller(Seller seller) {
        Seller existingSeller = sellerRepository.findById(seller.getId())
                .orElseThrow(() -> new RuntimeException("Seller not found with id: " + seller.getId()));

        // Sử dụng phương thức tiện ích để kiểm tra và cập nhật các trường
        updateIfPresent(seller.getSellerName(), existingSeller::setSellerName);
        updateIfPresent(seller.getMobile(), existingSeller::setMobile);
        updateIfPresent(seller.getEmail(), existingSeller::setEmail);
        updateIfPresent(seller.getGSTIN(), existingSeller::setGSTIN);
        updateIfPresent(seller.getBusinessDetails(), existingSeller::setBusinessDetails);
        updateIfPresent(seller.getAccountStatus(), existingSeller::setAccountStatus);

        // Kiểm tra và cập nhật PickupAddress nếu hợp lệ
        if (seller.getPickupAddress() != null) {
            updatePickupAddressIfValid(existingSeller, seller.getPickupAddress());
        }

        // Kiểm tra và cập nhật BankDetails nếu hợp lệ
        if (seller.getBankDetails() != null) {
            updateBankDetailsIfValid(existingSeller, seller.getBankDetails());
        }

        return sellerRepository.save(existingSeller);
    }

    // Phương thức tiện ích để kiểm tra null và cập nhật các trường cơ bản
    private <T> void updateIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    // Phương thức để cập nhật PickupAddress nếu tất cả các trường hợp lệ
    private void updatePickupAddressIfValid(Seller existingSeller, Address newAddress) {
        if (newAddress.getCity() != null && newAddress.getState() != null
                && newAddress.getMobile() != null && newAddress.getAddress() != null) {
            existingSeller.setPickupAddress(newAddress);
            existingSeller.getPickupAddress().setCity(newAddress.getCity());
            existingSeller.getPickupAddress().setState(newAddress.getState());
            existingSeller.getPickupAddress().setMobile(newAddress.getMobile());
            existingSeller.getPickupAddress().setAddress(newAddress.getAddress());
        }
    }

    // Phương thức để cập nhật BankDetails nếu tất cả các trường hợp lệ
    private void updateBankDetailsIfValid(Seller existingSeller, BankDetails newBankDetails) {
        if (newBankDetails.getAccountNumber() != null && newBankDetails.getIfscCode() != null
                && newBankDetails.getAccountHolderName() != null) {
            existingSeller.getBankDetails().setAccountNumber(newBankDetails.getAccountNumber());
            existingSeller.getBankDetails().setIfscCode(newBankDetails.getIfscCode());
            existingSeller.getBankDetails().setAccountHolderName(newBankDetails.getAccountHolderName());
        }
    }


    @Override
    public void deleteSeller(Long id) {
        sellerRepository.deleteById(id);
    }

    @Override
    public Seller verifySeller(String email, String otp) {
        Seller seller = sellerRepository.findByEmail(email);
        seller.setEmailVerified(true);

        return sellerRepository.save(seller);
    }

    @Override
    public Seller updateSellerStatus(Long id, AccountStatus accountStatus) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seller not found with id: " + id));
        seller.setAccountStatus(accountStatus);
        return sellerRepository.save(seller);

    }
}
