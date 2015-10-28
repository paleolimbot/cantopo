package net.fishandwhistle.ctexplorer.webres;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSON {
	private static final Pattern PAT_INTEGER = Pattern.compile("[-+]?[0-9]+|0[Xx][0-9]+");
	private static final Pattern PAT_DOUBLE = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
	private static final Pattern PAT_STRING = Pattern.compile("\"([^\\\\]+\\\\[\"'\\\\])*[^\"]*\"|'([^\\\\]+\\\\[\"'\\\\])*[^']*'");
	private static final Pattern PAT_BOOL = Pattern.compile("(true)|(false)");

	private HashMap<String, String> properties ;
	private HashMap<String, JSON> children ;
	private HashMap<String, ArrayList<Object>> lists ;
	
	public JSON() {
		properties = new HashMap<String, String>() ;
		children = new HashMap<String, JSON>() ;
		lists = new HashMap<String, ArrayList<Object>>() ;
	}
	
	public ArrayList<Object> getList(String key) {
		if(lists.containsKey(key)) {
			return lists.get(key) ;
		} else {
			return null ;
		}
	}
	
	public String getProperty(String key) {
		if(properties.containsKey(key)) {
			return properties.get(key) ;
		} else {
			return null ;
		}
	}
	
	public JSON getChildElement(String key) {
		if(children.containsKey(key)) {
			return children.get(key) ;
		} else {
			return null ;
		}
	}
	

	private static Object parse(String s, int[] start, Matcher integerMatcher, Matcher doubleMatcher, Matcher stringMatcher, Matcher booleanMatcher) {
		char[] c = s.toCharArray();
		skipSpace(s, start);
		if (c[start[0]] == '[') {
			start[0]++;
			ArrayList<Object> a = new ArrayList<Object>();
			if (c[start[0]] == ']') {
				start[0]++;
				return a;
			}
			while (true) {
				a.add(parse(s, start, integerMatcher, doubleMatcher, stringMatcher, booleanMatcher));
				boolean crlf = skipSpace(s, start);
				char p = c[start[0]];
				if (p == ']') {
					start[0]++;
					return a;
				}
				if (p == ',')
					start[0]++;
				else if (!crlf)
					throw new IllegalStateException(", or ] expected");
			}
		} else if (c[start[0]] == '{') {
			start[0]++;
			JSON a = new JSON() ;
			while (true) {
				String field = (String) parse(s, start, integerMatcher, doubleMatcher, stringMatcher, booleanMatcher);
				boolean crlf = skipSpace(s, start);
				if (c[start[0]] == ':') {
					start[0]++;
					Object result = parse(s, start, integerMatcher, doubleMatcher, stringMatcher, booleanMatcher);
					if(result instanceof JSON)
						a.children.put(field, (JSON) result) ;
					else if(result instanceof ArrayList<?>)
						a.lists.put(field, (ArrayList<Object>) result) ;
					else
						a.properties.put(field, result.toString()) ;
					
					crlf = skipSpace(s, start);
				} else
					a.properties.put(field, "");
				char p = c[start[0]];
				if (p == '}') {
					start[0]++;
					return a;
				}
				if (p == ',')
					start[0]++;
				else if (!crlf)
					throw new IllegalStateException(", or } expected at " + start[0]);
			}
		}
		if (doubleMatcher.find(start[0])) {
			String substring = match(start, s, doubleMatcher);
			if (substring != null) return Double.valueOf(substring);
		}
		if (integerMatcher.find(start[0])) {
			String substring = match(start, s, integerMatcher);
			if (substring != null) return Integer.valueOf(substring);
		}
		if (stringMatcher.find(start[0])) {
			String substring = match(start, s, stringMatcher);
			if (substring != null) return substring.substring(1, substring.length() - 1);
		}
		if (booleanMatcher.find(start[0])) {
			String substring = match(start, s, booleanMatcher);
			if (substring != null) return Boolean.valueOf(substring);
		}
		throw new IllegalStateException("unexpected end of data");
	}

	private static String match(int[] start, String s, Matcher matcher) {
		int ms = matcher.start();
		int me = matcher.end();
		if (start[0] == ms) {
			start[0] = me;
			return s.substring(ms, me);
		}
		return null;
	}

	public static boolean skipSpace(String s, int[] start) {
		boolean ret = false;
		while (true) {
			char c = s.charAt(start[0]);
			boolean crlf = (c == '\r') || (c == '\n');
			if ((c != ' ') && !crlf)
				break;
			if (crlf)
				ret = true;
			start[0]++;
		}
		return ret;
	}

	public static JSON parse(String json) {
		Matcher integerMatcher = PAT_INTEGER.matcher(json);
		Matcher doubleMatcher = PAT_DOUBLE.matcher(json);
		Matcher stringMatcher = PAT_STRING.matcher(json);
		Matcher booleanMatcher = PAT_BOOL.matcher(json);
		//noinspection unchecked
		Object o = parse(json, new int[]{0}, integerMatcher, doubleMatcher, stringMatcher, booleanMatcher);
		if(o instanceof JSON)
			return (JSON)o ;
		else
			return null ;
	}
}
