package com.koomi.entity;

import lombok.Data;

@Data
public class BankDetails {
    private String accountNumber;
    private String ifscCode;
//    private String bankName;
    private String accountHolderName;
}
