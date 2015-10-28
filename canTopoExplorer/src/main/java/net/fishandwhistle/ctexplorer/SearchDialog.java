package net.fishandwhistle.ctexplorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.fishandwhistle.ctexplorer.backend.CustomAlertDialog;
import net.fishandwhistle.ctexplorer.webres.JSON;
import net.fishandwhistle.ctexplorer.webres.WebGeocoder;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import ca.fwe.locations.geometry.Bounds;


public class SearchDialog extends CustomAlertDialog {

	private static final int MAX_RESULTS = 5 ;
	private static final int DELAY_TIME = 500 ; //ms
	private static final String TAG = "SearchDialog" ;

	private AsyncTask <String, Void, List<Address>> geocodeTask ;
	private AutoCompleteTextView text ;
	private ProgressBar progress ;
	private Bounds bounds ;
	private OnAddressSelectedListener listener ;
	private List<Address> currentList ;
	private long lastChar = 0 ;
	private AsyncTask <String, Void, JSON> secondaryGeocodeTask ;
	private int totalGeocodeRequests = 0 ;
	private Launcher launchInfo = null ;

	public interface OnAddressSelectedListener {
		public void onAddressSelected(Address a, Bounds b) ;
	}

	public SearchDialog(Context context) {
		super(context, R.layout.dialog_search);
		text = (AutoCompleteTextView)findViewById(R.id.dialog_geocode_text) ;
		progress = (ProgressBar)findViewById(R.id.dialog_geocode_progress) ;
		text.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				//nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				final String str = s.toString() ;

				long now = System.currentTimeMillis() ;
				long delay = now - lastChar ;
				Log.i(TAG, "new char. delay=" + delay) ;
				if(delay < DELAY_TIME && launchInfo != null) {
					launchInfo.cancelled = true ;
				}
				lastChar = now ;

				if(str.length() > 3) {
					final Launcher l = new Launcher() ;
					l.search = str ;

					text.postDelayed(new Runnable() {
						public void run() {
							if(!l.cancelled) {
								geocodeTask = new GeocoderTask() ;
								geocodeTask.execute(l.search) ;
							}
						}
					}, DELAY_TIME) ;
					launchInfo = l ;
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				//nothing
			}

		});

		text.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				cancel();
				if(listener != null) {
					final Address ad = currentList.get(position) ;
					secondaryGeocode(ad) ;
				}
			}

		});
	}

	public void clearText() {
		text.setText("");
	}

	@Override
	public void show() {
		super.show();
		text.postDelayed(new Runnable() {
			public void run() {
				InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);			}
		}, 250) ;
	}

	public void setOnAddressSelectedListener(OnAddressSelectedListener l) {
		listener = l ;
	}

	private void updateList(List<Address> list) {
		ArrayAdapter<String> a = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line) ;
		if(list != null) {
			for(Address ad: list) {
				a.add(labelFrom(ad));
			}
		}
		text.setAdapter(a);
		a.notifyDataSetChanged();
		currentList = list ;
	}

	private static String labelFrom(Address a) {
		String out = "" ;
		String sep = "" ;
		for(int i=0; i<5; i++) {
			String line = a.getAddressLine(i) ;
			if(line == null)
				break ;
			out += sep + line ;
			sep = " " ;
		}
		if(out.length() > 0)
			return out ;
		else
			return null ;
	}

	public void setBounds(Bounds b) {
		bounds = b ;
	}

	@Override
	protected Builder getBuilder(Context context) {
		Builder b = new Builder(context) ;
		b.setTitle(R.string.search_title) ;
		b.setNegativeButton(R.string.search_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}) ;
		b.setPositiveButton(R.string.search_search, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new GeocoderTask().execute(text.getText().toString(), "true") ;
				Toast.makeText(getContext(), R.string.search_geocoding, Toast.LENGTH_LONG).show();
			}
		}) ;
		return b ;
	}

	private class Launcher {
		boolean cancelled = false ;
		String search = null ;
	}

	private void secondaryGeocode(final Address ad) {
		if(secondaryGeocodeTask != null)
			secondaryGeocodeTask.cancel(false) ;
		secondaryGeocodeTask = new AsyncTask<String, Void, JSON>() {

			@Override
			protected JSON doInBackground(String... params) {
				WebGeocoder wg = new WebGeocoder() ;
				try {
					return wg.geocode(labelFrom(ad), 1) ;
				} catch (IOException e) {
					Log.e(TAG, "error in secondary geocode, returning null", e) ;
					return null ;
				}
			}

			@Override
			protected void onPreExecute() {
				progress.setVisibility(View.VISIBLE);
			}

			@Override
			protected void onPostExecute(JSON result) {
				progress.setVisibility(View.INVISIBLE);
				SearchDialog.this.cancel() ;
				Bounds b = null ;
				if(result != null) {
					ArrayList<Object> results = result.getList("results") ;
					if(results != null && results.size() > 0) {
						JSON geom = ((JSON)results.get(0)).getChildElement("geometry") ;
						if(geom != null) {
							JSON vp = geom.getChildElement("viewport") ;
							if(vp != null) {
								JSON ne = vp.getChildElement("northeast") ;
								JSON sw = vp.getChildElement("southwest") ;
								if(ne != null && sw != null) {
									double minX = Double.valueOf(sw.getProperty("lng")) ;
									double minY = Double.valueOf(sw.getProperty("lat")) ;
									double maxX = Double.valueOf(ne.getProperty("lng")) ;
									double maxY = Double.valueOf(ne.getProperty("lat")) ;
									b = new Bounds(minX, maxX, minY, maxY) ;
								}
							}
						}
					}
				} else {

				}
				listener.onAddressSelected(ad, b);
			}

		} ;
		secondaryGeocodeTask.execute(labelFrom(ad)) ;
	}

	private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

		private boolean executeListener = false ;

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<Address> doInBackground(String... params) {
			executeListener = params.length == 2 ;
			if(!this.isCancelled()) {
				Geocoder g = new Geocoder(getContext()) ;
				try {
					List<Address> out ;
					if(bounds != null) {
						out = g.getFromLocationName(params[0], MAX_RESULTS, 
								bounds.getMinY(), bounds.getMinX(), 
								bounds.getMaxY(), bounds.getMaxX()) ;
					} else {
						out = g.getFromLocationName(params[0], MAX_RESULTS) ;
					}	
					totalGeocodeRequests++ ;
					Log.i(TAG, "received geocode request for " + params[0] + " total:" + totalGeocodeRequests) ;
					return out ;
				} catch(IOException e) {
					Log.e(TAG, "io error on search", e) ;
					return null ;
				}
			} else {
				return null ;
			}
		}

		@Override
		protected void onPostExecute(List<Address> result) {
			progress.setVisibility(View.INVISIBLE);
			if(result != null) {
				if(!executeListener) {
					updateList(result) ;
				} else {
					if(listener != null) {
						if(result.size() > 0) {
							Address ad = currentList.get(0) ;
							secondaryGeocode(ad) ;
						} else {
							Toast.makeText(getContext(), R.string.search_error_noresults, Toast.LENGTH_SHORT).show() ;
						}
					}
				}
			} else {
				Toast.makeText(getContext(), R.string.search_error_nonetwork, Toast.LENGTH_SHORT).show();
			}
		}
	}

}
