package com.interapt.mikenguyen.tacobelltraining;

/**
 * Created by mikenguyen on 12/21/14.
 */
public class FoodItem {
    private int idNumber;
    private int numberOfSteps;
    private int currentStep;
    private String foodItemName;
    private String[] stepImageArray;
    private String[] stepDescriptionArray;

    public FoodItem(int idNumber, int numberOfSteps,String foodItemName, String[] stepImageArray, String[] stepDescriptionArray ) {
        this.idNumber = idNumber;
        this.numberOfSteps = numberOfSteps;
        this.foodItemName = foodItemName;
        this.stepImageArray = stepImageArray;
        this.stepDescriptionArray = stepDescriptionArray;
        this.currentStep = 0;
    }

    public void setCurrentStep(int step) {
        this.currentStep = step;
    }

    public int getCurrentStep() {
        return this.currentStep;
    }

    public int getIdNumber() {
        return this.idNumber;
    }

    public int getNumberOfSteps() {
        return this.numberOfSteps;
    }

    public String getFoodItemName() {
        return this.foodItemName;
    }

    public String[] getStepImageArray(){
        return this.stepImageArray;
    }

    public String[] getStepDescriptionArray() {
        return this.stepDescriptionArray;
    }
}
