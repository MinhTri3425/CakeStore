// AddressRepository.java
package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserIdOrderByIsDefaultDescUpdatedAtDesc(Long userId);
}
