package com.interapt.glass.tacobelltraining;

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

    public String getTestVideo(){
        return testVideo;
    }

    public String getEmployeeId(){
        return employeeId;
    }

    public String getFoodItemName(){
        return foodItemName;
    }

    public int getNumberOfTrials(){
        return numberOfTrials;
    }

    public String generateTestReportContent(){
        return "Taco Bell Training Test Result\nEmployee Id: " + employeeId
                + "\nFood Item: " + foodItemName + "\nNumber of trials: " + String.valueOf(numberOfTrials);
    }
}
