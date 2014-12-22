package com.interapt.mikenguyen.tacobelltraining;

/**
 * Created by mikenguyen on 12/21/14.
 */
public class TestReport {
    private String employeeId;
    private String foodItemName;
    private String testVideo;
    private int numberOfTrials;

    public TestReport(String employeeId, String foodItemName) {
        this.employeeId = employeeId;
        this.foodItemName = foodItemName;
        this.testVideo = "";
        this.numberOfTrials = 0;
    }

    public void incrementNumberOfTrials() {
        this.numberOfTrials++;
    }

    public void setTestVideo(String testVideo) {
        this.testVideo = testVideo;
    }
}
