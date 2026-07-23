package com.example.prolabii;

import java.util.List;

public class PreProcessor {

    public static double encodeGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return -1.0;
        }
        if (gender.equalsIgnoreCase("E") || gender.equalsIgnoreCase("Male")) {
            return 1.0;
        }
        if (gender.equalsIgnoreCase("K") || gender.equalsIgnoreCase("Female")) {
            return 0.0;
        }
        return -1.0;
    }

    public static void normalizeHarcama(List<UserRecord> allData) {
        double minFiyat = Double.MAX_VALUE;
        double maxFiyat = Double.MIN_VALUE;

        for (UserRecord record : allData) {
            if (record.getLineNetTotal() < minFiyat) minFiyat = record.getLineNetTotal();
            if (record.getLineNetTotal() > maxFiyat) maxFiyat = record.getLineNetTotal();
        }

        for (UserRecord record : allData) {
            double normalizedFiyat = normalize(record.getLineNetTotal(), minFiyat, maxFiyat);
            record.setLineNetTotal(normalizedFiyat);
        }
    }

    public static double normalize(double value, double min, double max) {
        if (max - min == 0) return 0;
        return (value - min) / (max - min);
    }
}
