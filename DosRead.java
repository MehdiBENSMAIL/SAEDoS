// coding : UTF-8
// SAE-01 DoS 2024
// BEN SMAIL Mehdi - C1
// FROEHLY Jean-Baptiste - B2

import java.io.*;
import java.awt.Frame;
import java.awt.FileDialog;

public class DosRead {
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
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Helper method to convert a little-endian byte array to an integer
   * @param bytes  the byte array to convert
   * @param offset the offset in the byte array
   * @param fmt    the format of the integer (16 or 32 bits)
   * @return the integer value
   */
  private static int byteArrayToInt(byte[] bytes,
      int offset, int fmt) {
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
   * @param n the number of samples to average
   */
  public void audioLPFilter(int n) {
    // On crée le tableau de sortie
    double[] output = new double[audio.length];
    // On parcourt le tableau de sortie
    for (int i = 0; i < output.length; i++) {
      // On calcule la moyenne des échantillons
      double sum = 0;
      for (int j = 0; j < n; j++) {
        if (i - j >= 0) {
          sum += audio[i - j];
        }
      }
      output[i] = sum / n;
    }
    audio = output;
  }

  /**
   * Resample the audio array and apply a threshold
   * @param period    the number of audio samples by symbol
   * @param threshold the threshold that separates 0 and 1
   */
  public void audioResampleAndThreshold(int period, int threshold){
    int newLength = audio.length / period;
    double[] resampledAudio = new double[newLength];
    outputBits = new int[newLength];

    for (int i = 0; i < newLength; i++) {
        // Calculate the average value for each period
        double sum = 0;
        for (int j = 0; j < period; j++) {
            int index = i * period + j;
            if (index < audio.length) {
                sum += audio[index];
            }
        }
        resampledAudio[i] = sum / period;

        // Apply threshold and convert to binary values
        outputBits[i] = (resampledAudio[i] >= threshold) ? 1 : 0;
    }
  }

  /**
   * Decode the outputBits array to a char array.
   * The decoding is done by comparing the START_SEQ with
   * the actual beginning of outputBits.
   * The next first symbol is the first bit of the first char.
   */
  public void decodeBitsToChar() {
    int start = 0;
    int i = 0;

    // Find the first START_SEQ
    while (i < outputBits.length - START_SEQ.length) {
      boolean found = true;
      for (int j = 0; j < START_SEQ.length; j++) {
        if (outputBits[i + j] != START_SEQ[j]) {
          found = false;
          break;
        }
      }
      if (found) {
        start = i + START_SEQ.length;
        break;
      }
      i++;
    }
    // If no START_SEQ was found, return
    if (start == 0) {
      System.out.println("Pas de séquence de début trouvée");
      return;
    }
    // Decode the bits to chars
    decodedChars = new char[(outputBits.length - start) / 8];
    for (int j = 0; j < decodedChars.length; j++) {
      int value = 0;
      for (int k = 0; k < 8; k++) {
        value += outputBits[start + j * 8 + k] << (7 - k);
      }
      decodedChars[j] = (char) value;
    }
  }


  /**
   * Print the elements of an array
   * @param data the array to print
  */
  public static void printIntArray(char[] data) {
    if (data == null || data.length == 0) {
      System.out.println("Le tableau est vide");
      return;
    }
    for (int i = 0; i < data.length; i++) {
      System.out.print(data[i]);
    }
    System.out.println();
  }

  /**
   * Display a signal in a window
   * @param sig   the signal to display
   * @param start the first sample to display
   * @param stop  the last sample to display
   * @param mode  "line" or "point"
   * @param title the title of the window
   */
  public static void displaySig(double[] sig, int start, int stop,
      String mode, String title) {
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
   * Display a button that
   * reveals the file explorer upon getting clicked.
   * @return the name of the selected file
   */
  public static String graphicalInterface() {
    StdDraw.enableDoubleBuffering();
    StdDraw.setCanvasSize(1280, 720);
    StdDraw.setXscale(0, 1280);
    StdDraw.setYscale(0, 720);
    StdDraw.setTitle("Graphical Interface");
    StdDraw.clear(StdDraw.BLACK);
    StdDraw.setPenColor(StdDraw.WHITE);
    StdDraw.setPenRadius(0.005);

    while (true) {
      // create a button to open the file explorer
      StdDraw.rectangle(640, 360, 200, 50);
      StdDraw.text(640, 360, "Open a file");
      StdDraw.show();
      // wait for the user to click the button
      while (!StdDraw.isMousePressed()) {
        StdDraw.pause(100);
      }
      // get the position of the click
      double x = StdDraw.mouseX();
      double y = StdDraw.mouseY();
      // if the click is inside the button
      if (x > 440 && x < 840 && y > 310 && y < 410) {
        // open the file explorer
        FileDialog fd = new FileDialog((Frame) null,
            "Choose a file", FileDialog.LOAD);
        fd.setVisible(true);
        String filename = fd.getFile();
        if (filename == null) {
          System.out.println("Aucun fichier choisi.");
        } else {
          // Check if the selected file is a .wav file
          if (filename.endsWith(".wav")) {
            System.out.println("Vous avez choisi : " + filename);
            return filename;
          } else {
            System.out.println("Mauvais format (.wav)");
          }
        }
      }
      // Clear the screen for the next iteration
      StdDraw.clear(StdDraw.BLACK);
    }
  }

  /**
   * Un exemple de main qui doit pourvoir être exécuté avec les méthodes
   * que vous aurez conçues.
   */
  public static void main(String[] args) {
    String wavFilePath;
    if (args.length != 1) {
      // No command line argument provided, use graphical interface
      wavFilePath = graphicalInterface();
    } else {
      // Command line argument provided, use it as the file path
      wavFilePath = args[0];
    }

    if (wavFilePath == null) {
      System.out.println("Aucun fichier choisi.");
      return;
    }

    // Open the WAV file and read its header!
    DosRead dosRead = new DosRead();
    dosRead.readWavHeader(wavFilePath);

    // Print the audio data properties
    System.out.println("Fichier audio : " + wavFilePath);
    System.out.println("\tFrequence d'echantillonage : "
        + dosRead.sampleRate + " Hz");
    System.out.println("\tBits par echantillon : "
        + dosRead.bitsPerSample + " bits");
    System.out.println("\tTaille : " + dosRead.dataSize + " bytes");

    // Read the audio data
    dosRead.readAudioDouble();
    // reverse the negative values
    dosRead.audioRectifier();
    // apply the low pass filter
    dosRead.audioLPFilter(44);
    // Resample audio data and apply a threshold to output only 0 & 1
    dosRead.audioResampleAndThreshold(dosRead.sampleRate / BAUDS, 12000);
    dosRead.decodeBitsToChar();
    if (dosRead.decodedChars != null) {
      System.out.print("\t------------------------------\n");
      System.out.print("\tMessage : ");
      printIntArray(dosRead.decodedChars);
    }
    displaySig(dosRead.audio, 0, dosRead.audio.length-1,
        "line", "Signal audio");
    // Close the file input stream
    try {
      dosRead.fileInputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
