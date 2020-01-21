package com.example.odoosalestester;

import java.util.Map;

public class Partner {
    private Integer id;
    private String name;
    private String street;
    private String street2;
    private String city;
    private String state;
    private String country;
    private String mobile;
    private String email;

    private Integer stateId;
    private Integer countryId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setData(Map<String, Object> classObj) {
        setId((Integer) classObj.get("id"));
        setName(OdooUtility.getString(classObj, "name"));
        setStreet(OdooUtility.getString(classObj, "street"));
        setStreet2(OdooUtility.getString(classObj, "street2"));
        setCity(OdooUtility.getString(classObj, "city"));
        setMobile(OdooUtility.getString(classObj, "mobile"));
        setEmail(OdooUtility.getString(classObj, "email"));

        //country, state : Many2one [12, "Indonesia"]
        M20field country_id = OdooUtility.getMany2One(classObj, "country_id");
        setCountry(country_id.value);
        setCountryId(country_id.id);

        M20field state_id = OdooUtility.getMany2One(classObj, "state_id");
        setState(state_id.value);
        setStateId(state_id.id);
    }
}
