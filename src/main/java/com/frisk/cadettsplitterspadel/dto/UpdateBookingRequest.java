package com.frisk.cadettsplitterspadel.dto;

public class UpdateBookingRequest {
    private Integer bookingId;
    private String bookingDate;
    private String startTime;
    private String endTime;

    public Integer getBookingId() {
        return bookingId; }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId; }

    public String getBookingDate() {
        return bookingDate; }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate; }

    public String getStartTime() {
        return startTime; }

    public void setStartTime(String startTime) {
        this.startTime = startTime; }

    public String getEndTime() {
        return endTime; }

    public void setEndTime(String endTime) {
        this.endTime = endTime; }
}
