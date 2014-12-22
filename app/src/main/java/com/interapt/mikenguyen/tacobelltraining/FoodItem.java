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

//    public FoodItem(int idNumber, int numberOfSteps, String foodItemName, String[] stepImageArray, String[] stepDescriptionArray) {
//        this.idNumber = idNumber;
//        this.numberOfSteps = numberOfSteps;
//        this.foodItemName = foodItemName;
//        this.stepImageArray = stepImageArray;
//        this.stepDescriptionArray = stepDescriptionArray;
//        this.currentStep = 1;
//    }

    public FoodItem(int foodItemNumber) {
        int numberOfStep = getNumberOfSteps(foodItemNumber);
        this.foodItemName = getFoodItemName(foodItemNumber);
        this.numberOfSteps = numberOfStep;
        this.stepImageArray = getStepImageArray(foodItemNumber, numberOfStep);
        this.currentStep = 1;
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

    public String getCurrentStepImage(){
        return this.stepImageArray[this.currentStep - 1];
    }

    public String[] getStepImageArray(){
        return this.stepImageArray;
    }

    public String[] getStepDescriptionArray() {
        return this.stepDescriptionArray;
    }

    public Boolean nextStep() {
        Boolean success = false;
        if(this.currentStep != this.numberOfSteps){
            this.currentStep++;
            success = true;
        }
        return success;
    }

    public Boolean previousStep() {
        Boolean success = false;
        if(this.currentStep != 1){
            this.currentStep--;
            success = true;
        }
        return success;
    }

    public static String getFoodItemName(int foodItemNumber) {
        String foodItemName = "";
        if (foodItemNumber != 0) {
            switch (foodItemNumber) {
                case 1:
                    foodItemName = "Triple Steak Stack";
                    break;
                case 2:
                    foodItemName = "Chicken Triple Steak Stack";
                    break;
                case 3:
                    foodItemName = "Cinnabon Coffee";
                    break;
                case 4:
                    foodItemName = "Iced Coffee";
                    break;
                case 5:
                    foodItemName = "Cheesy Burritos";
                    break;
                default:
                    break;
            }
        }
        else {
            foodItemName = "Invalid food item number";
        }
        return foodItemName;
    }

    public static int getNumberOfSteps(int foodItemNumber) {
        int numberOfSteps = 0;
        if (foodItemNumber != 0) {
            switch (foodItemNumber) {
                case 1:
                    numberOfSteps = 9;
                    break;
                case 2:
                    numberOfSteps = 5;
                    break;
                case 3:
                    numberOfSteps = 5;
                    break;
                case 4:
                    numberOfSteps = 5;
                    break;
                case 5:
                    numberOfSteps = 9;
                    break;
                default:
                    break;
            }
        }
        return numberOfSteps;
    }

    public static String[] getStepImageArray(int foodItemNumber, int numberOfSteps) {
        String[] stepImageArray = new String[numberOfSteps];
        if(foodItemNumber != 0) {
            for (int i = 0; i < numberOfSteps; i++) {
                stepImageArray[i] = "training" + String.valueOf(foodItemNumber) + "_" + String.valueOf(i + 1);
            }
        }
        return stepImageArray;
    }
}
