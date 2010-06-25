package temperance.storage;

import java.util.List;

public interface TpGeoPoint {
    
    public long add(Double latitude, Double longitude, int precision, String value);
    
    public String get(Double latitude, Double longitude);

    public List<String> search(Double latitude, Double longitude, int precision);
    
    public boolean delete(Double latitide, Double longitude, int precision);

}
