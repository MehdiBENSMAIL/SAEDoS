import java.io.*;

public class DosReadTest {
    static final int FP = 1000;
    static final int BAUDS = 100;
    static final int[] START_SEQ = { 1, 0, 1, 0, 1, 0, 1, 0 };
    FileInputStream fileInputStream;
    int sampleRate = 44100;
    int bitsPerSample;
    int dataSize;
    double[] audio;
    int[] outputBits;
    char[] decodedChars;

    /**
     * Constructor that opens the FileInputStream
     * and reads sampleRate, bitsPerSample and dataSize
     * from the header of the wav file
     * 
     * @param path the path of the wav file to read
     */
    public void readWavHeader(String path) {
        byte[] header = new byte[44]; // The header is 44 bytes long
        try {
            fileInputStream = new FileInputStream(path);
            // On vient récupérer les différents paramètres dans le header
            fileInputStream.read(header);
            // Le taux d'echantillonage est a l'offset 24
            sampleRate = byteArrayToInt(header, 24, 32);
            // Pour les bits par echantillon, c'est a l'offset 34
            bitsPerSample = byteArrayToInt(header, 34, 16);
            // pour la taille des donnees, c'est a l'offset 40
            dataSize = byteArrayToInt(header, 40, 32);
            System.out.println(dataSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to convert a little-endian byte array to an integer
     * 
     * @param bytes  the byte array to convert
     * @param offset the offset in the byte array
     * @param fmt    the format of the integer (16 or 32 bits)
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
        else
            return (bytes[offset] & 0xFF);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        // On crée le tableau de sortie
        audio = new double[audioData.length / 2];
        // On parcourt le tableau de sortie
        for (int i = 0; i < audio.length; i++) {
            // On récupère les deux octets correspondant à l'échantillon
            byte b1 = audioData[i * 2];
            byte b2 = audioData[i * 2 + 1];
            // On les convertit en entier
            int value = (b2 << 8) | (b1 & 0xFF);
            // On convertit l'entier en double
            audio[i] = (double) value;

        }

        // On normalise les valeurs entre -1 et 1
        // double max = 0;
        // for (int i = 0; i < audio.length; i++) {
        // if (Math.abs(audio[i]) > max) {
        // max = Math.abs(audio[i]);
        // }
        // }
        // for (int i = 0; i < audio.length; i++) {
        // audio[i] /= max;
        // }
    }

    /**
     * Reverse the negative values of the audio array
     */
    public void audioRectifier() {
        for (int i = 0; i < audio.length; i++) {
            if (audio[i] < 0) {
                audio[i] = Math.abs(audio[i]);
            }
        }
    }

    /**
     * Apply a low pass filter to the audio array
     * Fc = (1/2n)*FECH
     * 
     * @param n the number of samples to average
     */
    public void audioLPFilter(int n) {
        // On récupère la taille du tableau de sortie
        int newLength = audio.length / n;
        // On crée le tableau de sortie
        double[] audioFiltered = new double[newLength];
        // On parcourt le tableau de sortie
        for (int i = 0; i < newLength; i++) {
            // On calcule la moyenne des échantillons sur la période
            double sum = 0;
            for (int j = 0; j < n; j++) {
                sum += audio[i * n + j];
            }
            audioFiltered[i] = sum / n;
        }
        audio = audioFiltered;
    }

    /**
     * @param threshold the threshold that separates 0 and 1
     */
    public void audioResampleAndThreshold(int period, int threshold){
       // On récupère la taille du tableau de sortie
       int nbBits = outputBits.length / period;
       // On crée le tableau de sortie
       int[] outputBitsResampled = new int[nbBits];
       // On parcourt le tableau de sortie
       for (int i = 0; i < nbBits; i++) {
            // On calcule la moyenne des bits sur la période
            int sum = 0;
            for (int j = 0; j < period; j++) {
            sum += outputBits[i * period + j];
            }
            int average = sum / period;
            // On applique le seuil
            if (average > threshold) {
            outputBitsResampled[i] = 1;
            } else {
            outputBitsResampled[i] = 0;
            }
        }
    }

    /**
     * The next first symbol is the first bit of the first char.
     */
    public void decodeBitsToChar() {
        // Affichage des bits
        for (int i = 0; i < outputBits.length; i++) {
            System.out.print(outputBits[i]);
        }

        int start = 0;
        int i = 0;
        while (i < outputBits.length - 8) {
            boolean isStart = true;
            for (int j = 0; j < 8; j++) {
                if (outputBits[i + j] != START_SEQ[j]) {
                    isStart = false;
                }
            }
            if (isStart) {
                start = i + 8;
                break;
            }
            i++;
        }
        if (start == 0) {
            System.out.println("Pas de séquence de départ trouvée");
            return;
        }
        int nbBits = (outputBits.length - start) / 8;
        decodedChars = new char[nbBits];
        for (int j = 0; j < nbBits; j++) {
            int value = 0;
            for (int k = 0; k < 8; k++) {
                value += outputBits[start + j * 8 + k] * Math.pow(2, 7 - k);
            }
            decodedChars[j] = (char) value;
        }
    }

    /**
     * Print the elements of an array
     * 
     * @param data the array to print
     */
    public static void printIntArray(char[] data) {
        if (data == null) {
            System.out.println("null");
            return;
        }
        for (int i = 0; i < data.length - 1; i++) {
            System.out.print(data[i]);
        }
        System.out.println(data[data.length - 1]);
    }

    /**
     * Display a signal in a window
     * 
     * @param sig   the signal to display
     * @param start the first sample to display
     * @param stop  the last sample to display
     * @param mode  "line" or "point"
     * @param title the title of the window
     */
    public static void displaySig(double[] sig, int start, int stop, String mode, String title) {
        StdDraw.enableDoubleBuffering();
        StdDraw.setCanvasSize(1920, 720);
        StdDraw.setXscale(start, stop);
        StdDraw.setTitle(title);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.line(0, -1, 0, 1);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setPenRadius(0.005);
        if (mode.equals("line")) {
            for (int i = start; i < stop - 1; i++) {
                StdDraw.line(i, sig[i], i + 1, sig[i + 1]);
            }
        } else if (mode.equals("point")) {
            for (int i = start; i < stop; i++) {
                StdDraw.point(i, sig[i]);
            }
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

    /**
     * Un exemple de main qui doit pourvoir être exécuté avec les méthodes
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
        // dosRead.audioLPFilter(44);
        // Resample audio data and apply a threshold to output only 0 & 1
        dosRead.audioResampleAndThreshold(dosRead.sampleRate / BAUDS, 12000);

        dosRead.decodeBitsToChar();
        if (dosRead.decodedChars != null) {
            System.out.print("Message décodé : ");
            System.out.print("\t------------------------------\n");
            System.out.print("\tMessage décodé : ");
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
