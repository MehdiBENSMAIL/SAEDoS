// coding : UTF-8
// Date : 11/01/2024
// Authors : Jean-Baptiste FROEHLY   - B2
//           Mehdi         BEN SMAIL - C1
// Low-pass exponential moving average filter


public class LPFilter2 {

    /**
     * Apply a low pass filter to the audio array using the exponential moving average method
     * @param inputSignal the audio array to be filtered (double[])
     * @param sampleFreq the sample frequency of the audio array (double
     * @param cutoffFreq the cutoff frequency of the filter (double)
     * @return the filtered audio array (double[])
     */
    public double[] lpFilter(double[] inputSignal, double sampleFreq, double cutoffFreq) {
        // Initialize the output array
        double[] outputSignal = new double[inputSignal.length];
        // Formula can be found and detailed at https://en.wikipedia.org/wiki/RC_circuit
        double resistanceCapacitance = 1.0 / (cutoffFreq * 2 * Math.PI);
        double timeStep = 1.0 / sampleFreq;
        // Formula can be found and detailed at https://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
        double smoothingFactor = 1 - Math.exp(-timeStep / resistanceCapacitance);
        outputSignal[0] = inputSignal[0];
        for (int i = 1; i < inputSignal.length; i++) {
            outputSignal[i] = outputSignal[i - 1] + smoothingFactor * (inputSignal[i] - outputSignal[i - 1]);
        }
        return outputSignal;
    }
}