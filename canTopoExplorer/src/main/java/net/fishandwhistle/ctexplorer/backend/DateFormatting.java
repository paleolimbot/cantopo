package net.fishandwhistle.ctexplorer.backend;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;

public class DateFormatting {

	public static SimpleDateFormat getDateFormat(Context context) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM yyyy", Locale.CANADA) ;
		
		return sdf ;
	}
	
	public static SimpleDateFormat getDateTimeFormat(Context context) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM yyyy h:mm a", Locale.CANADA) ;
		
		return sdf ;
	}

}
