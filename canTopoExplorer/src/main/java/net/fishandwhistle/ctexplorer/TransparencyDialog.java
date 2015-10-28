package net.fishandwhistle.ctexplorer;

import net.fishandwhistle.ctexplorer.backend.CustomAlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;

public class TransparencyDialog extends CustomAlertDialog {
	
	private SeekBar seek ;
	private CheckBox check ;
	private OnCheckedChangeListener listener ;
	
	public TransparencyDialog(Context context) {
		super(context, R.layout.dialog_transparency);
		seek = (SeekBar)findViewById(R.id.transparnecy_seek) ;
		check = (CheckBox)findViewById(R.id.transparency_check) ;
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				seek.setEnabled(isChecked);
				if(listener != null)
					listener.onCheckedChanged(buttonView, isChecked);
			}
			
		});
	}

	@Override
	protected Builder getBuilder(Context context) {
		Builder b = new Builder(context) ;
		b.setTitle(R.string.transparency_title) ;
		b.setNegativeButton(R.string.transparency_done, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
			
		}) ;
		return b ;
	}

	public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener l) {
		seek.setOnSeekBarChangeListener(l);
	}
	
	public void setMax(int max) {
		seek.setMax(max);
	}
	
	public void setProgress(int progress) {
		seek.setProgress(progress);
	}
	
	public void setChecked(boolean state) {
		check.setChecked(state);
	}
	
	public void setOnCheckedChangeListener(OnCheckedChangeListener listen) {
		listener = listen ;
	}
	
}
