package kroko;

/*
This maven project is setup to create a .exe based on :
https://stackoverflow.com/questions/69811401/how-to-create-a-standalone-exe-in-java-that-runs-without-an-installer-and-a-jr

 */

import kroko.info.HeaderParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static kroko.info.Utils.*;

public class Converter {
    
    static HashMap<Integer, Integer> mapOffsetByte = new HashMap<>();
    
    public static void main(String[] args){

        HeaderParser parser = new HeaderParser();
        
        RomType romType = null;
        String title = "";
        String inputRom = "";
        String output = "";
        String day = "01";
        String month = "01";

        System.out.printf("SFC to BS Converter %s\n", Version.value);
        
        Mode mode = Mode.CONVERT;
                
        int i = 0;
        while (i<args.length) {
            String arg = args[i];
            i++;
            if (arg.equals("-h") || arg.equals("-help") || arg.equals("--help")) {
                try {
                    printHelp();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            if (arg.equals("-info-sfc")) {
                mode = Mode.INFO_SFC;
                if (i<args.length) {
                    inputRom = args[i];
                }
            }
            if (arg.equals("-info-bs")) {
                mode = Mode.INFO_BS;
                if (i<args.length) {
                    inputRom = args[i];
                }
            }
            if (arg.equals("-lorom") || arg.equals("-lo")) romType = RomType.SFC_LOROM;
            if (arg.equals("-hirom") || arg.equals("-hi")) romType = RomType.SFC_HIROM;
            if (arg.equals("-auto")) romType = null;
            
            if (arg.equals("-title") || arg.equals("-t")) {
                if (i<args.length) title = args[i].substring(0, Math.min(16, args[i].length()));
                i++;
            }
            if (arg.equals("-i") | arg.equals("-input")) {
                if (i<args.length) inputRom = args[i];
                i++;
            }
            if (arg.equals("-o") | arg.equals("-output")) {
                if (i<args.length) output = args[i];
                i++;
            }
            if (arg.equals("-d") | arg.equals("-day")) {
                if (i<args.length) day = args[i];
                i++;
            }
            if (arg.equals("-m") | arg.equals("-month")) {
                if (i<args.length) month = args[i];
                i++;
            }
            if (arg.equals("-b")) {
                int offset = -1;
                int value = -1;
                if (i+1<args.length) {
                    try {
                        offset = Integer.parseInt(args[i], 16);
                        if (offset<0 || offset>0x4F) throw new NumberFormatException("");
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid offset for option -b: "+args[i]);
                    }
                    try {
                        value = Integer.parseInt(args[i+1], 16);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid value for option -b: "+args[i+1]);
                    }
                    mapOffsetByte.put(offset, value);
                } else {
                    System.err.println("Invalid value for option -b.");
                }
                i++;
                i++;
            }
            
        }

        if (mode==Mode.INFO_BS) {
            // add console printer
            parser.addPrintWriter(new PrintWriter(System.out));
            // add file printer for Shift-JIS
            if (output != null && !output.isEmpty()) {
                PrintWriter writer;
                try {
                    writer = new PrintWriter(output, "SHIFT-JIS");
                    parser.addPrintWriter(writer);
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            
            parser.autodetectBsHeaders(inputRom);
            return;
        }
        if (mode == Mode.INFO_SFC) {
            parser.addPrintWriter(new PrintWriter(System.out));
            // add file printer for UTF-8
            if (output != null && !output.isEmpty()) {
                PrintWriter writer;
                try {
                    writer = new PrintWriter(output, StandardCharsets.UTF_8);
                    parser.addPrintWriter(writer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            parser.autodetectSfcHeader(inputRom);
            return;
        }
        
        if (inputRom.isEmpty()) {
            System.err.println("No input rom. Use option -help for more info.");
        } else {
            if (output.isEmpty()) {
                System.err.println("No output rom. Use option -help for more info.");
            } else {
                sfc2bs(title, inputRom, output, day, month, romType);
            }
        }
        
    }

    private static void printHelp() throws IOException {
        InputStream inputStream = Converter.class.getResourceAsStream("/help.txt");
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        for (String line; (line = reader.readLine()) != null;) {
            System.out.println(line);
        }
    }

    public static void sfc2bs(
            String title,
            String inputRom,
            String outputRom,
            String day,
            String month,
            RomType inputRomType) {
        
        byte[] data = readRom(inputRom);

        HeaderParser parser = new HeaderParser();
        
        RomType outputRomType = RomType.BS_LOROM;
        if (inputRomType==RomType.SFC_HIROM) outputRomType = RomType.BS_HIROM;
        
        if (inputRomType == null) {
            inputRomType = parser.autodetectSfcHeader(inputRom);
            if (inputRomType==null)
                inputRomType = RomType.SFC_LOROM;
        }
        int[] sfcHeader = getSfcHeader(data, inputRomType);
        
        parser.addPrintWriter(new PrintWriter(System.out));

        parser.printMessage("---------- Input Header ----------\n");
        parser.printSfcHeader(sfcHeader);
        parser.printMessage("---------- ------------ ----------\n");

        int[] bsHeader = new int[outputRomType.getHeaderLength()];
        for (int i = 0; i < bsHeader.length; i++) {
            bsHeader[i] = data[outputRomType.getOffsetHeader()+i];
        }
        
        int[] defaultValues = {
                0x0F, 0x00, 0x00, 0x00, // Block Allocation Flags
                0x00, 0x00, // Limited Starts
                0xC0, // Date - Month
                0xE0, // Date - Day
                0x20, // ROM Speed (unconfirmed) & Map Mode
                0x00, // File/Execution Type
                0x33, // Fixed (0x33)
                0x02  // Version Number (unconfirmed)
        };
        for (int i = 0; i < defaultValues.length; i++) {
            int defaultValue = defaultValues[i];
            bsHeader[0x20+i] = defaultValue;
        }
        
        // ROM Speed / Mode
        bsHeader[0x28] = sfcHeader[0x15];
        
        // ROM TYPE
        if (outputRomType==RomType.BS_HIROM) {
            bsHeader[0x28] = bsHeader[0x28] | 0x01;
        }
        
        // TITLE
        if (title.isEmpty()) writeBytes(bsHeader, getSfcTitle(sfcHeader), 0x10);
        else writeBytes(bsHeader, getTitle(title), 0x10);
        // DATE
        writeByte(bsHeader, getMonth(month), 0x26);
        writeByte(bsHeader, getDay(day), 0x27);

        // BLOCK ALLOCATION
        byte blockFlag = getBlockFlag(data.length);
        bsHeader[0x20] = blockFlag;

        for (Map.Entry<Integer, Integer> e : mapOffsetByte.entrySet()) {
            bsHeader[e.getKey()] = e.getValue();
        }

        int offset = outputRomType.getOffsetHeader();
        for (int i : bsHeader) {
            data[offset++] = (byte) (i & 0xFF);
        }

        parser.printMessage("---------- Output Header ----------\n");
        parser.printBsHeader(bsHeader);
        parser.printMessage("---------- ------------- ----------\n");
        
        ChecksumCalculator.updateChecksum(data, outputRomType, outputRomType.getOffsetHeader());
        saveData(outputRom, data);
    }

    private static byte[] getSfcTitle(int[] header) {
        byte[] res = new byte[16];
        Arrays.fill(res, (byte) 0x20);
        for (int i=0;i<16;i++) {
            res[i] = (byte) header[i];
        }
        return res;
    }

    /**
     * Converts a String into bytes using ASCII
     */
    public static byte[] getTitle(String title) {
        byte[] res = new byte[16];
        Arrays.fill(res, (byte) 0x20);
        char[] charArray = title.toCharArray();
        for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
            if (i<16) {
                char c = charArray[i];
                res[i] = (byte)c;
            }
        }
        return res;
    }

    

    

    static void saveData(String output, byte[] data) {
        System.out.println("Saving rom: "+output);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(output);
            stream.write(data);
            stream.flush();
            stream.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.flush();
                    stream.close();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}
