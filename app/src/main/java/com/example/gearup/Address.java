package com.example.gearup;

public class Address {
    private String fullName;
    private String phoneNumber;
    private String regionProvinceCity;
    private String postalCode;
    private String streetDetails;
    private String label;
    private boolean isDefaultAddress;

    public Address() {
        // Default constructor required for Firebase
    }

    public Address(String fullName, String phoneNumber, String regionProvinceCity, String postalCode,
                   String streetDetails, String label, boolean isDefaultAddress) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.regionProvinceCity = regionProvinceCity;
        this.postalCode = postalCode;
        this.streetDetails = streetDetails;
        this.label = label;
        this.isDefaultAddress = isDefaultAddress;
    }

    // Getters and setters
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRegionProvinceCity() { return regionProvinceCity; }
    public String getPostalCode() { return postalCode; }
    public String getStreetDetails() { return streetDetails; }
    public String getLabel() { return label; }
    public boolean isDefaultAddress() { return isDefaultAddress; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRegionProvinceCity(String regionProvinceCity) { this.regionProvinceCity = regionProvinceCity; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setStreetDetails(String streetDetails) { this.streetDetails = streetDetails; }
    public void setLabel(String label) { this.label = label; }
    public void setDefaultAddress(boolean defaultAddress) { isDefaultAddress = defaultAddress; }
}


