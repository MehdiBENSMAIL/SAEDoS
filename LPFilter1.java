// coding : UTF-8
// Date : 11/01/2024
// Authors : Jean-Baptiste FROEHLY   - B2
//           Mehdi         BEN SMAIL - C1
// Low-pass filter moving average

public class LPFilter1 {
    public static void main(String[] args) {

    }

    /**
     * Apply a low pass filter to the audio array (moving average)
     * @param audioInput the input audio array (double[])
     * @param sampleCount the number of samples to average
     */
    public void audioLPFilter(double[] audioInput, int sampleCount) {
      // Create the output array
      double[] output = new double[audioInput.length];
      // Iterate over the output array
      for (int i = 0; i < output.length; i++) {
        // Calculate the average of the samples
        double sum = 0;
        for (int j = 0; j < sampleCount; j++) {
          if (i - j >= 0) {
            sum += audioInput[i - j];
          }
        }
        output[i] = sum / sampleCount;
      }
      audioInput = output;
    }
}
