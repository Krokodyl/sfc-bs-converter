package kroko.options;

public enum MapMode {

    LOROM(0b00000000, Option.MAP_MODE, "LoROM"),
    HIROM(0b00000001, Option.MAP_MODE, "HiROM"),
    EXHIROM(0b00000101, Option.MAP_MODE, "ExHiROM")
    ;
    
    int value;
    public String label;
    public Option option;

    MapMode(int value, Option option, String label) {
        this.value = value;
        this.option = option;
        this.label = label;
    }
    
    public boolean test(int b) {
        return (this.option.mask & b) == value;
    }

}
