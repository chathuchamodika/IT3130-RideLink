package com.ridelink.account_service.service;

import com.ridelink.account_service.exception.AccountAlreadyExistsException;
import com.ridelink.account_service.dto.AccountResponse;
import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.entity.Account;
import com.ridelink.account_service.entity.AccountStatus;
import com.ridelink.account_service.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse register(RegisterRequest request) {

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AccountAlreadyExistsException("Email already exists");
        }

        Account account = new Account();

        account.setFullName(request.getFullName());
        account.setEmail(request.getEmail());

        account.setPasswordHash(request.getPassword());

        account.setPhoneNumber(request.getPhoneNumber());
        account.setRole(request.getRole());
        account.setStatus(AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(account);

        AccountResponse response = new AccountResponse();

        response.setId(savedAccount.getId());
        response.setFullName(savedAccount.getFullName());
        response.setEmail(savedAccount.getEmail());
        response.setPhoneNumber(savedAccount.getPhoneNumber());
        response.setRole(savedAccount.getRole());
        response.setStatus(savedAccount.getStatus());

        return response;
    }
}
