package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);

    List<Category> findByIsActiveTrueOrderBySortOrderAscIdAsc();

    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrderAscIdAsc(Long parentId);
}
