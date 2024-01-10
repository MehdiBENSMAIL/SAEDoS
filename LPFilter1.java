// coding : UTF-8
// Date : 11/01/2024
// Authors : Jean-Baptiste FROEHLY   - B2
//           Mehdi         BEN SMAIL - C1
// Low-pass filter butterworth method

public class LPFilter1 {

    /**
     * Apply a low pass filter to the audio array using the butterworth method
     * @param inputSignal the audio array to be filtered (double[])
     * @param sampleFreq  the sample frequency of the audio array (double
     * @param cutoffFreq  the cutoff frequency of the filter (double)
     * @return the filtered audio array (double[])
     */
    public double[] lpFilter(double[] inputSignal, double sampleFreq, double cutoffFreq) {
        int N = inputSignal.length;
        double[] outputSignal = new double[N];

        // Calculate filter coefficients
        int M = 40; // Filter order
        double[] h = new double[M];
        double fc = cutoffFreq / sampleFreq;

        for (int n = 0; n < M; n++) {
            if (n == (M - 1) / 2) {
                h[n] = 2 * Math.PI * fc;
            } else {
                h[n] = Math.sin(2 * Math.PI * fc * (n - (M - 1) / 2)) / (Math.PI * (n - (M - 1) / 2));
            }
            h[n] *= 0.54 - 0.46 * Math.cos(2 * Math.PI * n / (M - 1)); // Hamming window
        }

        // Apply the filter
        for (int i = 0; i < N; i++) {
            double sum = 0.0;
            for (int j = 0; j < M; j++) {
                if (i - j >= 0) {
                    sum += h[j] * inputSignal[i - j];
                }
            }
            outputSignal[i] = sum;
        }

        return outputSignal;
    }

}