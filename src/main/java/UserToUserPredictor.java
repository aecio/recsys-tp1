import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



public class UserToUserPredictor {
    
    private Matrix userItemMatrix;
    private double[][] userSimilarities;
    private double[] averageRatings;
    private int kNearestUsers;

    public UserToUserPredictor(Matrix userItemMatrix, int kNearestUsers) {
        this.userItemMatrix = userItemMatrix;
        this.kNearestUsers = kNearestUsers;
        this.userSimilarities = createUserSimilaritiesMatrix(userItemMatrix);
        this.averageRatings = createAverageRatingsMatrix(userItemMatrix);
//        new Matrix(userSimilarities).show();
    }

    private double[][] createUserSimilaritiesMatrix(Matrix userItemMatrix) {
        double similarities[][] = new double[userItemMatrix.numRows()][userItemMatrix.numRows()];
        for(int userA = 0; userA < userItemMatrix.numRows(); userA++) {
            for(int userB = 0; userB < userItemMatrix.numRows(); userB++) {
//                if(userA > userB) {
                    similarities[userA][userB] = userItemMatrix.pearsonCorrelationSimilarity(userA, userB);
//                }
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
            if(u1.similarity > u2.similarity) return -1;
            if(u1.similarity < u2.similarity) return 1;
            else return 0;
        }
        @Override
        public String toString() {
            return "user(id:"+id+", sim:"+similarity+", avg_r:"+avgRating+")";
        }
    }
    
    public double predict(int item, int user) {
        // get k nearest users, as b, from user a
//        List<User> users = new ArrayList<UserToUserPredictor.User>(userSimilarities.length);
//        for(int i = 0; i < userItemMatrix.numRows(); i++) {
//            if(i != user)
//                users.add(new User(i, userSimilarities[user][i], averageRatings[i]));
//        }
//        users.sort(User::similarityComparator);
        
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
            System.err.println("u.similarity="+u.similarity +" * p("+u.id+", "+item+")="+ userItemMatrix.value(u.id, item) +" - u.avgRating=" + u.avgRating);
            System.err.println("similarities_sum="+similaritiesSum);
        }
        
        System.err.println("averageRatings[user]="+averageRatings[user]+ " + numerator="+partialPredictions+" / similaritiesSum="+similaritiesSum);
        return averageRatings[user] + partialPredictions/similaritiesSum;
    }

}
