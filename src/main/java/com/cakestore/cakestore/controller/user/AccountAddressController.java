package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.Address;
import com.cakestore.cakestore.security.CustomUserDetails;
import com.cakestore.cakestore.service.user.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/account/address")
public class AccountAddressController {

    private final AddressService addressService;

    // ================== LIST ==================
    @GetMapping
    public String viewAddresses(
            Authentication auth,
            Model model,
            @RequestParam(value = "msg", required = false) String msg,
            @RequestParam(value = "err", required = false) String err) {

        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();

        List<Address> addresses = addressService.listAddresses(userId);

        model.addAttribute("addresses", addresses);
        model.addAttribute("msg", msg);
        model.addAttribute("err", err);

        return "account/address-list";
    }

    // (tuỳ UI) show form edit 1 địa chỉ cụ thể
    // Nếu bạn không có trang address-edit.html riêng thì có thể bỏ block này.
    @GetMapping("/edit/{addrId}")
    public String editAddressPage(
            Authentication auth,
            @PathVariable("addrId") Long addrId,
            Model model,
            @RequestParam(value = "err", required = false) String err) {

        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();

        // lấy địa chỉ để fill vào form
        Address addr = addressService.getAddressForCheckout(userId, addrId);

        model.addAttribute("addr", addr);
        model.addAttribute("err", err);
        return "account/address-edit";
    }

    // ================== ADD ==================
    @PostMapping("/add")
    public String addAddress(
            Authentication auth,
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam("line1") String line1,
            @RequestParam(value = "ward", required = false) String ward,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam("city") String city) {

        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();

        try {
            addressService.addAddress(userId, fullName, phone, line1, ward, district, city);
            return "redirect:/account/address?msg=added";
        } catch (Exception e) {
            return "redirect:/account/address?err=add_failed";
        }
    }

    // ================== UPDATE ==================
    @PostMapping("/update/{addrId}")
    public String updateAddress(
            Authentication auth,
            @PathVariable("addrId") Long addrId,

            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam("line1") String line1,
            @RequestParam(value = "ward", required = false) String ward,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam("city") String city,

            // checkbox "Đặt làm mặc định" trong form sửa (tên field ví dụ setDefault)
            @RequestParam(value = "setDefault", required = false) String setDefaultFlag) {
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();

        // nếu checkbox tồn tại trong form (checked) => setDefaultFlag != null
        Boolean makeDefault = (setDefaultFlag != null);

        try {
            addressService.updateAddress(
                    userId,
                    addrId,
                    fullName,
                    phone,
                    line1,
                    ward,
                    district,
                    city,
                    makeDefault);
            return "redirect:/account/address?msg=updated";
        } catch (Exception e) {
            // nếu bạn có trang edit riêng thì có thể redirect về
            // /account/address/edit/{addrId}
            return "redirect:/account/address?err=update_failed";
        }
    }

    // ================== SET DEFAULT (quick action) ==================
    @PostMapping("/default/{addrId}")
    public String setDefault(
            Authentication auth,
            @PathVariable("addrId") Long addrId) {

        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();

        try {
            addressService.setDefault(userId, addrId);
            return "redirect:/account/address?msg=default_ok";
        } catch (Exception e) {
            return "redirect:/account/address?err=default_failed";
        }
    }

    // ================== DELETE ==================
    @PostMapping("/delete/{addrId}")
    public String deleteAddress(
            Authentication auth,
            @PathVariable("addrId") Long addrId) {

        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();

        try {
            addressService.deleteAddress(userId, addrId);
            return "redirect:/account/address?msg=deleted";
        } catch (Exception e) {
            return "redirect:/account/address?err=delete_failed";
        }
    }
}
