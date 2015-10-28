package net.fishandwhistle.ctexplorer.backend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

public abstract class CustomAlertDialog {

	private Context context ;
	private AlertDialog dialog ;
	private View rootView ;
	private int layoutId ;
	
	public CustomAlertDialog(Context context, int layoutId) {
		this.context = context ;
		this.layoutId = layoutId ;
		this.buildDialog();
	}
	
	public Context getContext() {
		return context ;
	}
	
	protected String getString(int resId) {
		return context.getString(resId) ;
	}
	
	public AlertDialog getDialog() {
		return dialog ;
	}
	
	public View findViewById(int id) {
		return rootView.findViewById(id) ;
	}
	
	public View getRootView() {
		return rootView ;
	}
	
	public void cancel() {
		dialog.cancel();
	}
	
	public void show() {
		dialog.show();
	}
	
	public void dismiss() {
		dialog.dismiss();
	}
	
	public void hide() {
		dialog.hide();
	}
	
	public void setOnCancelListener(DialogInterface.OnCancelListener l) {
		dialog.setOnCancelListener(l);
	}
	
	public void setOnDismissListener(DialogInterface.OnDismissListener l) {
		dialog.setOnDismissListener(l);
	}
	
	public void setTitle(String title) {
		dialog.setTitle(title);
	}
	
	private void buildDialog() {
		AlertDialog.Builder builder = this.getBuilder(context) ;
		if(builder == null)
			builder = new AlertDialog.Builder(context) ;
		rootView = LayoutInflater.from(context).inflate(layoutId, null) ;
		builder.setView(rootView) ;
		dialog = builder.create() ;
	}

	protected abstract AlertDialog.Builder getBuilder(Context context) ;
}
