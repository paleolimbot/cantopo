package net.fishandwhistle.ctexplorer.backend;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.preference.PreferenceManager;

public class Units {

	public static final int UNITS_METRIC = 0 ;
	public static final int UNITS_IMPERIAL = 1 ;
	
	private static Map<String, Double> values = new HashMap<String, Double>() ;
	
	static {
		values.put("m", 1.0) ;
		values.put("cm", 0.01) ;
		values.put("km", 1000.0) ;
		values.put("mi", 1609.34) ;
		values.put("ft", 0.3048) ;
		values.put("in", 0.0254) ;
		
		values.put("m2", 1.0) ;
		values.put("ha", 10000.0) ;
		values.put("km2", 1e6) ;
		values.put("acres", 4046.86) ;
		values.put("mi2", 2.59e6) ;
	}
	
	public static double fromSI(double valueSI, String toUnit) {
		return valueSI / values.get(toUnit) ;
	}
	
	public static double toSI(double valueInUnit, String unit) {
		return valueInUnit * values.get(unit) ;
	}
	
	public static int getUnitCategoryConstant(Context context) {
		String pref = PreferenceManager.getDefaultSharedPreferences(context).getString("xml_units", "0") ;
		return Integer.valueOf(pref) ;
	}
}
