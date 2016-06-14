package topologies;

import org.junit.Test;

/**
 * Created by mramakrishnan on 5/3/16.
 */
public class ConversionTest {
    public static final String CDDT1="420.0";
    public static final String CDDT2="620.0";
    public static final String CDDT3="500.0";
    public static final int TIME_WINDOW_SIZE=1000;

    int cdDuration=800;
    String dyDuration= "1000";

    @Test
    public void testDoubleToIntConvertion(){
        Long dyDuration=Long.parseLong(this.dyDuration);
        System.out.println(dyDuration.intValue());
        //Calculate CDDAVE
//        int CDDAVE =  ((((int) ((Double.parseDouble(CDDT1) + Double.parseDouble(CDDT2) + cdDuration)) / 3) * 100 / TIME_WINDOW_SIZE)  / 10);//DTpercentage = (DTtotal / totalDuration) * 100 return (int) DTpercentage / 10;;
//        int CDDAVE =((((int) ((Double.parseDouble(CDDT1) + cdDuration)) / 2 *100)/ TIME_WINDOW_SIZE)  / 10);
        int CDDAVE = ((((int) ((Double.parseDouble(CDDT1) + cdDuration)) / 2) * 100 / (dyDuration).intValue())/10);
//        int CDDAVE =(((int) cdDuration) / TIME_WINDOW_SIZE * 100) / 10;
        System.out.println(CDDAVE);
    }


}
