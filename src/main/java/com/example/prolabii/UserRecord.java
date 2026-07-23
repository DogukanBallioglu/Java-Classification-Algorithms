package com.example.prolabii;

public class UserRecord {
    private int clientCode;
    private double gender;
    private double lineNetTotal;
    private int brandCode;
    private String category;

    public UserRecord(int clientCode, double gender, double lineNetTotal, int brandCode, String category) {
        this.clientCode = clientCode;
        this.gender = gender;
        this.lineNetTotal = lineNetTotal;
        this.brandCode = brandCode;
        this.category = category;
    }

    public void setLineNetTotal(double lineNetTotal) {
        this.lineNetTotal = lineNetTotal;
    }

    public int getClientCode() {
        return clientCode;
    }

    public double getGender() {
        return gender;
    }

    public double getLineNetTotal() {
        return lineNetTotal;
    }

    public int getBrandCode() {
        return brandCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
