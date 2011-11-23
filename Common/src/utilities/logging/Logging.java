package utilities.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;

public class Logging {
	private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public static void log(Object object) {
		Calendar calendar = Calendar.getInstance();;

		System.out.println(String.format("[%s]%s", dateFormat.format(calendar.getTime()), object.toString()));
	}
}
