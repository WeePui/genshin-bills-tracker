package com.genshin.tracker.repository;

import com.genshin.tracker.model.GenshinAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenshinAccountRepository extends JpaRepository<GenshinAccount, Long> {
    Optional<GenshinAccount> findByUid(String uid);
    boolean existsByUid(String uid);
    List<GenshinAccount> findAllByOrderByNicknameAsc();
}
