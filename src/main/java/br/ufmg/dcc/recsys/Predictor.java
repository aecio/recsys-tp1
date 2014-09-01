package br.ufmg.dcc.recsys;

public interface Predictor {
    public double predict(int item, int user);
}
