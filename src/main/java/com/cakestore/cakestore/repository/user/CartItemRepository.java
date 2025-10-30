// ...existing code...
package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// CartItemRepository.java
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);

    List<CartItem> findByCart_Id(Long cartId);

    void deleteByCartId(Long cartId);

    void deleteByCart_Id(Long cartId);

    // === thêm mấy cái này:
    Optional<CartItem> findByCart_IdAndProduct_IdAndVariant_Id(Long cartId, Long productId, Long variantId);

    Optional<CartItem> findByCart_IdAndProduct_IdAndVariantIsNull(Long cartId, Long productId);

    void deleteByCart_IdAndProduct_IdAndVariant_Id(Long cartId, Long productId, Long variantId);

    void deleteByCart_IdAndProduct_IdAndVariantIsNull(Long cartId, Long productId);
}
