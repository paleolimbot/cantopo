package net.fishandwhistle.ctexplorer.backend;

import net.fishandwhistle.ctexplorer.R;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HorizontalProgressDialog extends CustomAlertDialog {

	private ProgressBar pBar ;
	private TextView message ;
	
	public HorizontalProgressDialog(Context context) {
		super(context, R.layout.dialog_progress);
		pBar = (ProgressBar)findViewById(R.id.progress_progress) ;
		message = (TextView)findViewById(R.id.progress_message) ;
	}

	public void setMax(int max) {
		pBar.setMax(max);
	}
	
	public void setProgress(int progress) {
		pBar.setProgress(progress);
	}
	
	public void setIndeterminate(boolean flag) {
		pBar.setIndeterminate(flag);
	}
	
	public void setMessage(String text) {
		message.setText(text);
	}
	
	@Override
	protected Builder getBuilder(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context) ;
		builder.setTitle("Progress") ;
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}) ;
		return builder ;
	}

}
