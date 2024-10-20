package kroko;

public enum RomType {
    
    SFC_LOROM(0x7FC0, 0x1E, 0x40),
    SFC_HIROM(0xFFC0, 0x1E, 0x40),
    BS_LOROM(0x7FB0, 0x2E, 0x50),
    BS_HIROM(0xFFB0, 0x2E, 0x50)
    ;

    final int offsetHeader;
    final int offsetChecksum;
    final int headerLength;

    RomType(int offsetHeader, int offsetChecksum, int headerLength) {
        this.offsetHeader = offsetHeader;
        this.offsetChecksum = offsetChecksum;
        this.headerLength = headerLength;
    }

    public int getOffsetHeader() {
        return offsetHeader;
    }

    public int getOffsetChecksum() {
        return offsetChecksum;
    }

    public int getHeaderLength() {
        return headerLength;
    }
}
