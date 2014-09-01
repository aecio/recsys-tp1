package br.ufmg.dcc.recsys;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import br.ufmg.dcc.recsys.Matrix;
import br.ufmg.dcc.recsys.Predictor;
import br.ufmg.dcc.recsys.TopkRecommender;
import br.ufmg.dcc.recsys.UserToUserPredictor;

public class TopkRecommenderTest {

    @Test
    public void testRecommend() {
        // given
        
        final double nan = 0;
//        double[][] d = {
//            { 5, 3, 4, 4,nan}, //alice
//            { 3, 1, 2, 3, 3 }, //user1
//            { 4, 3, 4, 3, 5 }, //user2
//            { 3, 3, 1, 5, 4 }, //user3
//            { 1, 5, 5, 2, 1 }  //user4
//        };
        
        double[][] d = {
                { 5, 3, 4, 4,nan, nan}, //alice
                { 3, 1, 2, 3, 3, 4 }, //user1
                { 4, 3, 4, 3, 5, 4 }, //user2
                { 3, 3, 1, 5, 4, 2 }, //user3
                { 1, 5, 5, 2, 1, 5 }  //user4
            };
        
        Matrix userItemMatrix = new Matrix(d);
        Predictor predictor = new UserToUserPredictor(userItemMatrix, 2);
        final int numberOfRecommendations = 3;
        TopkRecommender topkRecommender = new TopkRecommender(userItemMatrix, predictor, numberOfRecommendations);
        
        int user = 0;
        
        // when 
        List<TopkRecommender.Prediction> recommendations = topkRecommender.recommend(user);
        
        // then
        assertThat(recommendations, is(notNullValue()));
        assertThat(recommendations.size(), is(2));
    }

}
