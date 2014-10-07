package br.ufmg.dcc.recsys;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class UserToUserPredictor implements Predictor {
    
    private Matrix userItemMatrix;
    private double[][] userSimilarities;
    private double[] averageRatings;
    private int kNearestUsers;

    public UserToUserPredictor(Matrix userItemMatrix, int kNearestUsers) {
        this.userItemMatrix = userItemMatrix;
        this.kNearestUsers = kNearestUsers;
        this.userSimilarities = createUserSimilaritiesMatrix(userItemMatrix);
        this.averageRatings = createAverageRatingsMatrix(userItemMatrix);
    }

    private double[][] createUserSimilaritiesMatrix(Matrix userItemMatrix) {
        double similarities[][] = new double[userItemMatrix.numRows()][userItemMatrix.numRows()];
        for(int userA = 0; userA < userItemMatrix.numRows(); userA++) {
            for(int userB = 0; userB < userItemMatrix.numRows(); userB++) {
                similarities[userA][userB] = userItemMatrix.pearsonCorrelationSimilarity(userA, userB);
                if(Double.isNaN(similarities[userA][userB])) {
                    similarities[userA][userB] = 0;
                }                    
            }
        }
        return similarities;
    }
    
    private double[] createAverageRatingsMatrix(Matrix userItemMatrix2) {
        double averageRatings[] = new double[userItemMatrix.numRows()];
        for(int user = 0; user < userItemMatrix.numRows(); user++) {
            averageRatings[user] = userItemMatrix.lineAvgNonZero(user);
        }
        return averageRatings;
    }

    static class User {
        int id;
        double similarity;
        double avgRating;
        User(int id, double similarity, double avgRating) {
            this.id = id;
            this.similarity = similarity;
            this.avgRating= avgRating;
        }
        public static int similarityComparator(User u1, User u2) {
            return Double.compare(u1.similarity,  u2.similarity)*-1;
        }
        @Override
        public String toString() {
            return "user(id:"+id+", sim:"+similarity+", avg_r:"+avgRating+")";
        }
    }
    
    public double predict(int item, int user) {
        // get k nearest users, as b, from user a
        List<User> similarUsers = IntStream
                .range(0, userItemMatrix.numRows())
                .filter(u -> u != user)
                .filter(u -> userItemMatrix.value(u, item) != 0d)
                .mapToObj(u -> new User(u, userSimilarities[user][u], averageRatings[u]))
                .sorted(User::similarityComparator)
                .limit(kNearestUsers)
                .collect(Collectors.toList());
        
        double partialPredictions = 0d;
        double similaritiesSum = 0d;
        for(User u : similarUsers) {
            final double userRating = userItemMatrix.value(u.id, item);
            if(userRating != 0d) {
                partialPredictions += u.similarity * (userRating - u.avgRating);
                similaritiesSum += Math.abs(u.similarity);
            }
        }
        
        double score;
        if(similaritiesSum == 0) {
            score = averageRatings[user];
        } else {
            score = averageRatings[user] + (partialPredictions/similaritiesSum);
        }
        
        if(Double.isNaN(score)) {
            throw new IllegalStateException("Invalid prediction! prediction=NaN");
        }
        
        return score;
    }

}
