// coding : UTF-8
// SAE-01 DoS 2024
// BEN SMAIL Mehdi - C1
// FROEHLY Jean-Baptiste - B2

// imports
import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.List;
import java.awt.Color;

public class DosSend {
    final int FECH = 44100; // frequence d'echantillonnage
    final int FP = 1000; // frequence de la porteuses
    final int BAUDS = 100; // debit en symboles par seconde
    final int FMT = 16; // format des donnees
    final int MAX_AMP = (1 << (FMT - 1)) - 1; // amplitude max en entier
    final int CHANNELS = 1; // nombre de voies audio (1 = mono)
    final int[] START_SEQ = { 1, 0, 1, 0, 1, 0, 1, 0 }; // sequence de synchro au dÃ©but
    final Scanner input = new Scanner(System.in); // pour lire le fichier texte
    // Liste de couleurs pour l'affichage multiple
    static Color[] COLORS = { StdDraw.WHITE, StdDraw.BLUE, StdDraw.GREEN, StdDraw.ORANGE, StdDraw.PINK,
            StdDraw.MAGENTA, StdDraw.YELLOW, StdDraw.CYAN };

    long taille; // nombre d'octets de donnees a transmettre
    double duree; // duree de l'audio
    double[] dataMod; // donnees modulees
    char[] dataChar; // donnees en char
    FileOutputStream outStream; // flux de sortie pour le fichier .wav

    /**
     * Constructor
     * @param path the path of the wav file to create
     */
    public DosSend(String path) {
        File file = new File(path);
        try {
            outStream = new FileOutputStream(file);
        } catch (Exception e) {
            System.out.println("Erreur de creation du fichier");
        }
    }

    /**
     * Write a raw 4-byte integer in little endian
     * @param octets     the integer to write
     * @param destStream the stream to write in
     */
    public void writeLittleEndian(int octets, int taille, FileOutputStream destStream) {
        char poidsFaible;
        while (taille > 0) {
            poidsFaible = (char) (octets & 0xFF);
            try {
                destStream.write(poidsFaible);
            } catch (Exception e) {
                System.out.println("Erreur d'ecriture");
            }
            octets = octets >> 8;
            taille--;
        }
    }

    /**
     * Create and write the header of a wav file
     *
     */
    public void writeWavHeader() {
        taille = (long) (FECH * duree);
        long nbBytes = taille * CHANNELS * FMT / 8;

        try {
            // [Declaration block of a WAVE format file]
            outStream.write(new byte[] { 'R', 'I', 'F', 'F' }); // FileTypeBlocID
            writeLittleEndian((int) (nbBytes + 36), 4, outStream); // FileSize
            outStream.write(new byte[] { 'W', 'A', 'V', 'E' }); // FileFormatID

            // [Block describing the audio format]
            outStream.write(new byte[] { 'f', 'm', 't', ' ' }); // FormatBlockID
            writeLittleEndian(16, 4, outStream); // BlocSize
            writeLittleEndian(1, 2, outStream); // AudioFormat
            writeLittleEndian(CHANNELS, 2, outStream); // NbrCanaux
            writeLittleEndian(FECH, 4, outStream); // Frequence
            writeLittleEndian(FECH * CHANNELS * FMT / 8, 4, outStream); // BytePerSec
            writeLittleEndian(CHANNELS * FMT / 8, 2, outStream); // BytePerBloc
            writeLittleEndian(FMT, 2, outStream); // BitsPerSample

            // [Data block]
            outStream.write(new byte[] { 'd', 'a', 't', 'a' }); // DataBlocID
            writeLittleEndian((int) nbBytes, 4, outStream); // DataSize

        } catch (Exception e) {
            System.out.printf(e.toString());
        }
    }

    /**
     * Write the data in the wav file
     * after normalizing its amplitude to the maximum value of the format (8 bits
     * signed)
     */
    public void writeNormalizeWavData() {
        try {
            // Ecriture des données
            for (int i = 0; i < dataMod.length; i++) {
                int sample = (int) (dataMod[i] * MAX_AMP);
                writeLittleEndian(sample, FMT / 8, outStream);
            }
        }
        // Gestion de l'erreur
        catch (Exception e) {
            System.out.println("Erreur d'ecriture");
        }
    }

    /**
     * Read the text data to encode and store them into dataChar
     * @return the number of characters read
     */
    public int readTextData() {
        String text = input.nextLine();
        dataChar = text.toCharArray();
        return dataChar.length;
    }

    public void test() {
        String text = input.nextLine();
        System.out.println(text);
    }

    /**
     * convert a char array to a bit array
     * @param chars
     * @return byte array containing only 0 & 1
     */
    public byte[] charToBits(char[] chars) {
        byte[] bits = new byte[chars.length * 8];
        for (int i = 0; i < chars.length; i++) { // for each char
            for (int j = 0; j < 8; j++) { // 8 bits per char
                bits[i * 8 + j] = (byte) ((chars[i] >> (7 - j)) & 0x01); // 7 - j to get the most significant bit first
            }
        }
        return bits;
    }

    /**
     * Modulate the data to send and apply the symbol throughout via BAUDS and FECH.
     * @param bits the data to modulate
     */
    public void modulateData(byte[] bits) {
        dataMod = new double[(int) (bits.length * FECH / BAUDS)];
        int index = 0;
        for (int i = 0; i < START_SEQ.length; i++) { // add the start sequence
            for (int j = 0; j < FECH / BAUDS && index < dataMod.length; j++) {
                dataMod[index] = START_SEQ[i];
                index++;
            }
        }
        for (int i = 0; i < bits.length; i++) { // add the data
            for (int j = 0; j < FECH / BAUDS && index < dataMod.length; j++) {
                dataMod[index] = bits[i];
                index++;
            }
        }
    }

    /**
     * Display a signal in a window
     * @param sig   the signal to display
     * @param start the first sample to display
     * @param stop  the last sample to display
     * @param mode  "line" or "point"
     * @param title the title of the window
     */
    public static void displaySig(double[] sig, int start, int stop, String mode, String title) {
        StdDraw.enableDoubleBuffering();
        StdDraw.setCanvasSize(1920, 720); // set the size of the window
        StdDraw.setTitle(title); // set the title of the window
        StdDraw.setXscale(start, stop); // set the scale of the window
        StdDraw.clear(StdDraw.BLACK); // set the background color to black
        StdDraw.line(0, -1, 0, 1); // draw the axis
        StdDraw.setPenColor(StdDraw.WHITE); // set the color of the signal to white
        StdDraw.setPenRadius(0.005); // set the radius of the signal
        // draw the signal depending on the mode
        if (mode.equals("line")) {
            for (int i = start; i < stop - 1; i++) {
                StdDraw.line(i, sig[i], i + 1, sig[i + 1]);
            }
        } else if (mode.equals("point")) {
            for (int i = start; i < stop; i++) {
                StdDraw.point(i, sig[i]);
            }
        }
        // draw the axis
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.0075);
        StdDraw.line(start, 0.5, stop, 0.5);
        // draw the ticks and the values
        for (int i = start; i < stop; i += (stop - start) / 10) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.line(i, 0.475, i, 0.525);
            StdDraw.setPenColor(StdDraw.YELLOW);
            StdDraw.text(i, 0.450, "" + i);
        }
        StdDraw.show();
    }

    /**
     * Display a signal in a window
     * @param sig   the signal to display
     * @param start the first sample to display
     * @param stop  the last sample to display
     * @param mode  "line" or "point"
     * @param title the title of the window
     */
    public static void displaySig(int[] sig, int start, int stop, String mode, String title) {
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
     * Display signals in a window
     * @param listOfSigs a list of the signals to display
     * @param start      the first sample to display
     * @param stop       the last sample to display
     * @param mode       "line" or "point"
     * @param title      the title of the window
     */
    public static void displaySig(List<double[]> listOfSigs, int start, int stop, String mode, String title) {
        StdDraw.enableDoubleBuffering();
        StdDraw.setCanvasSize(1920, 720);
        StdDraw.setXscale(start, stop);
        StdDraw.setTitle(title);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.line(0, -1, 0, 1);
        StdDraw.setPenRadius(0.005);
        int color_index = 0;
        for (int i = 0; i < listOfSigs.size(); i++) {
            // change the color for every signal up to seven different colors
            if (color_index > 7) {
                color_index = 0; // loops back to the first color if too many signals
            }
            for (int j = 0; j < listOfSigs.get(i).length; j++) {
                StdDraw.setPenColor(COLORS[color_index]); // set the color of the signal
            }
            if (mode.equals("line")) {
                for (int j = start; j < stop - 1; j++) {
                    StdDraw.line(j, listOfSigs.get(i)[j], j + 1, listOfSigs.get(i)[j + 1]);
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

    public static void main(String[] args) {
        // cree un objet DosSend
        DosSend dosSend = new DosSend("DosOok_message.wav");
        // lit le texte a envoyer depuis l'entree standard
        // et calcule la duree de l'audio correspondant
        dosSend.duree = (double) (dosSend.readTextData() + dosSend.START_SEQ.length / 8) * 8.0 / dosSend.BAUDS;

        // genere le signal module apres avoir converti les données en bits
        dosSend.modulateData(dosSend.charToBits(dosSend.dataChar));
        // Ecrit l'entete du fichier wav
        dosSend.writeWavHeader();
        // Ecrit les donnees audio dans le fichier wav
        dosSend.writeNormalizeWavData();

        // affiche les caracteristiques du signal dans la console
        System.out.println("Message : " + String.valueOf(dosSend.dataChar));
        System.out.println("\tNombre de symboles : " + dosSend.dataChar.length);
        System.out.println("\tNombre d'echantillons : " + dosSend.dataMod.length);
        System.out.println("\tDuree : " + dosSend.duree + " s");
        System.out.println();

        // exemple d'affichage du signal module dans une fenetre graphique
        displaySig(dosSend.dataMod, 0, dosSend.dataMod.length, "line", "Signal apres modulation");
    }
}