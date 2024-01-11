// coding : UTF-8
// Date : 11/01/2024
// Authors : Jean-Baptiste FROEHLY   - B2
//           Mehdi         BEN SMAIL - C1
// Low-pass moving average filter

public class LPFilter1 {

    /**
     * Apply a low pass filter to the audio array using the moving average method
     * @param inputSignal the audio array to be filtered (double[])
     * @param sampleFreq  the sample frequency of the audio array (double
     * @param cutoffFreq  the cutoff frequency of the filter (double)
     * @return the filtered audio array (double[])
     */
    public double[] lpFilter(double[] inputSignal, double sampleFreq, double cutoffFreq) {
        // Initialize the output array
        double[] outputSignal = new double[inputSignal.length];

        // Calculate the number of samples to consider for the moving average
        int numSamples = (int) Math.round(sampleFreq / cutoffFreq);
        numSamples = numSamples % 2 == 0 ? numSamples + 1 : numSamples;

        // Apply the moving average filter
        for (int i = 0; i < inputSignal.length; i++) {
            // Determine the range
            int start = Math.max(0, i - numSamples / 2);
            int end = Math.min(inputSignal.length - 1, i + numSamples / 2);

            // Calculate the average of the selected samples
            double sum = 0;
            for (int j = start; j <= end; j++) {
                sum += inputSignal[j];
            }
            outputSignal[i] = sum / (end - start + 1);
        }

        return outputSignal;
    }

}