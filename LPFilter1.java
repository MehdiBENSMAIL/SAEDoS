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
        // More info on the butterworth filter : https://en.wikipedia.org/wiki/Butterworth_filter
        int n = 44; // Order of the Butterworth filter
        double normCutoffFreq = 2.0 * Math.PI * cutoffFreq / sampleFreq;
        double[] b = new double[n + 1];
        double[] a = new double[n + 1];
        double[] outputSignal = new double[inputSignal.length];

        // Calculate Butterworth filter coefficients
        for (int k = 0; k <= n; k++) {
            b[k] = Math.pow(normCutoffFreq, n - k);
            a[k] = binomialCoeff(n, k);
        }
        // Apply the filter
        for (int i = 0; i < inputSignal.length; i++) {
            outputSignal[i] = 0.0;
            for (int j = 0; j <= n; j++) {
                if (i - j >= 0) {
                    outputSignal[i] += b[j] * inputSignal[i - j] / a[0];
                }
            }
        }
        return outputSignal;
    }

    /**
     * Calculate the binomial coefficient
     * @param n
     * @param k
     * @return the binomial coefficient
     */
    private double binomialCoeff(int n, int k) {
        if (k == 0 || k == n) {
            return 1.0;
        } else {
            return binomialCoeff(n - 1, k - 1) + binomialCoeff(n - 1, k);
        }
    }

}