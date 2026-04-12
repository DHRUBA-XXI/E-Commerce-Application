package com.DhrubaStudio.E_commercePlatform.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile,Long> {
    Optional<CustomerProfile> findByUserId(Long userId);
}
