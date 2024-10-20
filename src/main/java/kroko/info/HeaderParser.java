package kroko.info;

import kroko.RomType;
import kroko.options.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static kroko.info.Utils.*;

public class HeaderParser {

    List<PrintWriter> writers = new ArrayList<>(); 
    
    public HeaderParser() {
    }
    
    public void addPrintWriter(PrintWriter writer) {
        writers.add(writer);
    }
    
    public void printMessage(String message) {
        for (PrintWriter writer : writers) {
            writer.print(message);
            writer.flush();
        }
    }
    
    public void flushWriters() {
        for (PrintWriter writer : writers) {
            writer.flush();
        }
    }
    
    public void closeWriters() {
        for (PrintWriter writer : writers) {
            writer.close();
        }
    }

    public void printSfcHeader(int[] header) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (int i : header) {
            stream.write((byte)(i & 0xFF));
        }
        printSfcHeader(stream.toByteArray());
    }
    
    public void printSfcHeader(byte[] header) {
        int expectedSize = RomType.SFC_LOROM.getHeaderLength();
        if (header.length==expectedSize) {
            String title;
            title = new String(Arrays.copyOfRange(header, 0, 21), StandardCharsets.UTF_8);

            printMessage(String.format("Title: %s\n", title));

            for (RomSpeed speed : RomSpeed.values()) {
                byte value = header[0x15];
                if (speed.test(value)) printMessage(String.format("%s: %s (%2s)\n", speed.option.label, speed.label, h2(value)));
            }

            for (MapMode mapMode : MapMode.values()) {
                byte value = header[0x15];
                if (mapMode.test(value)) printMessage(String.format("%s: %s (%2s)\n", mapMode.option.label, mapMode.label, h2(value)));
            }

        } else {
            System.err.printf("Incorrect SFC header size: %s (expected %s)\n", header.length, expectedSize);
        }
    }


    public void printBsHeader(int[] header) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (int i : header) {
            stream.write((byte)(i & 0xFF));
        }
        printBsHeader(stream.toByteArray());
    }
    
    public void printBsHeader(byte[] header) {
        if (header.length==RomType.BS_LOROM.getHeaderLength()) {
            String title;
            byte value;
            
            try {
                title = new String(Arrays.copyOfRange(header, 0x10, 0x10+16), "SHIFT-JIS");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            String message = String.format("Title: %s\n", title);
            printMessage(message);

            value = header[0x20];
            printMessage(String.format("Blocks: %s (%8s)\n", getBlockCount(value), b8(value)));

            for (BootLimit boot : BootLimit.values()) {
                value = header[0x24];
                if (boot.test(value)) printMessage(String.format("%s: %s (%2s)\n", boot.option.label, boot.label, h2(value)));
            }

            value = header[0x26];
            printMessage(String.format("Month: %s (%2s)\n", getMonth(value), h2(value)));
            value = header[0x27];
            printMessage(String.format("Day: %s (%2s)\n", getDay(value), h2(value)));

            value = header[0x28];
            for (RomSpeed speed : RomSpeed.values()) {
                if (speed.test(value)) printMessage(String.format("%s: %s (%2s)\n", speed.option.label, speed.label, h2(value)));
            }
            for (MapMode mapMode : MapMode.values()) {
                if (mapMode.test(value)) printMessage(String.format("%s: %s (%2s)\n", mapMode.option.label, mapMode.label, h2(value)));
            }

            value = header[0x29];
            for (SoundlinkRadio setting : SoundlinkRadio.values()) {
                if (setting.test(value)) printMessage(String.format("%s: %s (%2s)\n", setting.option.label, setting.label, h2(value)));
            }
            for (Execution setting : Execution.values()) {
                if (setting.test(value)) printMessage(String.format("%s: %s (%2s)\n", setting.option.label, setting.label, h2(value)));
            }
            for (StGigaIntro setting : StGigaIntro.values()) {
                if (setting.test(value)) printMessage(String.format("%s: %s (%2s)\n", setting.option.label, setting.label, h2(value)));
            }
        


        } else {
            System.err.printf("Incorrect BS header size: %s\n", header.length);
        }
    }
    
    public RomType autodetectSfcHeader(String inputRom) {

        byte[] data = readRom(inputRom);

        RomType romType;

        // Checking sfc
        romType = RomType.SFC_LOROM;
        int checksum = readWord(data, romType.getOffsetHeader()+romType.getOffsetChecksum());
        int complement = readWord(data, romType.getOffsetHeader()+romType.getOffsetChecksum()-2);
        if (checksum+complement==0xFFFF) {
            byte[] header = Arrays.copyOfRange(data, romType.getOffsetHeader(), romType.getOffsetHeader() + romType.getHeaderLength());
            
            printMessage(String.format("----- SFC header found (offset %s) -----\n", h5(romType.getOffsetHeader())));
            printSfcHeader(header);
            printMessage("----- ------------------------------- -----\n");
                
            return romType;
            
        }
        romType = RomType.SFC_HIROM;
        checksum = readWord(data, romType.getOffsetHeader()+romType.getOffsetChecksum());
        complement = readWord(data, romType.getOffsetHeader()+romType.getOffsetChecksum()-2);
        if (checksum+complement==0xFFFF) {
            byte[] header = Arrays.copyOfRange(data, romType.getOffsetHeader(), romType.getOffsetHeader() + romType.getHeaderLength());

            printMessage(String.format("----- SFC header found (offset %s) -----\n", h5(romType.getOffsetHeader())));
            printSfcHeader(header);
            printMessage("----- ------------------------------- -----\n");
            
            return romType;
        }

        return null;
    }

    public void autodetectBsHeaders(String inputRom) {

        byte[] data = readRom(inputRom);

        RomType romType;
        int checksum;
        int complement;
        
        boolean found = false;

        // Checking bs
        int block = 0;
        while (block<8) {
            int blockOffset = block*0x20000;
            if (blockOffset < data.length) {
                romType = RomType.BS_LOROM;
                checksum = readWord(data, blockOffset + romType.getOffsetHeader() + romType.getOffsetChecksum());
                complement = readWord(data, blockOffset + romType.getOffsetHeader() + romType.getOffsetChecksum() - 2);
                if (checksum + complement == 0xFFFF) {
                    byte[] header = Arrays.copyOfRange(data, blockOffset + romType.getOffsetHeader(), blockOffset + romType.getOffsetHeader() + romType.getHeaderLength());
                    found = true;

                    printMessage(String.format("----- BS header found (offset %s) -----\n", h5(blockOffset + romType.getOffsetHeader())));
                    printBsHeader(header);
                    printMessage("----- ------------------------------ -----\n");
                }
                romType = RomType.BS_HIROM;
                checksum = readWord(data, blockOffset + romType.getOffsetHeader() + romType.getOffsetChecksum());
                complement = readWord(data, blockOffset + romType.getOffsetHeader() + romType.getOffsetChecksum() - 2);
                if (checksum + complement == 0xFFFF) {
                    byte[] header = Arrays.copyOfRange(data, blockOffset + romType.getOffsetHeader(), blockOffset + romType.getOffsetHeader() + romType.getHeaderLength());
                    found = true;
                    printMessage(String.format("----- BS header found (offset %s) -----\n", h5(blockOffset + romType.getOffsetHeader())));
                    printBsHeader(header);
                    printMessage("----- ------------------------------ -----\n");
                }

            }
            block++;
        }

        if (!found) printMessage("No valid BS header found.\n");
        flushWriters();
        closeWriters();
    }
    
}
