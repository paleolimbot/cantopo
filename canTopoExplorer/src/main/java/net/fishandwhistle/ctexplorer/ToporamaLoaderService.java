package net.fishandwhistle.ctexplorer;

import net.fishandwhistle.ctexplorer.backend.NTSTileNameSearcher;
import net.fishandwhistle.ctexplorer.backend.ToporamaLoader;
import net.fishandwhistle.ctexplorer.backend.ToporamaLoader.ErrorClass;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import fwe.nts.NTSMapSheet;

public class ToporamaLoaderService extends IntentService {

	private static final String TAG = "ToporamaLoaderService" ;
	
	public static final String ACTION_DOWNLOAD_TOPORAMA = "net.fishandwhistle.ctexplorer.DOWNLOAD_TOPO" ;
	public static final String ACTION_DOWNLOAD_COMPLETE = "net.fishandwhistle.ctexplorer.DOWNLOAD_COMPLETE" ;
	public static final String ACTION_DOWNLOAD_STARTED = "net.fishandwhistle.ctexplorer.DOWNLOAD_STARTED" ;
	public static final String EXTRA_DOWNLOAD_ERROR = "net.fishandwhistle.ctexplorer.DOWNLOAD_ERROR" ;
	
	//uri structure nts:021-h-01
	
	private NTSTileNameSearcher names ;
	
	public ToporamaLoaderService() {
		super("ToporamaLoaderService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "starting loader service with action " + intent.getAction() + " and data " + intent.getData()) ;
		if(intent.getAction().equals(ACTION_DOWNLOAD_TOPORAMA)) {
			Uri data = intent.getData() ;
			if(data != null) {
				String ntsId = data.getSchemeSpecificPart() ;
				final NTSMapSheet sheet = NTSMapSheet.getSheetById(ntsId) ;
				if(sheet != null) {
					if(names == null)
						names = new NTSTileNameSearcher(this) ;
					String name = names.getName(sheet) ;
					String add = "" ;
					if(name != null) {
						add = " (" + name + ")" ;
					}
					final int notificationId = sheet.getNtsId().hashCode() ;
					final String notificationTitle = String.format(getString(R.string.loader_title), ntsId, add) ;
					final ToporamaLoader l = new ToporamaLoader(this, sheet, true) ;
					ErrorClass result = l.load(new ToporamaLoader.ProgressCallback() {
						
						@Override
						public void onProgress(int prog) {
							//update notification
							Log.i(TAG, "updating progress: " + prog) ;
							NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE) ;
							Notification.Builder b = new Notification.Builder(ToporamaLoaderService.this) ;
							b.setOngoing(true) ;
							b.setContentTitle(notificationTitle) ;
							b.setSmallIcon(R.drawable.ic_stat_topoload) ;
							switch(l.getCurrentTask()) {
							case COMPLETED:
								return ;
							case DOWNLOAD_HIRES:
								b.setContentText(getString(R.string.loader_hires)) ;
								b.setProgress(100, prog, false) ;
								break;
							case DOWNLOAD_LOWRES:
								b.setContentText(getString(R.string.loader_lowres)) ;
								b.setProgress(100, prog, false) ;
								break;
							case INITIALIZE:
								Intent i = new Intent(ACTION_DOWNLOAD_STARTED) ;
								i.setData(Uri.parse("nts:"+sheet.getNtsId())) ;
								sendBroadcast(i) ;
								return ;
							case PROCESSING:
								b.setContentText(getString(R.string.loader_processing)) ;
								b.setProgress(0, 0, true) ;
								break;
							}
							nm.notify(notificationId, b.getNotification());
						}
					}) ;
					
					NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE) ;
					nm.cancel(notificationId);
					
					Intent i = new Intent(ACTION_DOWNLOAD_COMPLETE) ;
					i.setData(Uri.parse("nts:"+sheet.getNtsId())) ;
					i.putExtra(EXTRA_DOWNLOAD_ERROR, result.toString()) ;
					sendBroadcast(i) ;
				} else {
					//could not find sheet
				}
			} else {
				//no data
			}
		} else {
			//unrecognized action
		}
	}

}
