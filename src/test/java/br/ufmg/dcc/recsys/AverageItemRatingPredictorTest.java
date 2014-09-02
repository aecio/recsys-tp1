package br.ufmg.dcc.recsys;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class AverageItemRatingPredictorTest {

    @Test
    public void testPredict() {
        // given
        double[][] d = {
            { 1, 1, 2 },
            { 1, 2, 3 },
            { 1, 3, 0}
        };
        Matrix D = new Matrix(d);
        
        // when
        AverageItemRatingPredictor predictor = new AverageItemRatingPredictor(D);
        
        //then
        assertThat(predictor.predict(0, 0), is((1+1+1)/3d));
        assertThat(predictor.predict(1, 0), is((1+2+3)/3d));
        assertThat(predictor.predict(2, 0), is((2+3)/2d));
        
        assertThat(predictor.predict(0, 1), is((1+1+1)/3d));
        assertThat(predictor.predict(1, 1), is((1+2+3)/3d));
        assertThat(predictor.predict(2, 1), is((2+3)/2d));
        
        assertThat(predictor.predict(0, 2), is((1+1+1)/3d));
        assertThat(predictor.predict(1, 2), is((1+2+3)/3d));
        assertThat(predictor.predict(2, 2), is((2+3)/2d));
    }

}
