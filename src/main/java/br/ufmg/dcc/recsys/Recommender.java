package br.ufmg.dcc.recsys;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Recommender {
    
    public static class Rating {
        int userId;
        int itemId;
        int rating;
        int timestamp;

        public Rating(int userId, int itemId, int rating, int timestamp) {
            this.userId = userId;
            this.itemId = itemId;
            this.rating = rating;
            this.timestamp = timestamp;
        }
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: java -jar tp1.jar Recommender u.user u.item ua.base");
            System.exit(1);
        }
        
        String option = args[0];
        String ratingsFile = args[3];
        String testFile = args[4];

        Matrix ratingsMatrix = readRatingsMatrix(ratingsFile);
        
        if(option.equalsIgnoreCase("Recommender")) {
            recomendUserToUser(ratingsMatrix);
        }
        
        if(option.equalsIgnoreCase("Evaluation")) {
            evaluatePredictors(testFile, ratingsMatrix);
        }
    }

    private static void recomendUserToUser(Matrix ratingsMatrix) {
        UserToUserPredictor predictor = new UserToUserPredictor(ratingsMatrix, 20);
        TopkRecommender recommender = new TopkRecommender(ratingsMatrix, predictor, 100);
        
        int numUsers = ratingsMatrix.numRows();
        
        IntStream.range(0, numUsers)
            .mapToObj( u -> recommender.recommend(u))
            .flatMap( recs -> recs.stream() )
            .forEach( p -> System.out.println((p.user+1) +" "+ (p.item+1) +" "+ p.score) );
    }

    private static void evaluatePredictors(String testFile, Matrix ratingsMatrix) {
        if(testFile != null && !testFile.isEmpty()) {

            IntStream.of(1, 3, 5, 10, 25, 50, 75, 100, 125, 150).forEach( k -> {
                double rmse;
                
                UserToUserPredictor userUserPredictor = new UserToUserPredictor(ratingsMatrix, k);
                rmse = calculateRmse(testFile, userUserPredictor);
                System.out.println("RMSE user-to-user ("+k+"): "+rmse);
                
                ItemToItemPredictor itemItemPredictor = new ItemToItemPredictor(ratingsMatrix, k);
                rmse = calculateRmse(testFile, itemItemPredictor);
                System.out.println("RMSE item-to-item ("+k+"): "+rmse);
            });
            
            double rmse = calculateRmse(testFile, new AverageItemRatingPredictor(ratingsMatrix));
            System.out.println("RMSE avg-item-rating: "+rmse);
            System.out.flush();
        }
    }
    
    private static Matrix readRatingsMatrix(String ratingsFile) {
        try {
            List<Rating> ratings = Files.lines(Paths.get(ratingsFile))
                    .map( line -> {
                        String[] t = line.split("\\t");
                        return new Rating(Integer.valueOf(t[0]),
                                          Integer.valueOf(t[1]),
                                          Integer.valueOf(t[2]),
                                          Integer.valueOf(t[3]));
                    })
                    .collect(Collectors.toList());
            
            int numberOfUsers = ratings.stream().mapToInt(r->r.userId).max().getAsInt();
            int numberOfItems = ratings.stream().mapToInt(r->r.itemId).max().getAsInt();
            
            double matrix[][] = new double[numberOfUsers][numberOfItems];
            
            ratings.forEach( r -> {
                matrix[r.userId-1][r.itemId-1] = r.rating;  
            });
            
            return new Matrix(matrix);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ratings file: "+ratingsFile);
        }
    }

    private static double calculateRmse(String testFile, Predictor predictor) {
        try {
            OptionalDouble squaredError = Files.lines(Paths.get(testFile))
                    .map( line -> {
                        String[] t = line.split("\\t");
                        return new Rating(Integer.valueOf(t[0]),
                                          Integer.valueOf(t[1]),
                                          Integer.valueOf(t[2]),
                                          Integer.valueOf(t[3]));
                    })
                    .mapToDouble( r -> {
                        double p = predictor.predict(r.itemId-1, r.userId-1);
                        return Math.pow(p - r.rating, 2);
                    })
                    .average();
            
            return Math.sqrt(squaredError.getAsDouble());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to run evaluation.", e);
        }
    }
    
}
