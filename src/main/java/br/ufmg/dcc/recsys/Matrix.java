package br.ufmg.dcc.recsys;
/*************************************************************************
 * Compilation: javac Matrix.java Execution: java Matrix
 *
 * A bare-bones immutable data type for M-by-N matrices.
 *
 *************************************************************************/

public class Matrix {
    private final int M; // number of rows
    private final int N; // number of columns
    private final double[][] data; // M-by-N array

    // create M-by-N matrix of 0's
    public Matrix(int M, int N) {
        this.M = M;
        this.N = N;
        data = new double[M][N];
    }

    // create matrix based on 2d array
    public Matrix(double[][] data) {
        M = data.length;
        N = data[0].length;
        this.data = new double[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                this.data[i][j] = data[i][j];
    }

    // copy constructor
    private Matrix(Matrix A) {
        this(A.data);
    }

    // create and return a random M-by-N matrix with values between 0 and 1
    public static Matrix random(int M, int N) {
        Matrix A = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[i][j] = Math.random();
        return A;
    }

    // create and return the N-by-N identity matrix
    public static Matrix identity(int N) {
        Matrix I = new Matrix(N, N);
        for (int i = 0; i < N; i++)
            I.data[i][i] = 1;
        return I;
    }

    // swap rows i and j
    private void swap(int i, int j) {
        double[] temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    // create and return the transpose of the invoking matrix
    public Matrix transpose() {
        Matrix A = new Matrix(N, M);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[j][i] = this.data[i][j];
        return A;
    }

    // return C = A + B
    public Matrix plus(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N)
            throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] + B.data[i][j];
        return C;
    }

    // return C = A - B
    public Matrix minus(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N)
            throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] - B.data[i][j];
        return C;
    }

    // does A = B exactly?
    public boolean eq(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N)
            throw new RuntimeException("Illegal matrix dimensions.");
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                if (A.data[i][j] != B.data[i][j])
                    return false;
        return true;
    }

    // return C = A * B
    public Matrix times(Matrix B) {
        Matrix A = this;
        if (A.N != B.M)
            throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(A.M, B.N);
        for (int i = 0; i < C.M; i++)
            for (int j = 0; j < C.N; j++)
                for (int k = 0; k < A.N; k++)
                    C.data[i][j] += (A.data[i][k] * B.data[k][j]);
        return C;
    }

    // return x = A^-1 b, assuming A is square and has full rank
    public Matrix solve(Matrix rhs) {
        if (M != N || rhs.M != N || rhs.N != 1)
            throw new RuntimeException("Illegal matrix dimensions.");

        // create copies of the data
        Matrix A = new Matrix(this);
        Matrix b = new Matrix(rhs);

        // Gaussian elimination with partial pivoting
        for (int i = 0; i < N; i++) {

            // find pivot row and swap
            int max = i;
            for (int j = i + 1; j < N; j++)
                if (Math.abs(A.data[j][i]) > Math.abs(A.data[max][i]))
                    max = j;
            A.swap(i, max);
            b.swap(i, max);

            // singular
            if (A.data[i][i] == 0.0)
                throw new RuntimeException("Matrix is singular.");

            // pivot within b
            for (int j = i + 1; j < N; j++)
                b.data[j][0] -= b.data[i][0] * A.data[j][i] / A.data[i][i];

            // pivot within A
            for (int j = i + 1; j < N; j++) {
                double m = A.data[j][i] / A.data[i][i];
                for (int k = i + 1; k < N; k++) {
                    A.data[j][k] -= A.data[i][k] * m;
                }
                A.data[j][i] = 0.0;
            }
        }

        // back substitution
        Matrix x = new Matrix(N, 1);
        for (int j = N - 1; j >= 0; j--) {
            double t = 0.0;
            for (int k = j + 1; k < N; k++)
                t += A.data[j][k] * x.data[k][0];
            x.data[j][0] = (b.data[j][0] - t) / A.data[j][j];
        }
        return x;

    }

    // print matrix to standard output
    public void show() {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++)
                System.out.printf("%9.4f ", data[i][j]);
            System.out.println();
        }
    }

    // test client
    public static void main(String[] args) {
        double[][] d = { { 1, 2, 3 }, { 4, 5, 6 }, { 9, 1, 3 } };
        Matrix D = new Matrix(d);
        D.show();
        System.out.println();

        Matrix A = Matrix.random(5, 5);
        A.show();
        System.out.println();

        A.swap(1, 2);
        A.show();
        System.out.println();

        Matrix B = A.transpose();
        B.show();
        System.out.println();

        Matrix C = Matrix.identity(5);
        C.show();
        System.out.println();

        A.plus(B).show();
        System.out.println();

        B.times(A).show();
        System.out.println();

        // shouldn't be equal since AB != BA in general
        System.out.println(A.times(B).eq(B.times(A)));
        System.out.println();

        Matrix b = Matrix.random(5, 1);
        b.show();
        System.out.println();

        Matrix x = A.solve(b);
        x.show();
        System.out.println();

        A.times(x).show();

    }
    
    //
    // Additional methods
    //

    public double lineSum(int line) {
        double sum = 0d;
        for (int i = 0; i < data[line].length; i++) {
            sum += data[line][i];
        }
        return sum;
    }

    public double columnAvgNonZero(int column) {
        double sum = 0d;
        int total = 0;
        for (int i = 0; i < data.length; i++) {
            if(data[i][column] != 0) {
                sum += data[i][column];
                total++;
            }
        }
        return sum / total;
    }
    
    public double lineAvg(int line) {
        double sum = 0d;
        for (int i = 0; i < data[line].length; i++) {
            sum += data[line][i];
        }
        return sum / data[line].length;
    }

    public double lineAvgNonZero(int line) {
        double sum = 0d;
        int total = 0;
        for (int i = 0; i < data[line].length; i++) {
            if(data[line][i] != 0) {
                sum += data[line][i];
                total++;
            }
        }
        return sum / total;
    }

    public double pearsonCorrelationSimilarity(int a, int b) {
//        System.err.println("==============================");
        double r_avg_a = this.lineAvgNonZero(a);
        double r_avg_b = this.lineAvgNonZero(b);
        
//        System.err.println("r_avg_a: "+r_avg_a);
//        System.err.println("r_avg_b: "+r_avg_b);
//        
        double numerator = 0d;
        for(int p=0; p < N; p++) {
            if(data[a][p] != 0 && data[b][p] != 0) {
                numerator +=  (data[a][p] - r_avg_a) * (data[b][p] - r_avg_b); 
//                System.err.println("(data[a][p] - r_avg_a) = " + (data[a][p]+" - "+ r_avg_a)+" = "+(data[a][p] - r_avg_a) );
//                System.err.println("(data[b][p] - r_avg_b) = " + (data[b][p]+" - "+ r_avg_b)+" = "+ + (data[b][p] - r_avg_b));
//                System.err.println("    (times) = " + (data[a][p] - r_avg_a) * (data[b][p] - r_avg_b));
//                System.err.println("(numerator) = " + numerator);
            }
        }
        
//        System.err.println("numerator: "+numerator);
//        System.err.println("------------------------------");
        
        double sum_a = 0d;
        double sum_b = 0d;
        for(int p=0; p < N; p++) {
            if(data[a][p] != 0 && data[b][p] != 0) {
                sum_a += Math.pow((data[a][p] - r_avg_a), 2);
                sum_b += Math.pow((data[b][p] - r_avg_b), 2);
                
//                System.err.println("(data[a][p] - r_avg_a) = " + (data[a][p]+" - "+ r_avg_a)+" = "+(data[a][p] - r_avg_a) );
//                System.err.println("(data[a][p] - r_avg_a)^2 = " + Math.pow((data[a][p] - r_avg_a), 2) );
//                System.err.println("(sum_a) = " +sum_a);
//                System.err.println();
//                
//                System.err.println("(data[b][p] - r_avg_b) = " + (data[b][p]+" - "+ r_avg_b)+" = "+ + (data[b][p] - r_avg_b));
//                System.err.println("(data[b][p] - r_avg_b)^2 = " + Math.pow((data[b][p] - r_avg_b), 2));
//                System.err.println("(sum_b) = " +sum_b);
//                System.err.println();
            }
        }
        double denominator = Math.sqrt(sum_a)*Math.sqrt(sum_b);
//        System.err.println("sqrt(sum_a)="+Math.sqrt(sum_a)+ " sqrt(sum_b)="+Math.sqrt(sum_b)+ " times=" +Math.sqrt(sum_a)*Math.sqrt(sum_b));
//        System.err.println("denominator: "+denominator);
//        System.err.println("------------------------------");
//        System.err.println("similarity(a,b)="+numerator/denominator);
//        System.err.println("==============================");
//        
        return numerator/denominator;
    }
    
    
    public double pearsonCorrelationSimilarityAdjustedByCorrated(int a, int b) {
      double r_avg_a = this.lineAvgNonZero(a);
      double r_avg_b = this.lineAvgNonZero(b);
      
      int corated = 0;
      double numerator = 0d;
      for(int p=0; p < N; p++) {
          if(data[a][p] != 0 && data[b][p] != 0) {
              numerator +=  (data[a][p] - r_avg_a) * (data[b][p] - r_avg_b);
              corated++;
          }
      }
      
      double sum_a = 0d;
      double sum_b = 0d;
      for(int p=0; p < N; p++) {
          if(data[a][p] != 0 && data[b][p] != 0) {
              sum_a += Math.pow((data[a][p] - r_avg_a), 2);
              sum_b += Math.pow((data[b][p] - r_avg_b), 2);
          }
      }
      double denominator = Math.sqrt(sum_a)*Math.sqrt(sum_b);
      
      return Math.log(corated)*(numerator/denominator);
  }

    public int numRows() {
        return M;
    }
    
    public int numCols() {
        return N;
    }

    public double value(int row, int collumn) {
        return data[row][collumn];
    }
    
    public void printLineAvg(int line) {
        for (int i = 0; i < data[line].length; i++) {
            if(data[line][i]!=0d) {
                System.err.print(" data["+line+"]["+i+"]="+data[line][i]);
            }
        }
        System.err.println();
    }
    
    public void printCorrated(int u1, int u2) {
        System.err.println("corrated("+u1+","+u2+"):");
        for (int i = 0; i < M; i++) {
            if(data[u1][i] != 0d && data[u2][i] != 0d) {
                System.err.println(" data["+u1+"]["+i+"]="+data[u1][i] +" data["+u2+"]["+i+"]="+data[u2][i]);
            }
        }   
        System.err.println();
    }

    public double collumnCosineSimilarity(int i1, int i2) {
        double product = 0d;
        double norm1 = 0d;
        double norm2 = 0d;
        for (int i = 0; i < M; i++) {
            if(data[i][i1] != 0 && data[i][i2] != 0) { // only non-zero values
                product += data[i][i1] * data[i][i2];
                norm1 += Math.pow(data[i][i1], 2);
                norm2 += Math.pow(data[i][i2], 2);
            }
        }
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        return product / (norm1*norm2);
    }

}