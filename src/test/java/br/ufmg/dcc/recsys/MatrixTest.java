package br.ufmg.dcc.recsys;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import br.ufmg.dcc.recsys.Matrix;


public class MatrixTest {

    @Test
    public void testLineSum() {
        // given
        double[][] d = {
            { 1, 1, 1 },
            { 2, 2, 2 },
            { 3, 3, 3}
        };
        Matrix D = new Matrix(d);
        
        //then
        assertThat(D.lineSum(0), is(1+1+1d));
        assertThat(D.lineSum(1), is(2+2+2d));
        assertThat(D.lineSum(2), is(3+3+3d));
    }

    @Test
    public void testLineAverage() {
        // given
        double[][] d = {
            { 1, 1, 1 },
            { 2, 2, 2 },
            { 3, 3, 3}
        };
        Matrix D = new Matrix(d);
        
        //then
        assertThat(D.lineAvg(0), is((1+1+1)/3d));
        assertThat(D.lineAvg(1), is((2+2+2)/3d));
        assertThat(D.lineAvg(2), is((3+3+3)/3d));
    }
    
    @Test
    public void testLineAverageNonZero() {
        // given
        double[][] d = {
            { 1, 3, 1 },
            { 2, 0, 3 },
            { 3, 5, 0}
        };
        Matrix D = new Matrix(d);
        
        //then
        assertThat(D.lineAvgNonZero(0), is((1+3+1)/3d));
        assertThat(D.lineAvgNonZero(1), is((2+3)/2d));
        assertThat(D.lineAvgNonZero(2), is((3+5)/2d));
    }
    
    @Test
    public void testCollumnAverageNonZero() {
        // given
        double[][] d = {
            { 1, 1, 2 },
            { 1, 2, 3 },
            { 1, 3, 0}
        };
        Matrix D = new Matrix(d);
        
        //then
        assertThat(D.columnAvgNonZero(0), is((1+1+1)/3d));
        assertThat(D.columnAvgNonZero(1), is((1+2+3)/3d));
        assertThat(D.columnAvgNonZero(2), is((2+3)/2d));
    }
    
    @Test
    public void testPearsonCorrelationSimilarity() {
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
        
        //then
        assertThat(D.pearsonCorrelationSimilarity(0, 1), is(closeTo(0.83d, 0.1)));
        assertThat(D.pearsonCorrelationSimilarity(0, 2), is(closeTo(0.60d, 0.1)));
        assertThat(D.pearsonCorrelationSimilarity(0, 3), is(closeTo(0.00d, 0.1)));
        assertThat(D.pearsonCorrelationSimilarity(0, 4), is(closeTo(-0.76d, 0.1)));
    }

    @Test
    public void testCollumnConsineSimilarity() {
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
        
        //then
        assertThat(D.collumnCosineSimilarity(4, 0), is(closeTo(0.99d, 0.01)));
        assertThat(D.collumnCosineSimilarity(4, 3), is(closeTo(0.94d, 0.01)));
    }
    
}
