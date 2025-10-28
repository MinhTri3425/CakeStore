package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.entity.Address;
import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Address> listAddresses(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // sort: địa chỉ default lên trước, sau đó theo id tăng dần
        return u.getAddresses()
                .stream()
                .sorted((a, b) -> {
                    if (a.isDefault() && !b.isDefault())
                        return -1;
                    if (!a.isDefault() && b.isDefault())
                        return 1;
                    return a.getId().compareTo(b.getId());
                })
                .toList();
    }

    @Transactional
    public Address addAddress(
            Long userId,
            String fullName,
            String phone,
            String line1,
            String ward,
            String district,
            String city) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Address a = new Address();
        a.setUser(u);
        a.setFullName(fullName);
        a.setPhone(phone);
        a.setLine1(line1);
        a.setWard(ward);
        a.setDistrict(district);
        a.setCity(city);

        // Nếu user chưa có địa chỉ nào -> auto default
        boolean first = u.getAddresses().isEmpty();
        a.setDefault(first);

        u.getAddresses().add(a);
        userRepository.save(u);

        return a;
    }

    @Transactional
    public void setDefault(Long userId, Long addressId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Address picked = u.getAddresses()
                .stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Address not found for this user"));

        // tắt default cho tất cả
        for (Address addr : u.getAddresses()) {
            addr.setDefault(false);
        }

        // bật default cho cái được chọn
        picked.setDefault(true);

        userRepository.save(u);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Address target = u.getAddresses()
                .stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Address not found for this user"));

        boolean wasDefault = target.isDefault();

        // gỡ ra khỏi list (orphanRemoval=true trong User.addresses sẽ lo delete)
        u.getAddresses().remove(target);

        // nếu xoá default -> promote địa chỉ khác làm default
        if (wasDefault) {
            u.getAddresses()
                    .stream()
                    .min(Comparator.comparing(Address::getId))
                    .ifPresent(addr -> addr.setDefault(true));
        }

        userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public Address getAddressForCheckout(Long userId, Long addressId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return u.getAddresses()
                .stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Address not found for this user"));
    }
}
