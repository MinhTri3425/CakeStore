// ...existing code...
package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // convenience finders to support CartDbService (may match entity fields
    // getUserId or getUser().getId)
    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findByUser_Id(Long userId);
}