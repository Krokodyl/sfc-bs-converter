package kroko.options;

public enum RomSpeed {

    SLOWROM(0b00000000, Option.ROM_SPEED, "SlowRom"),
    FASTROM(0b00010000, Option.ROM_SPEED, "FastRom")
    ;
    
    int value;
    public String label;
    public Option option;

    RomSpeed(int value, Option option, String label) {
        this.value = value;
        this.option = option;
        this.label = label;
    }
    
    public boolean test(int b) {
        return (this.option.mask & b) == value;
    }

}
