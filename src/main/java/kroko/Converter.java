package kroko;

/*
This maven project is setup to create a .exe based on :
https://stackoverflow.com/questions/69811401/how-to-create-a-standalone-exe-in-java-that-runs-without-an-installer-and-a-jr

 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Converter {

    
    static HashMap<Integer, Integer> mapOffsetByte = new HashMap<>();
    
    public static void main(String[] args){
        System.out.println("SFC to BS Converter 1.0");
        
        RomType romType = RomType.LOW_ROM;
        String title = "";
        String inputRom = "";
        String outputRom = "";
        String day = "28";
        String month = "12";
                
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
            if (arg.equals("-lo")) romType = RomType.LOW_ROM;
            if (arg.equals("-hi")) romType = RomType.HI_ROM;
            if (arg.equals("-title") || arg.equals("-t")) {
                if (i<args.length) title = args[i].substring(0, Math.min(16, args[i].length()));
                i++;
            }
            if (arg.equals("-i") | arg.equals("-input")) {
                if (i<args.length) inputRom = args[i];
                i++;
            }
            if (arg.equals("-o") | arg.equals("-output")) {
                if (i<args.length) outputRom = args[i];
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

        if (inputRom.isEmpty()) {
            System.err.println("No input rom. Use option -i rom.sfc");
        } else {
            if (outputRom.isEmpty()) {
                System.err.println("No output rom. Use option -o rom.bs");
            } else {
                sfc2bs(title, inputRom, outputRom, day, month, romType);
            }
        }
        
    }

    private static void printHelp() throws IOException {
        // java.io.InputStream
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
            RomType romType) {
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(new File(inputRom).toPath());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        
        int[] header = getSfcHeader(data, romType);
        
        int[] defaultValues = {
                0x0F, 0x00, 0x00, 0x00, // Block Allocation Flags
                0x00, 0x00, // Limited Starts
                0xC0, // Date - Month
                0xE0, // Date - Day
                0x20, // ROM Speed (unconfirmed) & Map Mode
                0x20, // File/Execution Type
                0x33, // Fixed (0x33)
                0x02  // Version Number (unconfirmed)
        };
        for (int i = 0; i < defaultValues.length; i++) {
            int defaultValue = defaultValues[i];
            header[0x20+i] = defaultValue;
        }
        
        // TITLE
        if (title.isEmpty()) writeBytes(header, getSfcTitle(header), 0x10);
        else writeBytes(header, getTitle(title), 0x10);
        // DATE
        writeByte(header, getMonth(month), 0x26);
        writeByte(header, getDay(day), 0x27);

        // BLOCK ALLOCATION
        byte blockFlag = getBlockFlag(data.length);
        header[0x20] = blockFlag;

        for (Map.Entry<Integer, Integer> e : mapOffsetByte.entrySet()) {
            header[e.getKey()] = e.getValue();
        }

        int offset = romType.getOffsetHeader();
        for (int i : header) {
            data[offset++] = (byte) (i & 0xFF);
        }

        ChecksumCalculator.updateChecksumBS(data, romType);
        saveData(outputRom, data);
    }

    private static byte[] getSfcTitle(int[] header) {
        byte[] res = new byte[16];
        Arrays.fill(res, (byte) 0x20);
        for (int i=0;i<16;i++) {
            res[i] = (byte) header[0x10+i];
        }
        return res;
    }

    public static int[] getSfcHeader(byte[] data, RomType type) {
        int[] header = new int[0x50];
        for (int i=0;i<header.length;i++) {
            header[i] = data[type.getOffsetHeader()+i];
        }
        return header;
    }

    public static void writeBytes(int[] target, byte[] bytes, int offset) {
        for (byte b : bytes) {
            target[offset++] = b;
        }
    }
    
    public static void writeByte(int[] target, byte b, int offset) {
        target[offset] = b;
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

    public static byte getDay(String day) {
        try {
            int i = Integer.parseInt(day);
            if (i<=31) {
                byte b = (byte) (i << 3);
                return b;
            } else System.err.println("Invalid value for -day: "+day);
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for -day: "+day);
        }
        return (byte) 0xE0;
    }

    public static byte getMonth(String s) {
        try {
            int i = Integer.parseInt(s);
            if (i<=12) {
                byte b = (byte) (i << 4);
                return b;
            } else System.err.println("Invalid value for -month: "+s);
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for -month: "+s);
        }
        return (byte) 0xC0;
    }

    public static byte getBlockFlag(int romSize) {
        int blockCount = romSize/0x20000;
        if (blockCount*0x20000<romSize) blockCount++;
        byte res = 1;
        while (--blockCount>0) {
            res = (byte) ((res << 1) + 1);
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
