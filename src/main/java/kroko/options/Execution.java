package kroko.options;

public enum Execution {

    FLASH(0b00000000, Option.EXECUTION, "FLASH"),
    PSRAM(0b00100000, Option.EXECUTION, "PSRAM")
    ;
    
    int value;
    public String label;
    public Option option;

    Execution(int value, Option option, String label) {
        this.value = value;
        this.option = option;
        this.label = label;
    }
    
    public boolean test(int b) {
        return (this.option.mask & b) == value;
    }

}
