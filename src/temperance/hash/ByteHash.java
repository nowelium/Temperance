package temperance.hash;

public class ByteHash implements Hash {
    
    private final byte[] hash;
    
    private final int size;
    
    public ByteHash(byte[] hash, int size){
        this.hash = hash;
        this.size = size;
    }
    
    public String hashValue(){
        return hexValue();
    }
    
    public String hexValue(){
        final int length = hash.length;
        final StringBuilder buf = new StringBuilder(length * 2);
        for(int i = 0; i < length; ++i){
            byte b = hash[i];
            buf.append(Character.forDigit((b & 0xf0) >> 4, 16)); // div 16
            buf.append(Character.forDigit((b & 0x0f), 16));
        }
        return buf.toString();
    }
    
    public String longValue(){
        long temp = 0L;
        long value = 0L;
        for(short i = 0; i < size; ++i){
            temp = hash[i] & 0xff;
            value |= temp << ((size - 1 - i) * 8);
        }
        value |= temp;
        return Long.toString(value);
    }
}
