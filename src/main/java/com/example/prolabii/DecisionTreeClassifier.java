package com.example.prolabii;

import java.util.*;

public class DecisionTreeClassifier extends BaseAlgorithm {

    private int maxDepth;
    private TreeNode root;

    // Ağacın inebileceği maksimum derinliği belirler.
    public DecisionTreeClassifier(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    // Eğitim verilerini kullanarak karar ağacını kökten itibaren kurmaya başlar.
    @Override
    public void train(List<UserRecord> trainingData) {
        super.train(trainingData);
        this.root = buildTree(trainingData, 0);
    }

    // Ağacı özyinelemeli (recursive) olarak dal dal oluşturan fonksiyondur.
    private TreeNode buildTree(List<UserRecord> data, int currentDepth) {
        if (data.isEmpty()) return null;
        if (currentDepth >= maxDepth || isAllSameCategory(data)) {
            return new TreeNode(getMostCommonCategory(data));
        }

        SplitResult bestSplit = findBestSplit(data);

        if (bestSplit == null || bestSplit.leftData.isEmpty() || bestSplit.rightData.isEmpty()) {
            return new TreeNode(getMostCommonCategory(data));
        }

        TreeNode node = new TreeNode(bestSplit.featureName, bestSplit.threshold);
        node.left = buildTree(bestSplit.leftData, currentDepth + 1);
        node.right = buildTree(bestSplit.rightData, currentDepth + 1);

        return node;
    }

    // Eğitilmiş ağacı kullanarak yeni bir müşterinin kategorisini tahmin eder.
    @Override
    public String predict(UserRecord targetUser) {
        return traverseTree(targetUser, root);
    }

    // Müşterinin özelliklerine göre ağaç üzerinde kural sorarak aşağı doğru ilerler.
    private String traverseTree(UserRecord user, TreeNode node) {
        if (node.isLeaf) return node.category;

        double valueToCompare = 0;
        switch (node.featureName) {
            case "Gender": valueToCompare = user.getGender(); break;
            case "LineNetTotal": valueToCompare = user.getLineNetTotal(); break;
            case "BrandCode": valueToCompare = user.getBrandCode(); break;
        }

        if (valueToCompare <= node.threshold) {
            return traverseTree(user, node.left);
        } else {
            return traverseTree(user, node.right);
        }
    }

    // Ağaçtaki her bir düğümdür.
    private class TreeNode {
        boolean isLeaf;
        String category;
        String featureName;
        double threshold;
        TreeNode left;
        TreeNode right;

        public TreeNode(String category) {
            this.isLeaf = true;
            this.category = category;
        }

        public TreeNode(String featureName, double threshold) {
            this.isLeaf = false;
            this.featureName = featureName;
            this.threshold = threshold;
        }
    }

    // Verilen listedeki en çok tekrar eden kategoriyi bulur.
    private String getMostCommonCategory(List<UserRecord> data) {
        Map<String, Integer> counts = new HashMap<>();
        for (UserRecord record : data) {
            counts.put(record.getCategory(), counts.getOrDefault(record.getCategory(), 0) + 1);
        }
        return Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    // Listedeki tüm verilerin aynı kategoriye ait olup olmadığını kontrol eder.
    private boolean isAllSameCategory(List<UserRecord> data) {
        if (data.isEmpty()) return true;
        String firstCat = data.get(0).getCategory();
        for (UserRecord record : data) {
            if (!record.getCategory().equals(firstCat)) return false;
        }
        return true;
    }

    // Bölünme işlemi sonucunda ortaya çıkan verileri ve sınır değerlerini tutar.
    private class SplitResult {
        String featureName;
        double threshold;
        List<UserRecord> leftData = new ArrayList<>();
        List<UserRecord> rightData = new ArrayList<>();
    }

    // Sınır değeri için gini'yi hesaplar.
    private SplitResult findBestSplit(List<UserRecord> data) {
        SplitResult bestSplit = new SplitResult();
        double bestGini = Double.MAX_VALUE;
        String[] features = {"Gender", "LineNetTotal", "BrandCode"};

        for (String feature : features) {
            Set<Double> thresholds = new HashSet<>();
            for (UserRecord r : data) {
                if (feature.equals("Gender")) thresholds.add(r.getGender());
                else if (feature.equals("LineNetTotal")) thresholds.add(r.getLineNetTotal());
                else if (feature.equals("BrandCode")) thresholds.add((double) r.getBrandCode());
            }

            for (Double threshold : thresholds) {
                List<UserRecord> left = new ArrayList<>();
                List<UserRecord> right = new ArrayList<>();

                for (UserRecord r : data) {
                    double val = 0;
                    if (feature.equals("Gender")) val = r.getGender();
                    else if (feature.equals("LineNetTotal")) val = r.getLineNetTotal();
                    else if (feature.equals("BrandCode")) val = r.getBrandCode();

                    if (val <= threshold) left.add(r);
                    else right.add(r);
                }

                if (left.isEmpty() || right.isEmpty()) continue;

                double gini = calculateSplitGini(left, right);

                if (gini < bestGini) {
                    bestGini = gini;
                    bestSplit.featureName = feature;
                    bestSplit.threshold = threshold;
                    bestSplit.leftData = left;
                    bestSplit.rightData = right;
                }
            }
        }
        return bestSplit.featureName != null ? bestSplit : null;
    }

    // Gininin safsızlık derecesini hesaplar.
    private double calculateSplitGini(List<UserRecord> left, List<UserRecord> right) {
        double totalSize = left.size() + right.size();
        return (left.size() / totalSize) * calculateGini(left) + (right.size() / totalSize) * calculateGini(right);
    }

    // Bir veri grubunun kendi içindeki Gini safsızlık (karmaşıklık) değerini ölçer.
    private double calculateGini(List<UserRecord> data) {
        Map<String, Integer> counts = new HashMap<>();
        for (UserRecord r : data) counts.put(r.getCategory(), counts.getOrDefault(r.getCategory(), 0) + 1);

        double impurity = 1.0;
        for (int count : counts.values()) {
            double prob = (double) count / data.size();
            impurity -= prob * prob;
        }
        return impurity;
    }
}