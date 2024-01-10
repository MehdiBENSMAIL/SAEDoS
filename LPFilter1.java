// coding : UTF-8
// Date : 11/01/2024
// Auteurs : Jean-Baptiste FROEHLY   - B2
//           Mehdi         BEN SMAIL - C1
// Filtre passe-bas de moyenne glissante

public class LPFilter1 {
    public static void main(String[] args) {

    }

    /**
     * Filtre passe-bas de moyenne glissante
     * @param audio tableau d'entrée (double)
     * @param n nombre d'échantillons à moyenner (int)
     */
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
