package br.ufmg.dcc.recsys;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ItemToItemPredictor implements Predictor {

    private Matrix userItemMatrix;
    private int kNearestItems;
    private double[][] itemSimilarities;
    private double[] averageRatings;

    public ItemToItemPredictor(Matrix userItemMatrix, int kNearestItems) {
        this.userItemMatrix = userItemMatrix;
        this.kNearestItems = kNearestItems;
        this.itemSimilarities = createItemItemSimilaritiesMatrix(userItemMatrix);
        this.averageRatings = createAverageRatingsMatrix(userItemMatrix);
    }
    
    private double[][] createItemItemSimilaritiesMatrix(Matrix userItemMatrix) {
        double[][] similarities = new double[userItemMatrix.numCols()][userItemMatrix.numCols()];
        for(int item1 = 0; item1 < userItemMatrix.numCols(); item1++) {
            for(int item2 = 0; item2 < userItemMatrix.numCols(); item2++) {
                similarities[item1][item2] = userItemMatrix.collumnCosineSimilarity(item1, item2);
                if(Double.isNaN(similarities[item1][item2])) {
                    similarities[item1][item2] = 0d;
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
    
    static class Item {
        int id;
        double similarity;
        double userRating;
        Item(int id, double similarity, double userRating) {
            this.id = id;
            this.similarity = similarity;
            this.userRating = userRating;
        }
        public static int similarityComparator(Item i1, Item i2) {
            return Double.compare(i1.similarity,  i2.similarity)*-1;
        }
        @Override
        public String toString() {
            return "item(id:"+id+", similarity:"+similarity+", user_r:"+userRating+")";
        }
    }

    @Override
    public double predict(int item, int user) {
        
        List<Item> ratedItems = IntStream
              .range(0, userItemMatrix.numCols())
              .filter(i -> i != item)
              .filter(i -> userItemMatrix.value(user, i) != 0d)
              .mapToObj(i -> new Item(i, itemSimilarities[item][i], userItemMatrix.value(user, i)))
//              .map(i -> {System.out.println(i); return i;})
              .sorted(Item::similarityComparator)
              .limit(kNearestItems)
              .collect(Collectors.toList());
        
        double ratingsSum = 0d;
        double totalSimilarities = 0d;
        for (Item i : ratedItems) {
            ratingsSum += i.similarity * i.userRating;
            totalSimilarities += i.similarity;
//            if(Double.isNaN(ratingsSum)) {
//                System.err.println("ratingsSum="+ratingsSum+" += i.similarity="+i.similarity+" * i.userRating="+i.userRating);
//                System.err.println("totalSimilarities="+totalSimilarities);
//                throw new IllegalStateException("Invalid ratingsSum found! value=NaN");
//            }
//            if(Double.isNaN(totalSimilarities)) {
//                System.err.println("ratingsSum="+ratingsSum+" += i.similarity="+i.similarity+" * i.userRating="+i.userRating);
//                System.err.println("totalSimilarities="+totalSimilarities);
//                throw new IllegalStateException("Invalid totalSimilarity found! value=NaN");
//            }
        }
        
        
        double score;
        if(totalSimilarities == 0) {
            score = averageRatings[user];
        } else {
            score = ratingsSum / totalSimilarities;
        }
        
        if(Double.isNaN(score)) {
            throw new IllegalStateException("Invalid prediction. value=NaN");
        }
        return score;
    }

}
