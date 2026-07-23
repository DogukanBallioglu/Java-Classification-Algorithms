package com.example.prolabii;
import java.util.List;

public abstract class BaseAlgorithm implements IClassifier {

    protected List<UserRecord> trainingData;

    @Override
    public void train(List<UserRecord> trainingData) {
        this.trainingData = trainingData;
    }

    @Override
    public abstract String predict(UserRecord user);
}