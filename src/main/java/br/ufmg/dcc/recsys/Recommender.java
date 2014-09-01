package br.ufmg.dcc.recsys;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Recommender {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("Invalid parameters.");
        }
        
        String usersFile = args[0];
        String itemsFile = args[1];
        String ratingsFile = args[2];


//        List<String> lines = Files.lines(Paths.get(usersFile))
//                .map((line) -> line.split("\\|")[0])
//                .collect(Collectors.toList());
//        
//        
//        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
//        decoder.onMalformedInput(CodingErrorAction.IGNORE);
//        InputStream is = new FileInputStream(new File(itemsFile));
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is, decoder));
//        
//        List<String> lines = reader.lines()
//                .map((line) -> line.split("\\|")[0])
//                .map((line) -> { System.out.println(line);  return line;})
//                .collect(Collectors.toList());
//        reader.close();
        
        Matrix ratingsMatrix = readRatingsMatrix(ratingsFile);
        
        UserToUserPredictor userToUserPredictor = new UserToUserPredictor(ratingsMatrix, 20);
        TopkRecommender userToUserRecommender = new TopkRecommender(ratingsMatrix, userToUserPredictor, 100);
        
        int numUsers = ratingsMatrix.numRows();
        
        IntStream.range(0, numUsers)
                .mapToObj((u) -> userToUserRecommender.recommend(u))
                .flatMap((recs) -> recs.stream() )
                .forEach((p) -> System.out.println((p.user+1) +" "+ (p.item+1) +" "+ p.score) );
        
    }

    static class Rating {
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
    
    private static Matrix readRatingsMatrix(String ratingsFile) {
        try {
            List<Rating> ratings = Files.lines(Paths.get(ratingsFile))
                    .map((line) -> {
                        String[] t = line.split("\\t");
                        return new Rating(Integer.valueOf(t[0]), Integer.valueOf(t[1]), Integer.valueOf(t[2]), Integer.valueOf(t[3]));
                    })
//                    .limit(1000)
                   .collect(Collectors.toList());
            
            
            int numberOfUsers = ratings.stream().mapToInt((r)->r.userId).max().getAsInt();
            int numberOfItems = ratings.stream().mapToInt((r)->r.itemId).max().getAsInt();
            
            double matrix[][] = new double[numberOfUsers][numberOfItems];
            
            ratings.forEach( (r) -> {
              matrix[r.userId-1][r.itemId-1] = r.rating;  
            });
            
            return new Matrix(matrix);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ratings file: "+ratingsFile);
        }
    }
}
