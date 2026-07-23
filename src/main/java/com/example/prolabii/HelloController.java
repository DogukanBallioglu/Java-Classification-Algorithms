package com.example.prolabii;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class HelloController {

    @FXML private Label statusLabel;
    @FXML private TextField kInput;
    @FXML private TextField depthInput;
    @FXML private TextField splitInput;
    @FXML private TextArea resultArea;
    @FXML private BarChart<String, Number> accuracyChart;
    @FXML private ComboBox<String> modelSelector;

    @FXML private Label kLabel;
    @FXML private Label depthLabel;

    private List<UserRecord> allData;
    private Evaluator evaluator;

    @FXML
    public void initialize() {
        modelSelector.setItems(FXCollections.observableArrayList(
                "K-En Yakın Komşu (KNN)",
                "Karar Ağacı (Decision Tree)"
        ));
        modelSelector.getSelectionModel().selectFirst();
        modelSelector.setOnAction(event -> updateInputVisibility());
        updateInputVisibility();
    }

    private void updateInputVisibility() {
        String selected = modelSelector.getValue();
        if (selected.equals("K-En Yakın Komşu (KNN)")) {
            showNode(kInput, true);
            showNode(kLabel, true);
            showNode(depthInput, false);
            showNode(depthLabel, false);
        } else {
            showNode(kInput, false);
            showNode(kLabel, false);
            showNode(depthInput, true);
            showNode(depthLabel, true);
        }
    }

    private void showNode(javafx.scene.Node node, boolean isVisible) {
        if (node != null) {
            node.setVisible(isVisible);
            node.setManaged(isVisible);
        }
    }

    @FXML
    protected void onLoadDataClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Excel Veri Setini Seçiniz");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Dosyaları", "*.xlsx"));

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            statusLabel.setText("Veri okunuyor...");
            allData = DataLoader.loadData(file.getAbsolutePath());

            if (allData != null && !allData.isEmpty()) {
                PreProcessor.normalizeHarcama(allData);
                statusLabel.setText("Başarılı: " + allData.size() + " kayıt yüklendi.");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
        }
    }

    @FXML
    protected void onRunModelsClick() {
        if (allData == null || allData.isEmpty()) {
            resultArea.appendText("Hata: Önce veri yükleyin!\n");
            return;
        }

        try {
            double splitRatio = Double.parseDouble(splitInput.getText());
            evaluator = new Evaluator();
            evaluator.splitData(allData, splitRatio);

            String selectedModel = modelSelector.getValue();
            String modelTitle = "";
            IClassifier model;

            if (selectedModel.equals("K-En Yakın Komşu (KNN)")) {
                int kValue = Integer.parseInt(kInput.getText());
                model = new KNNClassifier(kValue);
                modelTitle = "KNN (K=" + kValue + ")";
            } else {
                int depthValue = Integer.parseInt(depthInput.getText());
                model = new DecisionTreeClassifier(depthValue);
                modelTitle = "Karar Ağacı (Derinlik=" + depthValue + ")";
            }

            String report = evaluator.evaluateModel(model, modelTitle);
            resultArea.appendText(report + "\n-------------------\n");

            addChartData(modelTitle, evaluator.getLastAccuracy());

            showConfusionMatrixVisual(evaluator.getLastMatrix(), modelTitle);

        } catch (Exception e) {
            resultArea.appendText("Hata: Parametreleri kontrol edin (Sayı girdiğinizden emin olun).\n");
        }
    }

    private void addChartData(String name, double accuracy) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        series.getData().add(new XYChart.Data<>("Doğruluk", accuracy));
        accuracyChart.getData().add(series);
    }

    private void showConfusionMatrixVisual(Map<String, Map<String, Integer>> matrix, String title) {
        if (matrix == null) return;

        Stage stage = new Stage();
        stage.setTitle(title + " - Detaylı Sınıflandırma Analizi");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(3);
        grid.setVgap(3);

        Set<String> categories = new TreeSet<>(matrix.keySet());
        for (Map<String, Integer> p : matrix.values()) categories.addAll(p.keySet());
        List<String> catList = new ArrayList<>(categories);

        for (int i = 0; i < catList.size(); i++) {
            grid.add(new Label(catList.get(i)), i + 1, 0);
            grid.add(new Label(catList.get(i)), 0, i + 1);
        }

        for (int r = 0; r < catList.size(); r++) {
            for (int c = 0; c < catList.size(); c++) {
                int val = matrix.getOrDefault(catList.get(r), new HashMap<>()).getOrDefault(catList.get(c), 0);
                Label l = new Label(String.valueOf(val));
                l.setMinSize(50, 40);
                l.setAlignment(Pos.CENTER);

                String color = (catList.get(r).equals(catList.get(c))) ? "#a5d6a7" : (val > 0 ? "#ef9a9a" : "#ffffff");
                l.setStyle("-fx-background-color: " + color + "; -fx-border-color: #ccc;");
                grid.add(l, c + 1, r + 1);
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setPrefHeight(400);
        scrollPane.setFitToWidth(true);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Kategoriler");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Tahmin Sayısı");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Kategori Bazlı Doğru ve Yanlış Tahmin Dağılımı");
        barChart.setPrefHeight(350);

        XYChart.Series<String, Number> seriesCorrect = new XYChart.Series<>();
        seriesCorrect.setName("Doğru Tahmin (Yeşil Köşegen)");

        XYChart.Series<String, Number> seriesIncorrect = new XYChart.Series<>();
        seriesIncorrect.setName("Yanlış Tahmin (Kırmızı Hatalar)");

        for (String cat : catList) {
            int correct = matrix.getOrDefault(cat, new HashMap<>()).getOrDefault(cat, 0);

            int totalActual = 0;
            Map<String, Integer> row = matrix.getOrDefault(cat, new HashMap<>());
            for (int val : row.values()) {
                totalActual += val;
            }

            int incorrect = totalActual - correct;

            seriesCorrect.getData().add(new XYChart.Data<>(cat, correct));
            seriesIncorrect.getData().add(new XYChart.Data<>(cat, incorrect));
        }

        barChart.getData().addAll(seriesCorrect, seriesIncorrect);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(15));
        mainLayout.getChildren().addAll(scrollPane, barChart);

        stage.setScene(new Scene(mainLayout, 900, 800));
        stage.show();
    }
}