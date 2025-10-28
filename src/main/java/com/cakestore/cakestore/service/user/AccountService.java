package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.entity.Address;
import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.UserRepository;
import com.cakestore.cakestore.viewmodel.AccountProfileVM;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ giữ session mở trong lúc mình chạm tới u.getAddresses()
    @Transactional(readOnly = true)
    public AccountProfileVM getProfile(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address defAddr = pickDefaultAddress(u);

        AccountProfileVM vm = new AccountProfileVM();
        vm.setId(u.getId());
        vm.setEmail(u.getEmail());
        vm.setFullName(u.getFullName());
        vm.setPhone(u.getPhone());

        if (defAddr != null) {
            vm.setAddressFullName(defAddr.getFullName());
            vm.setAddressPhone(defAddr.getPhone());
            vm.setAddressLine1(defAddr.getLine1());
            vm.setAddressWard(defAddr.getWard());
            vm.setAddressDistrict(defAddr.getDistrict());
            vm.setAddressCity(defAddr.getCity());
            vm.setAddressDisplay(defAddr.getFullAddress());
        } else {
            vm.setAddressFullName("");
            vm.setAddressPhone("");
            vm.setAddressLine1("");
            vm.setAddressWard("");
            vm.setAddressDistrict("");
            vm.setAddressCity("");
            vm.setAddressDisplay("");
        }

        if (u.getCreatedAt() != null) {
            vm.setMemberSince(
                    u.getCreatedAt()
                            .toLocalDate()
                            .format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else {
            vm.setMemberSince("N/A");
        }

        return vm;
    }

    // updateProfile & upsertDefaultAddress sẽ mutate DB → nên có @Transactional mặc
    // định (readOnly=false)
    @Transactional
    public void updateProfile(Long userId, String fullName, String phone) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        u.setFullName(fullName);
        u.setPhone(phone);

        userRepository.save(u);
    }

    /**
     * Cập nhật (hoặc tạo) địa chỉ mặc định
     */
    @Transactional
    public void upsertDefaultAddress(
            Long userId,
            String recvName,
            String recvPhone,
            String line1,
            String ward,
            String district,
            String city) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address defAddr = pickDefaultAddress(u);

        if (defAddr == null) {
            // chưa có -> tạo mới
            defAddr = new Address();
            defAddr.setUser(u);
            defAddr.setDefault(true);
            u.getAddresses().add(defAddr);
        }

        defAddr.setFullName(recvName);
        defAddr.setPhone(recvPhone);
        defAddr.setLine1(line1);
        defAddr.setWard(ward);
        defAddr.setDistrict(district);
        defAddr.setCity(city);

        userRepository.save(u);
    }

    @Transactional
    public void changePassword(Long userId, String oldPw, String newPw) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPw, u.getPasswordHash())) {
            throw new IllegalArgumentException("WRONG_OLD_PASSWORD");
        }

        u.setPasswordHash(passwordEncoder.encode(newPw));
        userRepository.save(u);
    }

    /**
     * Trả về địa chỉ mặc định nếu có, fallback sang địa chỉ đầu tiên nếu chưa có
     * default.
     * Lưu ý: hàm này assume caller đang ở trong @Transactional, để tránh
     * LazyInitializationException.
     */
    private Address pickDefaultAddress(User u) {
        // ưu tiên isDefault = true
        Optional<Address> def = u.getAddresses()
                .stream()
                .filter(Address::isDefault)
                .findFirst();

        if (def.isPresent()) {
            return def.get();
        }

        // fallback: địa chỉ cũ nhất (id nhỏ nhất)
        return u.getAddresses()
                .stream()
                .sorted(Comparator.comparing(Address::getId))
                .findFirst()
                .orElse(null);
    }
}
