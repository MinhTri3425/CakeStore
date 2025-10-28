package com.cakestore.cakestore.viewmodel;

public class AccountProfileVM {
    private Long id;
    private String email;
    private String fullName;
    private String phone;

    // địa chỉ mặc định
    private String addressFullName; // tên người nhận
    private String addressPhone; // sđt nhận hàng
    private String addressLine1; // số nhà / đường
    private String addressWard;
    private String addressDistrict;
    private String addressCity;

    // text để render nhanh gọn
    private String addressDisplay; // "12 Lý Thường Kiệt, P.7, Q.10, TP.HCM"

    private String memberSince;

    // getters/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressFullName() {
        return addressFullName;
    }

    public void setAddressFullName(String addressFullName) {
        this.addressFullName = addressFullName;
    }

    public String getAddressPhone() {
        return addressPhone;
    }

    public void setAddressPhone(String addressPhone) {
        this.addressPhone = addressPhone;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressWard() {
        return addressWard;
    }

    public void setAddressWard(String addressWard) {
        this.addressWard = addressWard;
    }

    public String getAddressDistrict() {
        return addressDistrict;
    }

    public void setAddressDistrict(String addressDistrict) {
        this.addressDistrict = addressDistrict;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressDisplay() {
        return addressDisplay;
    }

    public void setAddressDisplay(String addressDisplay) {
        this.addressDisplay = addressDisplay;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }
}
