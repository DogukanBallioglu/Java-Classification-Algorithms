package com.example.prolabii;

import java.util.*;

public class Evaluator {

    private List<UserRecord> trainingData = new ArrayList<>();
    private List<UserRecord> testingData = new ArrayList<>();
    private double lastAccuracy = 0;
    private Map<String, Map<String, Integer>> lastMatrix;

    // Veri setini rastgele karıştırarakbelirtilen oranda Eğitimve Test veri setlerine böler.
    public void splitData(List<UserRecord> allData, double trainRatio) {
        trainingData.clear();
        testingData.clear();
        Collections.shuffle(allData);

        int trainSize = (int) (allData.size() * trainRatio);
        for (int i = 0; i < allData.size(); i++) {
            if (i < trainSize) {
                trainingData.add(allData.get(i));
            } else {
                testingData.add(allData.get(i));
            }
        }
    }

    public String evaluateModel(IClassifier model, String modelName) {

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        //Modelin eğitim aşamasıdır.
        long startTimeTrain = System.currentTimeMillis();
        model.train(trainingData);
        long endTimeTrain = System.currentTimeMillis();
        long trainDuration = endTimeTrain - startTimeTrain;

        Map<String, Map<String, Integer>> fullMatrix = new TreeMap<>();
        int correctPredictions = 0;

        // Modelin test aşamasıdır.
        long startTimeTest = System.currentTimeMillis();
        for (UserRecord testUser : testingData) {
            String actualCategory = testUser.getCategory();
            String predictedCategory = model.predict(testUser);

            if (predictedCategory.equals(actualCategory)) {
                correctPredictions++;
            }

            // Yapılan tahminleri Hata Matrisine kaydeder.
            fullMatrix.putIfAbsent(actualCategory, new TreeMap<>());
            Map<String, Integer> row = fullMatrix.get(actualCategory);
            row.put(predictedCategory, row.getOrDefault(predictedCategory, 0) + 1);
        }
        long endTimeTest = System.currentTimeMillis();
        long testDuration = endTimeTest - startTimeTest;

        // Tüketilen net belleği ve Doğruluk oranını hesaplar.
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsedKB = Math.abs((memoryAfter - memoryBefore) / 1024);
        this.lastAccuracy = ((double) correctPredictions / testingData.size()) * 100;
        this.lastMatrix = fullMatrix;

        // Arayüzdeki metin kutusu için matristen sadece "Hatalı" olanları ayıklıyoruz.
        Map<String, Integer> flatErrorMatrix = new HashMap<>();
        for(String actual : fullMatrix.keySet()) {
            for(Map.Entry<String, Integer> pred : fullMatrix.get(actual).entrySet()) {
                if(!actual.equals(pred.getKey())) {
                    flatErrorMatrix.put("Gerçek: " + actual + " -> Tahmin: " + pred.getKey(), pred.getValue());
                }
            }
        }

        return String.format(
                "[%s Sonuçları]\nDoğruluk: %% %.2f\nEğitim Süresi: %d ms\nTest Süresi: %d ms\nKullanılan Bellek: %d KB\n",
                modelName, lastAccuracy, trainDuration, testDuration, memoryUsedKB
        );
    }

    public double getLastAccuracy() {
        return lastAccuracy;
    }

    public Map<String, Map<String, Integer>> getLastMatrix() {
        return lastMatrix;
    }
}