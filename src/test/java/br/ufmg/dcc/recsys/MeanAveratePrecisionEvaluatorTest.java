package br.ufmg.dcc.recsys;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class MeanAveratePrecisionEvaluatorTest {

    @Test
    public void testAveragePrecision1() {
        // given
        double[][] d = {
            { 5, 3, 4, 4, 4, 2 }, //alice
            { 3, 1, 2, 3, 3, 3 }, //user1
            { 4, 3, 4, 3, 5, 5 }, //user2
            { 3, 3, 1, 5, 4, 2 }, //user3
            { 1, 5, 5, 2, 1, 5 }  //user4
        };
        Matrix D = new Matrix(d);
        
        // relevantes: 0, 2, 3, 4
        int aliceUser = 0;
        List<Integer> recommendedItems  = Arrays.asList(
            0, // 1/1
            1, // 
            3  // 2/3
        );
        // missgin 3, 4
        int  k = 3;
                
        MeanAveratePrecisionEvaluator mapEvaluator = new MeanAveratePrecisionEvaluator(D, k);
        
        // when
        double averagePrecision = mapEvaluator.averagePrecision(recommendedItems, aliceUser );
        
        // then
        assertThat(averagePrecision, is( ((1/1d)+(2/3d))/4d) );
    }
    
    @Test
    public void testAveragePrecision2() {
        double[][] d = {
            { 5, 3, 4, 4, 4, 2 }, //alice
            { 3, 1, 2, 3, 3, 3 }, //user1
            { 4, 3, 4, 3, 5, 5 }, //user2
            { 3, 3, 1, 5, 4, 2 }, //user3
            { 1, 5, 5, 2, 1, 5 }  //user4
        };
        Matrix D = new Matrix(d);
        // relevantes: 0, 2, 3, 4
        // ttotal relevantes = 4
        int aliceUser = 0;
        List<Integer> recommendedItems  = Arrays.asList(
            0, // 1/1
            1, // x
            3, // 2/3
            2, // 3/4
            5  // 
        );
        int  k = 5;
                
        MeanAveratePrecisionEvaluator mapEvaluator = new MeanAveratePrecisionEvaluator(D, k);
        
        // when
        double averagePrecision = mapEvaluator.averagePrecision(recommendedItems, aliceUser );
        
        // then
        assertThat(averagePrecision, is( ((1/1d)+(2/3d)+(3/4d))/4d) );
    }
    
    @Test
    public void testMeanAveragePrecision2() {
        double[][] d = {
            { 5, 3, 4, 4, 4, 2 }, //alice
            { 3, 1, 5, 3, 3, 3 }, //user1
            { 4, 3, 4, 3, 5, 5 }, //user2
            { 3, 3, 1, 5, 4, 2 }, //user3
            { 1, 5, 5, 2, 1, 5 }  //user4
        };
        Matrix D = new Matrix(d);
        
        int aliceId = 0;
        List<Integer> recommendedItemsAlice  = Arrays.asList(
            0, // 1/1
            1, // 1/2
            3  // 2/3
        );
        final double aliceAvgPrecision = ((1/1d)+(2/3d))/4d;
        
        
        int user1Id =1;
        List<Integer> recommendedItemsUser1  = Arrays.asList(
            0, // x
            1, // x
            2  // 1/3
        );
        final double user1AvgPrecision =  (1/3d) / 1d;
        
        int k = 3;
                
        MeanAveratePrecisionEvaluator mapEvaluator = new MeanAveratePrecisionEvaluator(D, k);
        
        Map<Integer, List<Integer>> recommendationsList = new HashMap<Integer, List<Integer>>();
        recommendationsList.put(aliceId, recommendedItemsAlice);
        recommendationsList.put(user1Id, recommendedItemsUser1);
        
        // when
        double averagePrecision = mapEvaluator.meanAveragePrecision(recommendationsList);
        
        // then
        assertThat(averagePrecision, is( (aliceAvgPrecision+user1AvgPrecision)/2d) );
    }
    
}
