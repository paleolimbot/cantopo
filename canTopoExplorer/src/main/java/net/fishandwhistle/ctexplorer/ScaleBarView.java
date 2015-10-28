package net.fishandwhistle.ctexplorer;

import net.fishandwhistle.ctexplorer.backend.Units;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class ScaleBarView extends View {

	public static final String[] UNITS_SMALL = {"cm", "in"} ;
	public static final String[] UNITS_MED = {"m", "ft"} ;
	public static final String[] UNITS_LG = {"km", "mi"} ;

	private int widthHint ;
	private int height ;

	private int unitCategory ;
	private double mPerPixel ;
	private double[] scaleParameters ;

	private String labelText ;
	private float[] labelAnchor ;
	private int labelHeight ;
	private int labelPadding ;

	private Path path ;

	private Paint textPaint ;
	private Paint tickPaint ;

	public ScaleBarView(Context context) {
		super(context) ;
		init() ;
	}

	public ScaleBarView(Context context, AttributeSet attrs) {
		super(context, attrs) ;
		init() ;
	}

	public ScaleBarView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init() ;
	}

	private void init() {
		mPerPixel = 0; 
		unitCategory = Units.getUnitCategoryConstant(getContext()) ;
		labelHeight = 20 ;
		labelPadding = 5 ;

		this.setPaint();
	}

	private void setPaint() {
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG) ;
		textPaint.setTextSize(labelHeight);
		textPaint.setTextAlign(Align.RIGHT);

		tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG) ;
		tickPaint.setStyle(Paint.Style.STROKE);
		tickPaint.setStrokeWidth(2);
	}

	public void setUnitCategory(int cat) {
		this.unitCategory = cat ;
		this.refreshScale(mPerPixel);
	}
	
	public void refreshUnitCategory() {
		this.setUnitCategory(Units.getUnitCategoryConstant(getContext()));
	}

	public void refreshScale(double mPerPixel) {
		this.mPerPixel = mPerPixel ;
		this.requestLayout();
		this.invalidate();
	}

	private void recalculate() {
		double availableWidth = 0.8 * widthHint ;
		double geoWidthM = mPerPixel * availableWidth ;
		String[] units ;
		if(geoWidthM < 1) {
			units = UNITS_SMALL ;
		} else if(geoWidthM < 1600) {
			units = UNITS_MED ;
		} else {
			units = UNITS_LG ;
		}

		String unit = units[unitCategory] ;
		double widthHintU = Units.fromSI(geoWidthM, unit) ;
		double tenFactor = Math.floor(Math.log10(widthHintU)) ;
		double widthInTens = Math.floor(widthHintU / Math.pow(10, tenFactor)) ;
		if(widthInTens == 1) {
			widthInTens = 10 ;
			tenFactor -= 1 ;
		} else if(widthInTens == 7) {
			widthInTens = 6 ;
		} else if(widthInTens == 9) {
			widthInTens = 8 ;
		}


		int majDivTens ;
		if(widthInTens < 6) {
			majDivTens = 1 ;
		} else {
			majDivTens = 2 ;
		}

		double widthU = widthInTens * Math.pow(10, tenFactor) ;
		double majorDiv = majDivTens * Math.pow(10, tenFactor) ;
		long majorDivs = Math.round(widthU / majorDiv) ;
		double widthPx = Units.toSI(widthU, unit) / mPerPixel ;
		double majorDivPx = widthPx / majorDivs ;
		this.scaleParameters = new double[] {widthU, majorDiv, widthPx, majorDivPx} ;
		this.labelText = String.valueOf(Math.round(widthU)) + " " + unit ;
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(mPerPixel > 0) {
			widthHint = MeasureSpec.getSize(widthMeasureSpec) ;
			height = MeasureSpec.getSize(heightMeasureSpec) ;
			this.recalculate();
			int widthPx = (int)Math.round(scaleParameters[2]) ;

			int w = resolveSizeAndState(widthPx + this.getPaddingLeft() + this.getPaddingRight(), widthMeasureSpec, 1);
			widthHint = MeasureSpec.getSize(w) - this.getPaddingLeft() - this.getPaddingRight();
			this.recalculate();
			this.setPath();
			this.setMeasuredDimension(w, heightMeasureSpec) ;
		} else {
			this.setMeasuredDimension(0, 0);
		}
	}

	@Override
	public void draw(Canvas canvas) {
		if(path != null) {
			canvas.drawPath(path, tickPaint);
			canvas.drawText(labelText, labelAnchor[0], labelAnchor[1], textPaint);
		}
	}

	private void setPath() {
		int leftScaleBar = this.getPaddingLeft() ;
		int topScaleBar = this.getPaddingTop() + labelHeight + labelPadding;
		int bottomScaleBar = height - this.getPaddingBottom() ;
		float majorDivHeight = (bottomScaleBar - topScaleBar) * 0.5f ;

		float widthPx = (float)scaleParameters[2] ;
		float majorDivPx = (float)scaleParameters[3] ;
		
		//draw outline
		Path path = new Path() ;
		path.moveTo(leftScaleBar, topScaleBar);
		path.lineTo(leftScaleBar, bottomScaleBar);
		path.lineTo(leftScaleBar+widthPx, bottomScaleBar);
		path.lineTo(leftScaleBar+widthPx, topScaleBar) ;

		//draw major divisions ;
		int majorDivCount = Math.round(widthPx / majorDivPx) ;
		for(int i=1; i<majorDivCount; i++) {
			float pxX = i * majorDivPx + leftScaleBar ;
			path.moveTo(pxX, bottomScaleBar);
			path.lineTo(pxX, bottomScaleBar - majorDivHeight);
		}
		this.path = path ;
		float labelAnchorX = this.getPaddingRight() + this.getPaddingLeft() + widthPx ;
		float labelAnchorY = this.getPaddingTop() + labelHeight ;
		labelAnchor = new float[] {labelAnchorX, labelAnchorY} ;
	}



}
