import java.util.List;
import java.awt.Color;

public class TestLP {
    public static void main(String[] args) {
        LPFilter1 lpFilter1 = new LPFilter1();
        LPFilter2 lpFilter2 = new LPFilter2();
        int[] testValue = { 10, 100, 1000, 10000 };
        int[] sampleFreq = { 50 };
        double[] cutoffFreq = { 0.75 };

        for (int i = 0; i < testValue.length; i++) {
            double[] signal = generateSignal(testValue[i]);
            for (int j = 0; j < sampleFreq.length; j++) {
                for (int k = 0; k < cutoffFreq.length; k++) {
                    long time1 = System.nanoTime();
                    double[] output1 = lpFilter1.lpFilter(signal, sampleFreq[j], cutoffFreq[k]);
                    time1 = System.nanoTime() - time1;
                    long time2 = System.nanoTime();
                    double[] output2 = lpFilter2.lpFilter(signal, sampleFreq[j], cutoffFreq[k]);
                    time2 = System.nanoTime() - time2;
                    System.out.println("Test value for value :" + testValue[i] + " Sample frequency: " + sampleFreq[j] + " Cutoff frequency: " + cutoffFreq[k]);
                    System.out.println("Time for LPFilter1: " + time1 + " nanoseconds");
                    System.out.println("Time for LPFilter2: " + time2 + " nanoseconds");
                    System.out.println("--------------------------------------------------");
                }
            }
        }

    }

    public static void printArray(double[] array) {
        for (double d : array) {
            System.out.print(d + " ");
        }
        System.out.println();
    }

    /**
     * Generate a random signal
     * 
     * @param length
     * @return the generated signal
     */
    public static double[] generateSignal(int length) {
        double[] signal = new double[length];
        for (int i = 0; i < length; i++) {
            signal[i] = Math.random();
        }
        return signal;
    }

    static Color[] COLORS = { StdDraw.BLUE, StdDraw.GREEN,
            StdDraw.ORANGE };

    /**
     * Display signals in a window
     * 
     * @param listOfSigs a list of the signals to display
     * @param start      the first sample to display
     * @param stop       the last sample to display
     * @param mode       "line" or "point"
     * @param title      the title of the window
     */
    public static void displaySig(List<double[]> listOfSigs,
            int start, int stop, String mode, String title) {
        StdDraw.enableDoubleBuffering();
        StdDraw.setCanvasSize(1920, 720);
        StdDraw.setXscale(start, stop);
        StdDraw.setTitle(title);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.line(0, -1, 0, 1);
        StdDraw.setPenRadius(0.005);
        int color_index = 0;
        for (int i = 0; i < listOfSigs.size(); i++) {
            // set the color of the signal
            StdDraw.setPenColor(COLORS[color_index]);
            if (mode.equals("line")) {
                for (int j = start; j < stop - 1; j++) {
                    StdDraw.line(j, listOfSigs.get(i)[j],
                            (double) j + 1, listOfSigs.get(i)[j + 1]);
                }
            } else if (mode.equals("point")) {
                for (int j = start; j < stop; j++) {
                    StdDraw.point(j, listOfSigs.get(i)[j]);
                }
            }
            color_index++;
        }
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.0075);
        StdDraw.line(start, 0.5, stop, 0.5);
        for (int i = start; i < stop; i += (stop - start) / 10) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.line(i, 0.475, i, 0.525);
            StdDraw.setPenColor(StdDraw.YELLOW);
            StdDraw.text(i, 0.450, "" + i);
        }
        StdDraw.show();
    }

}
