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
                .filter(i -> (i != user))
                .mapToObj(i -> new User(i, userSimilarities[user][i], averageRatings[i]))
                .sorted(User::similarityComparator)
                .limit(kNearestUsers)
//                .map((u) -> { System.err.println(u); return u; })
                .collect(Collectors.toList());
        
        // for each user, compute predictions
        double partialPredictions = 0d;
        double similaritiesSum = 0d;
        
        for(User u : similarUsers) {
            partialPredictions += u.similarity * (userItemMatrix.value(u.id, item) - u.avgRating);
            similaritiesSum += u.similarity;
//            System.err.println("u.similarity="+u.similarity +" * p("+u.id+", "+item+")="+ userItemMatrix.value(u.id, item) +" - u.avgRating=" + u.avgRating);
//            System.err.println("similarities_sum="+similaritiesSum);
        }
        
//        System.err.println("averageRatings[user]="+averageRatings[user]+ " + numerator="+partialPredictions+" / similaritiesSum="+similaritiesSum);
        final double score = averageRatings[user] + partialPredictions/similaritiesSum;
        if(Double.isNaN(score)) {
            throw new IllegalStateException("Invalid prediction! prediction=NaN");
        }
        return score;
    }

}
