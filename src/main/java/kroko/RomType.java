package kroko;

public enum RomType {
    
    LOW_ROM(0x7FB0),
    HI_ROM(0xFFB0);

    final int offsetHeader;

    RomType(int offsetHeader) {
        this.offsetHeader = offsetHeader;
    }

    public int getOffsetHeader() {
        return offsetHeader;
    }
}
