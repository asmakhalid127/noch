package com.noch.models;

import java.util.List;

public class User {

    private String id;
    private String email;
    private String name;
    private String type;
    private boolean verified;
    private List<String> purchasedProducts;

    public User(String id, String email, String name,
                String type, boolean verified,
                List<String> purchasedProducts) {
        this.id                = id;
        this.email             = email;
        this.name              = name;
        this.type              = type;
        this.verified          = verified;
        this.purchasedProducts = purchasedProducts;
    }

    public String       getId()                { return id; }
    public String       getEmail()             { return email; }
    public String       getName()              { return name; }
    public String       getType()              { return type; }
    public boolean      isVerified()           { return verified; }
    public List<String> getPurchasedProducts() { return purchasedProducts; }

    public void setId(String id)                            { this.id = id; }
    public void setEmail(String email)                      { this.email = email; }
    public void setName(String name)                        { this.name = name; }
    public void setType(String type)                        { this.type = type; }
    public void setVerified(boolean verified)               { this.verified = verified; }
    public void setPurchasedProducts(List<String> products) { this.purchasedProducts = products; }

    public boolean isCustomer() { return "customer".equals(type); }
    public boolean isAdmin()    { return "admin".equals(type); }
}