package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.OtpToken;
import com.cakestore.cakestore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByUserAndPurposeOrderByCreatedAtDesc(User user, OtpToken.Purpose purpose);
}
