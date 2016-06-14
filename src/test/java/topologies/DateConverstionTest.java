package topologies;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by mramakrishnan on 5/4/16.
 */
public class DateConverstionTest {
    @Test
    public void convertMMDDYYTimetolong(){
        DateTimeFormatter patternFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        String time= "2016-05-04T11:28:39.2287994-07:00";
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        System.out.println();
        DateTime dtTime= ISODateTimeFormat.dateTime().parseDateTime(nowAsISO);
        System.out.println(dtTime.toDate().toString());
        System.out.println(Long.toString(dtTime.toDate().getTime()));
        System.out.println(new java.util.Date().getTime());
    }


    @Test
    public void testISODateCreation(){
        DateTime dt = new DateTime();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        System.out.println(fmt.print(dt));

    }
}
