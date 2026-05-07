package io.springbatch.nabimarket.user.repository;

import io.springbatch.nabimarket.user.domain.Provider;
import io.springbatch.nabimarket.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
