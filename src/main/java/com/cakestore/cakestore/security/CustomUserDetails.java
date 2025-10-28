package com.cakestore.cakestore.security;

import com.cakestore.cakestore.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Đây là object sẽ nằm trong Authentication.getPrincipal()
 * sau khi đăng nhập thành công.
 * Quan trọng: có getId() để controller biết user hiện tại là ai.
 */
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final boolean active;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole(); // "customer", "admin", "shipper", ...
        this.active = user.isActive(); // true / false
    }

    public Long getId() {
        return id;
    }

    public String getRoleRaw() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security yêu cầu authority kiểu "ROLE_X"
        // User.role trong DB là "customer" -> trả "ROLE_CUSTOMER"
        String springRoleName = "ROLE_" + role.toUpperCase();
        return List.of(new SimpleGrantedAuthority(springRoleName));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        // app mình đăng nhập bằng email, nên username = email
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // chưa có logic hết hạn tài khoản
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // chưa có logic khoá manual
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // chưa force đổi mật khẩu định kỳ
    }

    @Override
    public boolean isEnabled() {
        // nếu user.isActive == false => không cho login
        return active;
    }
}
