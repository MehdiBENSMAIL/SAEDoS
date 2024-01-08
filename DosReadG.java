import java.io.*;

public class DosReadG {
    static final int FP = 1000;
    static final int BAUDS = 100;
    static final int[] START_SEQ = {1, 0, 1, 0, 1, 0, 1, 0};
    FileInputStream fileInputStream;
    int sampleRate = 44100;
    int bitsPerSample;
    int dataSize;
    double[] audio;
    int[] outputBits;
    char[] decodedChars;

    /**
     * Constructor that opens the FileInputStream
     * and reads sampleRate, bitsPerSample, and dataSize
     * from the header of the wav file
     * @param path the path of the wav file to read
     */
    public void readWavHeader(String path) {
        byte[] header = new byte[44]; // The header is 44 bytes long
        try {
            fileInputStream = new FileInputStream(path);
            fileInputStream.read(header);

            // Read sampleRate, bitsPerSample, and dataSize from the header
            sampleRate = byteArrayToInt(header, 24, 32);
            bitsPerSample = byteArrayToInt(header, 34, 16);
            dataSize = byteArrayToInt(header, 40, 32);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to convert a little-endian byte array to an integer
     * @param bytes the byte array to convert
     * @param offset the offset in the byte array
     * @param fmt the format of the integer (16 or 32 bits)
     * @return the integer value
     */
    private static int byteArrayToInt(byte[] bytes, int offset, int fmt) {
        if (fmt == 16)
            return ((bytes[offset + 1] & 0xFF) << 8) | (bytes[offset] & 0xFF);
        else if (fmt == 32)
            return ((bytes[offset + 3] & 0xFF) << 24) |
                    ((bytes[offset + 2] & 0xFF) << 16) |
                    ((bytes[offset + 1] & 0xFF) << 8) |
                    (bytes[offset] & 0xFF);
        else return (bytes[offset] & 0xFF);
    }

    /**
     * Read the audio data from the wav file
     * and convert it to an array of doubles
     * that becomes the audio attribute
     */
    public void readAudioDouble() {
        byte[] audioData = new byte[dataSize];
        try {
            fileInputStream.read(audioData);

            // Convert audioData to an array of doubles
            audio = new double[dataSize / (bitsPerSample / 8)];
            for (int i = 0; i < audio.length; i++) {
                audio[i] = (double) byteArrayToInt(audioData, i * (bitsPerSample / 8), bitsPerSample);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reverse the negative values of the audio array
     */
    public void audioRectifier() {
        for (int i = 0; i < audio.length; i++) {
            if (audio[i] < 0) {
                audio[i] = -audio[i];
            }
        }
    }

    /**
     * Apply a low pass filter to the audio array
     * Fc = (1/2n)*FECH
     * @param n the number of samples to average
     */
    public void audioLPFilter(int n) {
        double[] filteredAudio = new double[audio.length];
        for (int i = 0; i < audio.length; i++) {
            double sum = 0;
            for (int j = Math.max(0, i - n + 1); j <= i; j++) {
                sum += audio[j];
            }
            filteredAudio[i] = sum / n;
        }
        audio = filteredAudio;
    }

    /**
     * Resample the audio array and apply a threshold
     * @param period the number of audio samples per symbol
     * @param threshold the threshold that separates 0 and 1
     */
    public void audioResampleAndThreshold(int period, int threshold) {
        // Resample the audio array
        int newLength = audio.length / period;
        double[] resampledAudio = new double[newLength];
        for (int i = 0; i < newLength; i++) {
            resampledAudio[i] = audio[i * period];
        }

        // Apply threshold to create outputBits array
        outputBits = new int[newLength];
        for (int i = 0; i < newLength; i++) {
            outputBits[i] = (resampledAudio[i] > threshold) ? 1 : 0;
        }
    }

    /**
     * Decode the outputBits array to a char array
     * The decoding is done by comparing the START_SEQ with the actual beginning of outputBits.
     * The next first symbol is the first bit of the first char.
     */
    public void decodeBitsToChar() {
        decodedChars = new char[(outputBits.length - START_SEQ.length) / 8];
        for (int i = 0; i < decodedChars.length; i++) {
            int charValue = 0;
            for (int j = 0; j < 8; j++) {
                charValue = (charValue << 1) | outputBits[i * 8 + j];
            }
            decodedChars[i] = (char) charValue;
        }
    }

    /**
     * Print the elements of an array
     * @param data the array to print
     */
    public static void printIntArray(char[] data) {
        for (char c : data) {
            System.out.print(c);
        }
        System.out.println();
    }

    /**
     * Display a signal in a window
     * @param sig  the signal to display
     * @param start the first sample to display
     * @param stop the last sample to display
     * @param mode "line" or "point"
     * @param title the title of the window
     */
    public static void displaySig(double[] sig, int start, int stop, String mode, String title) {
        // Implementation of displaySig method goes here
        // ...

    }

    /**
    *  Un exemple de main qui doit pourvoir être exécuté avec les méthodes
    * que vous aurez conçues.
    */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java DosRead <input_wav_file>");
            return;
        }
        String wavFilePath = args[0];

        // Open the WAV file and read its header
        DosRead dosRead = new DosRead();
        dosRead.readWavHeader(wavFilePath);

        // Print the audio data properties
        System.out.println("Fichier audio: " + wavFilePath);
        System.out.println("\tSample Rate: " + dosRead.sampleRate + " Hz");
        System.out.println("\tBits per Sample: " + dosRead.bitsPerSample + " bits");
        System.out.println("\tData Size: " + dosRead.dataSize + " bytes");

        // Read the audio data
        dosRead.readAudioDouble();
        // reverse the negative values
        dosRead.audioRectifier();
        // apply a low pass filter
        dosRead.audioLPFilter(44);
        // Resample audio data and apply a threshold to output only 0 & 1
        dosRead.audioResampleAndThreshold(dosRead.sampleRate / BAUDS, 12000);

        dosRead.decodeBitsToChar();
        if (dosRead.decodedChars != null) {
            System.out.print("Message décodé : ");
            printIntArray(dosRead.decodedChars);
        }

        displaySig(dosRead.audio, 0, dosRead.audio.length - 1, "line", "Signal audio");

        // Close the file input stream
        try {
            dosRead.fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
