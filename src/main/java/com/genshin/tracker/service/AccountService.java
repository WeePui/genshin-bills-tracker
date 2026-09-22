package com.genshin.tracker.service;

import com.genshin.tracker.model.GenshinAccount;
import com.genshin.tracker.repository.GenshinAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AccountService {

    private final GenshinAccountRepository accountRepository;

    public AccountService(GenshinAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<GenshinAccount> getAllAccounts() {
        return accountRepository.findAllByOrderByNicknameAsc();
    }

    public Optional<GenshinAccount> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public Optional<GenshinAccount> getAccountByUid(String uid) {
        return accountRepository.findByUid(uid);
    }

    public GenshinAccount saveAccount(GenshinAccount account) {
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    public boolean existsByUid(String uid) {
        return accountRepository.existsByUid(uid);
    }
}
