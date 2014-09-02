package br.ufmg.dcc.recsys;

public class AverageItemRatingPredictor implements Predictor {
    
    private double[] avgRatings;

    public AverageItemRatingPredictor(Matrix userItemMatrix) {
        avgRatings = new double[userItemMatrix.numCols()];
        for(int item = 0; item < userItemMatrix.numCols(); item++) {
            avgRatings[item] = userItemMatrix.columnAvgNonZero(item);
            if(Double.isNaN(avgRatings[item])) {
                avgRatings[item] = 2.5; // middle of rating scale
            }                    
        }
    }

    @Override
    public double predict(int item, int user) {
        return avgRatings[item];
    }

}
