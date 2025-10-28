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
