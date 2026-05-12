package io.springbatch.nabimarket.user.repository;

import io.lettuce.core.dynamic.annotation.Param;
import io.springbatch.nabimarket.user.domain.Provider;
import io.springbatch.nabimarket.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT COUNT(*) FROM users WHERE login_id = :loginId", nativeQuery = true)
    long countByLoginIdIncludingDeleted(@Param("loginId") String loginId);

    default boolean existsByLoginIdIncludingDeleted(String loginId) {
        return countByLoginIdIncludingDeleted(loginId) > 0;
    }

    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByNicknameAndIdNot(String nickname, Long userId);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
