package com.example.prolabii;

import java.util.*;

public class KNNClassifier extends BaseAlgorithm {

    private int k;

    // Algoritmanın kararsız kalmasını önlemek için bakacağı komşu sayısını belirler.
    public KNNClassifier(int k) {
        this.k = k;
    }

    // İki müşteri profili arasındaki matematiksel benzerliği Öklid Mesafesi ile hesaplar.
    // Özelliklerin farklarının kareleri toplamının karekökü alınarak çok boyutlu uzayda uzaklık bulunur.
    private double calculateDistance(UserRecord u1, UserRecord u2) {
        double sum = 0;

        sum += Math.pow(u1.getGender() - u2.getGender(), 2);
        sum += Math.pow(u1.getLineNetTotal() - u2.getLineNetTotal(), 2);
        sum += Math.pow(u1.getBrandCode() - u2.getBrandCode(), 2);

        return Math.sqrt(sum);
    }

    // Yeni gelen müşterinin bilgilerini kullanarak tüm eğitim verilerine olan uzaklığını ölçer ve sınıfını tahmin eder.
    @Override
    public String predict(UserRecord targetUser) {
        List<Neighbor> distances = new ArrayList<>();

        // 1. ADIM: Hedef müşterinin, eğitim setindeki TÜM müşterilere olan tek tek uzaklığını hesaplar.
        for (UserRecord trainUser : trainingData) {
            double dist = calculateDistance(targetUser, trainUser);
            distances.add(new Neighbor(dist, trainUser.getCategory()));
        }

        // 2. ADIM: Hesaplanan tüm uzaklıkları en yakından en uzağa doğru sıralar.
        Collections.sort(distances);

        Map<String, Integer> categoryCounts = new HashMap<>();

        // 3. ADIM: En yakın "K" adet komşuyu seç ve hangi kategoriden olduklarını sayar.
        for (int i = 0; i < k; i++) {
            if (i >= distances.size()) break;

            String category = distances.get(i).category;
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }

        // Komşular arasında en çok tekrar eden kategoriyi bulur.
        String bestCategory = null;
        int maxVote = -1;
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            if (entry.getValue() > maxVote) {
                maxVote = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        return bestCategory;
    }

    // Müşterilerin birbirine olan uzaklıklarını ve gerçek kategorilerini bir arada tutar ve sıralar.
    private class Neighbor implements Comparable<Neighbor> {
        double distance;
        String category;

        public Neighbor(double distance, String category) {
            this.distance = distance;
            this.category = category;
        }

        // Uzaklığa göre otomatik sıralar.
        @Override
        public int compareTo(Neighbor other) {
            return Double.compare(this.distance, other.distance);
        }
    }
}