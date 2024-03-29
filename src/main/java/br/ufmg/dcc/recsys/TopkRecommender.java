package br.ufmg.dcc.recsys;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class TopkRecommender {

    private Predictor predictor;
    private int numberOfRecommendations;
    private Matrix userItemMatrix;

    public TopkRecommender(Matrix userItemMatrix, Predictor predictor, int numberOfRecommendations) {
        this.userItemMatrix = userItemMatrix;
        this.predictor = predictor;
        this.numberOfRecommendations = numberOfRecommendations;
    }

    public static class Prediction {
        private int item;
        double score;
        int user;
        public Prediction(int user, int item, double score) {
            this.user = user;
            this.item = item;
            this.score = score;
        }
        public static int compare(Prediction i1, Prediction i2) {
            return Double.compare(i1.score, i2.score)*-1;
        }
        public int getItem() {
            return item;
        }
        public int getUser() {
            return user;
        }
        @Override
        public String toString() {
            return "Prediction [item=" + item + ", score=" + score + "]";
        }
    }
    
    public List<Prediction> recommend(int user) {
        int numItems = userItemMatrix.numCols();
        return IntStream.range(0, numItems)
                .filter( (i) -> userItemMatrix.value(user, i) == 0d)
//                .map((obj) -> { System.err.println("filter: "+obj); return obj;})
                .mapToObj((i) -> new Prediction(user, i, predictor.predict(i, user)))
                .map((obj) -> {
//                    System.err.println("pred: "+obj);
                    if(Double.isNaN(obj.score)) {
                        throw new IllegalStateException("Invalid score=NaN! "+obj);
                    }
                    return obj;
                })
                .sorted(Prediction::compare)
                .limit(numberOfRecommendations)
                .collect(Collectors.toList());
    }

}
