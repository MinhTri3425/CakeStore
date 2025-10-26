package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Repository này sẽ được sử dụng để quản lý CRUD Category
}