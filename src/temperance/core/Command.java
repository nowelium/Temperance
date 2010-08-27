package temperance.core;

public abstract class Command {
    
    public static final int SPLIT;
    
    static {
        // TODO: configure value
        String splitSize = System.getProperty("temperance.memc.split_size", "100");
        int splitSizeValue = 100;
        try {
            splitSizeValue = Integer.parseInt(splitSize);
        } catch(NumberFormatException e){
            // nop
        }
        SPLIT = splitSizeValue;
    }

}
