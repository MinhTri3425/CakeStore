package com.cakestore.cakestore.repository.admin;

import com.cakestore.cakestore.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminCategoryRepository extends JpaRepository<Category, Long> {
	// Repository này sẽ được sử dụng để quản lý CRUD Category
	@Query("""
			select c from Category c
			where (:q is null or lower(c.name) like lower(concat('%', :q, '%'))
			               or lower(c.slug) like lower(concat('%', :q, '%')))
			  and (:active is null or c.isActive = :active)
			""")
	Page<Category> search(@Param("q") String q,
			@Param("active") Boolean active,
			Pageable pageable);

	boolean existsByNameIgnoreCase(String name);
}