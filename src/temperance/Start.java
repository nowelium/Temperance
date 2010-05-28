package temperance;

public class Start {
    public static void main(String...args){
        StartStop st = new StartStop();
        st.start(StartStop.createCliOptions(), args);
    }
}
