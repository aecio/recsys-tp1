import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class UserToUserPredictorTest {

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
        final double prediction = new UserToUserPredictor(D, 2).predict(4, 0);
        
        // then
        assertThat(prediction, closeTo(4.8d, 0.1));
    }

}
