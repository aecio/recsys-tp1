package br.ufmg.dcc.recsys;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class ItemToItemPredictorTest {

    @Test
    public void testPredict() {
        // given
        final double nan = 0;
        double[][] d = {
            { 5, 3, 4, 4,nan}, //alice
            { 3, 1, 2, 3, 3 }, //user1
            { 4, 3, 4, 3, 5 }, //user2
            { 3, 3, 1, 5, 4 }, //user3
            { 1, 5, 5, 2, 1 }  //user4
        };
        Matrix D = new Matrix(d);
        
        // when
        final double prediction = new ItemToItemPredictor(D, 2).predict(4, 0);
        
        // then
        assertThat(prediction, closeTo(4.51d, 0.1));
    }
    
    @Test
    public void testPredictWithNullCoRatedItems() {
        // given
        final double nan = 0;
        double[][] d = {
                { 5, 3,nan,nan,nan}, //alice
                {nan,nan, 2, 3, 3 }, //user1
                {nan,nan, 4, 3, 5 }, //user2
                {nan,nan, 1, 5, 4 }, //user3
                {nan,nan, 5, 2, 1 }  //user4
            };
        Matrix D = new Matrix(d);
        
        // when
        final double prediction = new ItemToItemPredictor(D, 2).predict(4, 0);
        
        // then
        assertThat(Double.isNaN(prediction), is(false));
    }

}
