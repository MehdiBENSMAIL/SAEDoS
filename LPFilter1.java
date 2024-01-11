// coding : UTF-8
// Date : 11/01/2024
// Authors : Jean-Baptiste FROEHLY   - B2
//           Mehdi         BEN SMAIL - C1
// Low-pass filter sinc function

public class LPFilter1 {

    /**
     * Apply a low pass filter to the audio array using the sinc function
     * @param inputSignal the audio array to be filtered (double[])
     * @param sampleFreq  the sample frequency of the audio array (double)
     * @param cutoffFreq  the cutoff frequency of the filter (double)
     * @return the filtered audio array (double[])
     */
    public double[] lpFilter(double[] inputSignal,
            double sampleFreq, double cutoffFreq) {
        int signalLength = inputSignal.length;
        double[] outputSignal = new double[signalLength];
        for (int n = 0; n < signalLength; n++) {
            double sum = 0.0;
            for (int k = 0; k < signalLength; k++) {
                if (k != n) { // Avoid division by 0
                    // Apply the sinc function
                    sum += inputSignal[k] *
                    Math.sin(2 * Math.PI * cutoffFreq * (n - k) / sampleFreq)
                    / (Math.PI * (n - k));
                }
            }
            outputSignal[n] = inputSignal[n] * 2 *
                                cutoffFreq / sampleFreq + sum;
        }

        return outputSignal;
    }
}