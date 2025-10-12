package com.frisk.cadettsplitterspadel.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "padel_customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable=false, length = 100)
    private String name;

    @Column(unique=true, nullable=false, length = 100)
    private String email;

    @Column(unique=true, nullable=false, length = 20)
    private String phone;

    @Column(name = "user_id", length = 100, unique = true, nullable = false)
    private String userId;

    public String getUserId() {return userId;}

    public void setUserId(String userId) {this.userId = userId;}

    public Customer() {
    }

    public Customer(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
