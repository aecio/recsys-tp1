package br.ufmg.dcc.recsys;

import java.util.List;
import java.util.Map;

public class MeanAveratePrecisionEvaluator {
    
    private Matrix userItemMatrix;
    private int k;
    private double[] totalRelevants;

    public MeanAveratePrecisionEvaluator(Matrix userItemMatrix, int k) {
        this.userItemMatrix = userItemMatrix;
        this.k = k;
        this.totalRelevants = new double[userItemMatrix.numRows()];
        for(int u = 0; u < userItemMatrix.numRows(); u++) {
            for(int i = 0; i < userItemMatrix.numCols(); i++) {
                if(userItemMatrix.value(u, i) >= 4d) {
                    totalRelevants[u]++;
                }
            }
        }
    }

    public double averagePrecision(List<Integer> recommendations, int user) {
        double precision = 0d;
        int relevantCount = 0;
        double position = 0;
        for(int i = 0; i < k && i < recommendations.size(); i++) {
            position++;
            try {
                if(userItemMatrix.value(user, recommendations.get(i)) >= 4d) {
                    relevantCount++;
                    precision += relevantCount / position;
                }
            }catch(IndexOutOfBoundsException e) {
                // yeah, its empty on purpose
                // the test matrix may not contain all ratings for all itens and users
                // when this happens, we consider it is not relevant
            }
        }
        if(totalRelevants[user] == 0)
            return 0;
        else
            return precision/totalRelevants[user];
    }

    public double meanAveragePrecision(Map<Integer, List<Integer>> recommendationsList) {
        double averagePrecision = 0d;
        for (Map.Entry<Integer, List<Integer>> list : recommendationsList.entrySet()) {
            averagePrecision += averagePrecision(list.getValue(), list.getKey());
        }
        return averagePrecision/recommendationsList.size();
    }

}
