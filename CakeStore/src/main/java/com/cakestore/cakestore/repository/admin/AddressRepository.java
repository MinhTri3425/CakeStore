package com.cakestore.cakestore.repository.admin;

import com.cakestore.cakestore.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // Tìm địa chỉ mặc định của một User
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}