package com.miracle.coordifit.auth.service;

public interface IEmailService {    
    boolean sendVerificationCode(String email);        
    boolean verifyCode(String email, String code);
}
