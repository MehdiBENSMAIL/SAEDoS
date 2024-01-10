public class LPFilter1 {
    public static void main(String[] args) {

    }

    public void audioLPFilter(double[] audio, int n) {
        // On crée le tableau de sortie
        double[] output = new double[audio.length];
        // On parcourt le tableau de sortie
        for (int i = 0; i < output.length; i++) {
          // On calcule la moyenne des échantillons
          double sum = 0;
          for (int j = 0; j < n; j++) {
            if (i - j >= 0) {
              sum += audio[i - j];
            }
          }
          output[i] = sum / n;
        }
        audio = output;
    }
}
