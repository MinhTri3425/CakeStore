// src/main/java/com/cakestore/cakestore/service/impl/UserServiceImpl.java
package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.entity.Address;
import com.cakestore.cakestore.entity.Branch; 
import com.cakestore.cakestore.repository.UserRepository;
import com.cakestore.cakestore.repository.AddressRepository;
import com.cakestore.cakestore.repository.BranchRepository; 
import com.cakestore.cakestore.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; // Thêm import

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;
    private final BranchRepository branchRepository; 

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                           AddressRepository addressRepository, BranchRepository branchRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.addressRepository = addressRepository;
        this.branchRepository = branchRepository; 
    }

    @Override
    public Page<User> findUsers(String keyword, Boolean status, Long branchId, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        return userRepository.searchAllWithStatus(kw, status, branchId, pageable);
    }

    @Override
    public Page<User> findUsersByRole(String role, String keyword, Boolean status, Long branchId, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        return userRepository.searchByRoleWithStatus(role, kw, status, branchId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    // THÊM MỚI: Triển khai hàm
    @Override
    @Transactional(readOnly = true)
    public List<User> findPotentialManagers() {
        // Manager có thể là staff hoặc admin
        return userRepository.findByRoleInAndIsActiveTrue(List.of("staff", "admin"));
    }

    @Override
    @Transactional
    public User save(User user, String rawPassword, String line1, String city, String district, String ward, Long branchId) { 
        User savedUser;
        boolean isNew = user.getId() == null;

        if ("staff".equalsIgnoreCase(user.getRole())) {
            if (branchId == null) {
                throw new IllegalArgumentException("Nhân viên (staff) phải được gán vào một chi nhánh.");
            }
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không hợp lệ."));
            user.setBranch(branch);
        } else {
            user.setBranch(null);
        }

        if (isNew) {
            if (rawPassword == null || rawPassword.isBlank()) {
                throw new IllegalArgumentException("Mật khẩu không được để trống khi tạo mới.");
            }
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            if (!user.isActive()) user.setActive(true);
            savedUser = userRepository.save(user);
        } else {
            if (rawPassword != null && !rawPassword.isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
            } else {
                User existed = userRepository.findById(user.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
                user.setPasswordHash(existed.getPasswordHash());
            }
            savedUser = userRepository.save(user);
        }

        if (line1 != null && !line1.isBlank() && city != null && !city.isBlank()) {
            Address defaultAddress = addressRepository.findByUserIdAndIsDefaultTrue(savedUser.getId())
                .orElseGet(() -> {
                    Address newAddr = new Address(savedUser, savedUser.getFullName(), savedUser.getPhone(), line1, city);
                    newAddr.setDefault(true);
                    return newAddr;
                });
            
            defaultAddress.setFullName(savedUser.getFullName());
            defaultAddress.setPhone(savedUser.getPhone());
            defaultAddress.setLine1(line1);
            defaultAddress.setCity(city);
            defaultAddress.setDistrict(district);
            defaultAddress.setWard(ward);
            
            addressRepository.save(defaultAddress);
        }

        return savedUser;
    }

    @Override
    @Transactional
    public void updateActiveStatus(Long id, boolean isActive) {
        userRepository.findById(id).ifPresent(u -> { 
            u.setActive(isActive);
            userRepository.save(u);
        });
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        updateActiveStatus(id, false);
    }

    @Override
    public boolean isEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }
}