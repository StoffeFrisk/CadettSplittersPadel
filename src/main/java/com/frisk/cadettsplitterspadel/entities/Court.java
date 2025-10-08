package com.frisk.cadettsplitterspadel.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "padel_court")
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "court_number", nullable = false, unique = true)
    private int courtNumber;

    @Column(name = "hourly_rate_sek", nullable = false)
    private int hourlyRateSek;

    @Column(nullable = false)
    private boolean active = true;

    public Court() {
    }

    public Court(int courtNumber, int hourlyRateSek, boolean active) {
        this.courtNumber = courtNumber;
        this.hourlyRateSek = hourlyRateSek;
        this.active = active;
    }

    public int getCourtNumber() {
        return courtNumber;
    }

    public void setCourtNumber(int courtNumber) {
        this.courtNumber = courtNumber;
    }

    public Integer getId() {
        return id;
    }

    public int getHourlyRateSek() {
        return hourlyRateSek;
    }

    public void setHourlyRateSek(int hourlyRateSek) {
        this.hourlyRateSek = hourlyRateSek;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
