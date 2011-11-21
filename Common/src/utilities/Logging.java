package utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logging {
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static final void Info(Object o)
	{
		Calendar cal =  Calendar.getInstance();;
		System.out.println(String.format("[%s]%s", dateFormat.format(cal.getTime()), o.toString()));
	}
}
