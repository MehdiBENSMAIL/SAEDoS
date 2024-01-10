public class LPFilter2 {
    // Filtre passe-bas basé sur fourier

    public static void main(String[] args) {
        // On crée un tableau de 1000 échantillons
        double[] audio = new double[1000];
        // On remplit le tableau avec des valeurs aléatoires
        for (int i = 0; i < audio.length; i++) {
            audio[i] = Math.random();
        }
        // On applique le filtre
        audioLPFilter(audio, 10);
    }

    public static void audioLPFilter(double[] audio, int n) {
        // On calcule la transformée de Fourier
        Complex[] fourier = FFT.fft(audio);
        // On crée un tableau de sortie
        Complex[] output = new Complex[fourier.length];
        // On parcourt le tableau de sortie
        for (int i = 0; i < output.length; i++) {
            // On calcule la moyenne des échantillons
            double sum = 0;
            for (int j = 0; j < n; j++) {
                if (i - j >= 0) {
                    sum += fourier[i - j].re();
                }
            }
            output[i] = new Complex(sum / n, 0);
        }
        // On calcule la transformée de Fourier inverse
        Complex[] inverse = FFT.ifft(output);
        // On remplit le tableau de sortie
        for (int i = 0; i < audio.length; i++) {
            audio[i] = inverse[i].re();
        }
    }
}