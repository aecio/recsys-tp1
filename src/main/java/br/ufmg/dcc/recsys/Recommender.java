package br.ufmg.dcc.recsys;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import br.ufmg.dcc.recsys.ItemToItemPredictor.Similarity;
import br.ufmg.dcc.recsys.TopkRecommender.Prediction;

public class Recommender {
    
    private static final String MODEL_FILE = "./item-to-item.model";
    
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
        
        public int getUserId() {
            return userId;
        }
        
        public int getItemId() {
            return itemId;
        }
    }
    
    @SuppressWarnings("unused")
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: java -jar tp2.jar Recommender u.user u.item ua.base");
            System.exit(1);
        }
        
        String option = args[0];
        String usersFile = args[1];
        String itemsFile = args[2];
        String ratingsFile = args[3];

        Matrix ratingsMatrix = readRatingsMatrix(ratingsFile);
        
        if(option.equalsIgnoreCase("Indexer")) {
            System.err.println("Training Item-Item Colaborative Filtering model...");
            ItemToItemPredictor predictor = new ItemToItemPredictor(ratingsMatrix, 20, Similarity.ADJUSTED_COSINE);
            predictor.writeToFile(MODEL_FILE);
            System.err.println("Model file written in:" + MODEL_FILE);
        }
        
        if(option.equalsIgnoreCase("Recommender")) {
            System.err.println("Training Item-Item Colaborative Filtering model...");
            ItemToItemPredictor predictor = ItemToItemPredictor.fromFile(MODEL_FILE);
            TopkRecommender recommender = new TopkRecommender(ratingsMatrix, predictor, 100);
            recomendUserToUser(ratingsMatrix, recommender, System.out);
        }
        
//        if(option.equalsIgnoreCase("Evaluator")) {
//            String groundThruthFile = args[1];
//            String outputRecomendationsFile = args[2];
//            ItemToItemPredictor predictor = ItemToItemPredictor.fromFile(MODEL_FILE);
//            TopkRecommender recommender = new TopkRecommender(ratingsMatrix, predictor, 100);
//            recomendUserToUser(ratingsMatrix, recommender, new PrintStream(outputRecomendationsFile));
//        }
        
        if(option.equalsIgnoreCase("Evaluation")) {
            String testFile = args[4];
            evaluatePredictors(testFile, ratingsMatrix);
        }
    }

    private static void recomendUserToUser(Matrix ratingsMatrix,
                                           TopkRecommender recommender,
                                           PrintStream out) {
        int numUsers = ratingsMatrix.numRows();
        IntStream.range(0, numUsers)
            .mapToObj( u -> recommender.recommend(u))
            .flatMap( recs -> recs.stream() )
            .forEach( p -> out.println((p.user+1) +" "+ (p.getItem()+1) +" "+ p.score) );
    }

    private static void evaluatePredictors(String testFile, Matrix ratingsMatrix) {
        if(testFile != null && !testFile.isEmpty()) {

            int[] valuesOfK = {1, 3, 5, 10, 25, 50, 75, 100, 125, 150, 200, 225, 250};
            
            System.out.println("k, MAP user-to-user, RMSE user-to-user");
            IntStream.of(valuesOfK).forEach( k -> {
                UserToUserPredictor userUserPredictor = new UserToUserPredictor(ratingsMatrix, k);
                TopkRecommender recommender = new TopkRecommender(ratingsMatrix, userUserPredictor, 100);
                double rmse = calculateRmse(testFile, userUserPredictor);
                double map10 = calculateMap(testFile, recommender);
                System.out.println( k + ", " + map10 + ", " + rmse );
            });
            
            System.out.println("k, MAP item-to-item, RMSE item-to-item");
            IntStream.of(valuesOfK).forEach( k -> {
                ItemToItemPredictor itemItemPredictor = new ItemToItemPredictor(ratingsMatrix, k);
                TopkRecommender recommender = new TopkRecommender(ratingsMatrix, itemItemPredictor, 100);
                double rmse = calculateRmse(testFile, itemItemPredictor);
                double map = calculateMap(testFile, recommender);
                System.out.println( k + ", " + map + ", " + rmse );
            });
            
            System.out.println("k, MAP item-to-item adjusted-cosine, RMSE item-to-item adjusted-cosine");
            IntStream.of(valuesOfK).forEach( k -> {
                ItemToItemPredictor itemItemPredictor = new ItemToItemPredictor(ratingsMatrix, k, Similarity.ADJUSTED_COSINE);
                TopkRecommender recommender = new TopkRecommender(ratingsMatrix, itemItemPredictor, 100);
                double rmse = calculateRmse(testFile, itemItemPredictor);
                double map = calculateMap(testFile, recommender);
                System.out.println( k + ", " + map + ", " + rmse );
            });
            
            
            TopkRecommender recommender = new TopkRecommender(
                    ratingsMatrix, new AverageItemRatingPredictor(ratingsMatrix), 100);
            
            double map = calculateMap(testFile, recommender);
            double rmse = calculateRmse(testFile, new AverageItemRatingPredictor(ratingsMatrix));
            
            System.out.println("MAP avg-item-rating, RMSE avg-item-rating");
            System.out.println(map + "," + rmse); 
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
            
//            System.out.println("Loaded matrix from file ["+ratingsFile+"] of size ["
//                  + numberOfUsers + "," + numberOfItems + "]");
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
//                        System.err.println(p);
                        return Math.pow(p - r.rating, 2);
                    })
                    .average();
            
            return Math.sqrt(squaredError.getAsDouble());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to run evaluation.", e);
        }
    }
    
    private static double calculateMap(String testFile, TopkRecommender recommender) {
        
        Matrix testRatingsMatrix = readRatingsMatrix(testFile);
        try {
            int numUsers = testRatingsMatrix.numRows();
            
            Map<Integer, List<Integer>> recommendationsByUser = new HashMap<>();
            for(int user = 0; user < numUsers; user++) {
                List<Integer> recommendations = recommender.recommend(user).stream()
                        .map(Prediction::getItem)
                        .collect(Collectors.toList());
                recommendationsByUser.put(user, recommendations);
            }
            
            MeanAveratePrecisionEvaluator map = new MeanAveratePrecisionEvaluator(testRatingsMatrix, 100);
            return map.meanAveragePrecision(recommendationsByUser);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to run MAP evaluation.", e);
        }
    }
    
}
