import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.List;

public class DosSend {
    final int FECH = 44100; // frÃ©quence d'Ã©chantillonnage
    final int FP = 1000;    // frÃ©quence de la porteuses
    final int BAUDS = 100;  // dÃ©bit en symboles par seconde
    final int FMT = 16 ;    // format des donnÃ©es
    final int MAX_AMP = (1<<(FMT-1))-1; // amplitude max en entier
    final int CHANNELS = 1; // nombre de voies audio (1 = mono)
    final int[] START_SEQ = {1,0,1,0,1,0,1,0}; // sÃ©quence de synchro au dÃ©but
    final Scanner input = new Scanner(System.in); // pour lire le fichier texte

    long taille;                // nombre d'octets de donnÃ©es Ã  transmettre
    double duree ;              // durÃ©e de l'audio
    double[] dataMod;           // donnÃ©es modulÃ©es
    char[] dataChar;            // donnÃ©es en char
    FileOutputStream outStream; // flux de sortie pour le fichier .wav


    /**
     * Constructor
     * @param path  the path of the wav file to create
     */
    public DosSend(String path){
        File file = new File(path);
        try{
            outStream = new FileOutputStream(file);
        } catch (Exception e) {
            System.out.println("Erreur de crÃ©ation du fichier");
        }
    }

    /**
     * Write a raw 4-byte integer in little endian
     * @param octets    the integer to write
     * @param destStream  the stream to write in
     */
    public void writeLittleEndian(int octets, int taille, FileOutputStream destStream){
        char poidsFaible;
        while(taille > 0){
            poidsFaible = (char) (octets & 0xFF);
            try {
                destStream.write(poidsFaible);
            } catch (Exception e) {
                System.out.println("Erreur d'Ã©criture");
            }
            octets = octets >> 8;
            taille--;
        }
    }

    /**
     * Create and write the header of a wav file
     *
     */
    public void writeWavHeader(){
        taille = (long)(FECH * duree);
        long nbBytes = taille * CHANNELS * FMT / 8;

        try  {
            outStream.write(new byte[]{'R', 'I', 'F', 'F'});
            /*
                Ã€ complÃ©ter
            */
        } catch(Exception e){
            System.out.printf(e.toString());
        }
    }


    /**
     * Write the data in the wav file
     * after normalizing its amplitude to the maximum value of the format (8 bits signed)
     */
    public void writeNormalizeWavData(){
        try {
            /*
                Ã€ complÃ©ter
            */
            }
        catch (Exception e) {
            System.out.println("Erreur d'Ã©criture");
        }
    }

    /**
     * Read the text data to encode and store them into dataChar
     * @return the number of characters read
     */
    public int readTextData(){
        String text = input.nextLine();
        dataChar = text.toCharArray();
        return dataChar.length;
    }

    /**
     * convert a char array to a bit array
     * @param chars
     * @return byte array containing only 0 & 1
     */
    public byte[] charToBits(char[] chars){
        byte[] bits = new byte[chars.length*8];
        for (int i = 0; i < chars.length; i++) {
            for (int j = 0; j < 8; j++) {
                bits[i*8+j] = (byte) ((chars[i] >> (7-j)) & 0x01);
            }
        }
        return bits;
    }

    /**
     * Modulate the data to send and apply the symbol throughout via BAUDS and FECH.
     * @param bits the data to modulate
     */
    public void modulateData(byte[] bits){
        /*
            Ã€ complÃ©ter
        */
    }

    /**
     * Display a signal in a window
     * @param sig  the signal to display
     * @param start the first sample to display
     * @param stop the last sample to display
     * @param mode "line" or "point"
     * @param title the title of the window
     */
    public static void displaySig(double[] sig, int start, int stop, String mode, String title){
        StdDraw.enableDoubleBuffering();
        StdDraw.setCanvasSize(1920, 720);
        StdDraw.setXscale(start, stop);
        StdDraw.setTitle(title);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.line(0, -1, 0, 1);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setPenRadius(0.005);       
        if(mode.equals("line")){
            for(int i=start; i<stop-1; i++){
                StdDraw.line(i, sig[i], i+1, sig[i+1]);
            }
        } else if(mode.equals("point")){
            for(int i=start; i<stop; i++){
                StdDraw.point(i, sig[i]);
            }
        }
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.0075);
        StdDraw.line(start, 0.5, stop, 0.5);
        for(int i=start; i<stop; i+=100){
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.line(i, 0.475, i, 0.525);
            StdDraw.filledRectangle(i, 0.450, 30, 0.02);
            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.text(i, 0.450, ""+i);
        }
        StdDraw.show();
    }

    /**
     * Display signals in a window
     * @param listOfSigs  a list of the signals to display
     * @param start the first sample to display
     * @param stop the last sample to display
     * @param mode "line" or "point"
     * @param title the title of the window
     */
    public static void displaySig(List<double[]> listOfSigs, int start, int stop, String mode, String title){
      /*
          Ã€ complÃ©ter
      */
    }


    public static void main(String[] args) {
        // crÃ©Ã© un objet DosSend
        DosSend dosSend = new DosSend("DosOok_message.wav");
        // lit le texte Ã  envoyer depuis l'entrÃ©e standard
        // et calcule la durÃ©e de l'audio correspondant
        dosSend.duree = (double)(dosSend.readTextData()+dosSend.START_SEQ.length/8)*8.0/dosSend.BAUDS;

        // gÃ©nÃ¨re le signal modulÃ© aprÃ¨s avoir converti les donnÃ©es en bits
        dosSend.modulateData(dosSend.charToBits(dosSend.dataChar));
        // Ã©crit l'entÃªte du fichier wav
        dosSend.writeWavHeader();
        // Ã©crit les donnÃ©es audio dans le fichier wav
        dosSend.writeNormalizeWavData();

        // affiche les caractÃ©ristiques du signal dans la console
        System.out.println("Message : "+String.valueOf(dosSend.dataChar));
        System.out.println("\tNombre de symboles : "+dosSend.dataChar.length);
        System.out.println("\tNombre d'Ã©chantillons : "+dosSend.dataMod.length);
        System.out.println("\tDurÃ©e : "+dosSend.duree+" s");
        System.out.println();

        // exemple d'affichage du signal modulÃ© dans une fenÃªtre graphique
        displaySig(dosSend.dataMod, 1000, 3000, "line", "Signal apres modulation");
    }
}