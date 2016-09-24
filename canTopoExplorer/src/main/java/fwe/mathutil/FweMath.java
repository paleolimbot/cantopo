package fwe.mathutil;

import java.util.List;

public class FweMath {
	public static final String DECIMAL = "." ;
	public static final String ZERO = "0" ;
	
	
	public static String addZeros(int number, int length) {
		if(number >= 0) {
			return addZeros(new Integer(number).toString(), length) ;
		} else {
			return null ;
		}
	}
	public static String addZeros(String string, int length) {
		if(string != null && string.length() > 0) {
			String trimmed = string.trim() ;
			if(trimmed.length() == length) {
				return trimmed ;
			} else if(trimmed.length() > length) {
				//return the end of the string
				return trimmed.substring(trimmed.length()-length) ;
			} else { //trimmed.length() < length
				int numberOfZeros = length - trimmed.length() ;
				String out = "" ;
				for(int i=1; i<=numberOfZeros; i++) {
					out += "0" ;
				}
				out += trimmed ;
				return out ;
			}
		} else {
			return null ;
		}
	}
	
	public static RegressionResults regression(List<Double> x, List<Double> y) {
		if(x.size() == y.size() && x.size() >= 2) {
			
	        int n = x.size() ;

	        // first pass: read in data, compute xbar and ybar
	        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
	        for(int i=0; i<n; i++) {
	        	double xVal = x.get(i) ;
	        	double yVal = y.get(i) ;
	            sumx  += xVal ;
	            sumx2 += xVal * xVal;
	            sumy  += yVal ;
	        }
	        
	        double xbar = sumx / n;
	        double ybar = sumy / n;

	        // second pass: compute summary statistics
	        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
	        for (int i = 0; i < n; i++) {
	        	double xVal = x.get(i) ;
	        	double yVal = y.get(i) ;
	            xxbar += (xVal - xbar) * (xVal - xbar);
	            yybar += (yVal - ybar) * (yVal - ybar);
	            xybar += (xVal - xbar) * (yVal - ybar);
	        }
	        double beta1 = xybar / xxbar;
	        double beta0 = ybar - beta1 * xbar;

	        // print results
	        //System.out.println("y   = " + beta1 + " * x + " + beta0);

	        // analyze results
	        int df = n - 2;
	        double rss = 0.0;      // residual sum of squares
	        double ssr = 0.0;      // regression sum of squares
	        for (int i = 0; i < n; i++) {
	        	double xVal = x.get(i) ;
	        	double yVal = y.get(i) ;
	        	
	            double fit = beta1*xVal + beta0;
	            rss += (fit - yVal) * (fit - yVal);
	            ssr += (fit - ybar) * (fit - ybar);
	        }
	        double R2    = ssr / yybar;
//	        double svar  = rss / df;
//	        double svar1 = svar / xxbar;
//	        double svar0 = svar/n + xbar*xbar*svar1;
//	        System.out.println("R^2                 = " + R2);
//	        System.out.println("std error of beta_1 = " + Math.sqrt(svar1));
//	        System.out.println("std error of beta_0 = " + Math.sqrt(svar0));
//	        svar0 = svar * sumx2 / (n * xxbar);
//	        System.out.println("std error of beta_0 = " + Math.sqrt(svar0));
//
//	        System.out.println("SSTO = " + yybar);
//	        System.out.println("SSE  = " + rss);
//	        System.out.println("SSR  = " + ssr);
	        
	        return new RegressionResults(beta1, beta0, R2) ;
			
		} else {
			return null ;
		}		
	}
	
	public static class RegressionResults {
		
		public RegressionResults(double m, double b, double r2) {
			this.m = m;
			this.b = b;
			this.r2 = r2;
		}
		public double m ;
		public double b ;
		public double r2 ;
		
		public double value(double x) {
			return m * x + b ;
		}
		
	}
	
}
