package br.ufmg.dcc.recsys;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("serial")
public class ItemToItemPredictor implements Predictor, Serializable {

    private Matrix userItemMatrix;
    private int kNearestItems;
    private double[][] itemSimilarities;
    private double[] averageRatings;
    private List<List<Item>> ratedItemsByUser;
    private Similarity similarity;
    
    public enum Similarity {
        COSINE, ADJUSTED_COSINE
    }

    public ItemToItemPredictor(Matrix userItemMatrix, int kNearestItems) {
        this(userItemMatrix, kNearestItems, Similarity.COSINE);
    }
    
    public ItemToItemPredictor(Matrix userItemMatrix, int kNearestItems, Similarity similarity) {
        this.userItemMatrix = userItemMatrix;
        this.kNearestItems = kNearestItems;
        this.similarity = similarity;
        this.itemSimilarities = createItemItemSimilaritiesMatrix(userItemMatrix);
        this.averageRatings = createAverageRatingsMatrix(userItemMatrix);
        this.ratedItemsByUser = new ArrayList<List<Item>>();
        for(int user = 0; user < userItemMatrix.numRows(); user++) {
            this.ratedItemsByUser.add(getItemsRatedByUser(user));
        }
    }
    
    private double[][] createItemItemSimilaritiesMatrix(Matrix userItemMatrix) {
        double[][] similarities = new double[userItemMatrix.numCols()][userItemMatrix.numCols()];
        for(int item1 = 0; item1 < userItemMatrix.numCols(); item1++) {
            for(int item2 = 0; item2 < userItemMatrix.numCols(); item2++) {
                if(item1 < item2) {
                    if(similarity == Similarity.COSINE) {
                        similarities[item1][item2] = userItemMatrix.collumnCosineSimilarity(item1, item2);
                    } else {
                        similarities[item1][item2] = userItemMatrix.collumnAdjustedCosineSimilarity(item1, item2);
                    }
                    if(Double.isNaN(similarities[item1][item2])) {
                        similarities[item1][item2] = 0d;
                    }
                    similarities[item2][item1] = similarities[item1][item2];
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
    
    private List<Item> getItemsRatedByUser(int user) {
        return IntStream
              .range(0, userItemMatrix.numCols())
              .filter(i -> userItemMatrix.value(user, i) != 0d)
              .mapToObj(i -> new Item(i, 0d, userItemMatrix.value(user, i)))
              .collect(Collectors.toList());
    }

    static class Item implements Serializable {
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
        
        List<Item> ratedItems = ratedItemsByUser.get(user).stream()
            .filter(i -> i.id != item)
            .map(i -> new Item(i.id, itemSimilarities[item][i.id], userItemMatrix.value(user, i.id)))
            .sorted(Item::similarityComparator)
//            .filter(i -> i.similarity > 0d)
            .limit(kNearestItems)
            .collect(Collectors.toList());
        
        
        double ratingsSum = 0d;
        double totalSimilarities = 0d;
        for (Item i : ratedItems) {
            ratingsSum += i.similarity * i.userRating;
            totalSimilarities += Math.abs(i.similarity);
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

    public void writeToFile(String modelFile) {
        try {
            FileOutputStream fileOut = new FileOutputStream(modelFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save model to file: "+modelFile, e);
        }
    }

    public static ItemToItemPredictor fromFile(String modelFile) {
        ItemToItemPredictor predictor;
        try {
            FileInputStream fileIn = new FileInputStream(modelFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            predictor = (ItemToItemPredictor) in.readObject();
            in.close();
            fileIn.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load model from file: "+modelFile, e);
        }        
        return predictor;
    }
    
}
