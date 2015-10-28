package net.fishandwhistle.ctexplorer.backend;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import org.beyka.tiffbitmapfactory.TiffBitmapFactory;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.util.Log;
import ca.fwe.nts.NTSMapSheet;

public class ToporamaLoader {

	private static final String TAG = "ToporamaLoader" ;

	public enum ErrorClass {NO_ERROR, INFORMATION_ONLY, CANCELLED_ERROR, NETWORK_ERROR, NO_FILE_ON_SERVER,
		IO_ERROR, ZIP_ERROR, TIFF_PROCESSING_ERROR, NO_TIFF_FILE}

	public enum ProgressType {INITIALIZE, DOWNLOAD_LOWRES, DOWNLOAD_HIRES, PROCESSING, COMPLETED}

	private static final int DOWNLOAD_NO_FILE = -1 ;
	private static final int DOWNLOAD_NO_NETWORK = -2 ;
	private static final int DOWNLOAD_IO_ERROR = -3 ;
	private static final int DOWNLOAD_CANCELLED = -4 ;

	private boolean download ;
	private NTSMapSheet sheet ;
	private AsyncTask<Void, Integer, ErrorClass> task ;
	private MapCacheManager cache ;
	private int totalContentLength ;
	private boolean cancelled ;
	private ProgressType currentTask ;
	private int currentProgress ;

	public interface OnToporamaLoadListener {
		public void onToporamaLoad(NTSMapSheet sheet, ErrorClass error, int contentLength) ;
		public void onToporamaProgress(NTSMapSheet sheet, ProgressType type, int value) ;
	}

	public interface ProgressCallback {
		public void onProgress(int prog) ;
	}

	public ToporamaLoader(Context context, NTSMapSheet sheet, boolean download) {
		if(sheet == null)
			throw new IllegalArgumentException("Need non-null mapsheet to initiate loader") ;
		if(sheet.getScale() != NTSMapSheet.SCALE_50K)
			throw new IllegalArgumentException("Can't load mapsheet with scale other than 50K") ;
		this.sheet = sheet ;
		this.download = download ;
		cache = new MapCacheManager(context) ;
		totalContentLength = 0 ;
		currentProgress = -1000 ;
	}

	public void cancel() {
		cancelled = true ;
	}

	public void loadAsync(final OnToporamaLoadListener l) {
		task = new AsyncTask<Void, Integer, ErrorClass>() {

			@Override
			protected ErrorClass doInBackground(Void... params) {

				return load(new ProgressCallback() {

					@Override
					public void onProgress(int prog) {
						publishProgress(prog) ;
					}

				}) ;
			}

			@Override
			protected void onPostExecute(ErrorClass result) {
				cache.cleanup();
				if(l != null) {
					l.onToporamaLoad(sheet, result, totalContentLength);
				}
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				if(l != null) {
					l.onToporamaProgress(sheet, currentTask, values[0]);
				}
			}



		} ;
		task.execute() ;
	}

	public ProgressType getCurrentTask() {
		return currentTask ;
	}

	public ErrorClass load(final ProgressCallback pc) {
		currentTask = ProgressType.INITIALIZE ;
		if(pc != null) {
			pc.onProgress(-1);
		}

		currentTask = ProgressType.DOWNLOAD_LOWRES ;
		if(pc != null)
			pc.onProgress(0) ;

		File sheetJpeg = cache.getMapFile(sheet) ;
		String url = getPreviewUrl() ;
		int length = download(url, sheetJpeg, new ProgressCallback() {
			public void onProgress(int prog) {
				if(pc != null && prog != currentProgress)
					pc.onProgress(prog) ;
				currentProgress = prog ;
			}
		}) ;
		switch(length) {
		case DOWNLOAD_NO_FILE:
			return ErrorClass.NO_FILE_ON_SERVER ;
		case DOWNLOAD_NO_NETWORK:
			return ErrorClass.NETWORK_ERROR ;
		case DOWNLOAD_IO_ERROR:
			sheetJpeg.delete() ;
			return ErrorClass.IO_ERROR ;
		case DOWNLOAD_CANCELLED:
			sheetJpeg.delete() ;
			return ErrorClass.CANCELLED_ERROR ;
		default:
			totalContentLength += length ;
		}

		currentTask = ProgressType.DOWNLOAD_HIRES ;
		if(pc != null)
			pc.onProgress(-2) ;
		currentProgress = -2 ;

		url = getHiResUrl() ;
		File zipFile = cache.getTempFile() ;
		length = download(url, zipFile, new ProgressCallback() {
			public void onProgress(int prog) {
				if(pc != null && prog != currentProgress)
					pc.onProgress(prog) ;
				currentProgress = prog ;
			}
		}) ;

		switch(length) {
		case DOWNLOAD_NO_FILE:
			return ErrorClass.NO_FILE_ON_SERVER ;
		case DOWNLOAD_NO_NETWORK:
			return ErrorClass.NETWORK_ERROR ;
		case DOWNLOAD_IO_ERROR:
			cache.cleanup();
			return ErrorClass.IO_ERROR ;
		case DOWNLOAD_CANCELLED:
			cache.cleanup();
			return ErrorClass.CANCELLED_ERROR ;
		default:
			totalContentLength += length ;
		}

		currentTask = ProgressType.PROCESSING ;
		if(pc != null)
			pc.onProgress(-1) ;
		currentProgress = -1 ;

		if(download) {
			ErrorClass result = extractTiff(zipFile) ;
			currentTask = ProgressType.COMPLETED ;
			if(pc != null)
				pc.onProgress(-1000);
			cache.cleanup();
			return result ;
		} else {
			currentTask = ProgressType.COMPLETED ;
			if(pc != null)
				pc.onProgress(-1000);
			cache.cleanup();
			return ErrorClass.INFORMATION_ONLY ;
		}
	}

	private int download(String urlString, File destination, ProgressCallback callb) {
		URL url = null ;
		int length = 0 ;
		try {
			url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.connect();
			if(connection.getResponseCode() == 200) {
				length = connection.getContentLength() ;
			} else {
				return DOWNLOAD_NO_FILE ;
			}

		} catch(IOException e) {
			return DOWNLOAD_NO_NETWORK ;
		}
		if(download) {
			try {
				// download the file
				InputStream input = new BufferedInputStream(url.openStream(), 8192);
				OutputStream output = new FileOutputStream(destination);

				byte data[] = new byte[1024];
				int count;
				int totalCount = 0 ;
				while (((count = input.read(data)) != -1) && !cancelled) {
					output.write(data, 0, count);
					totalCount += count ;
					callb.onProgress(100*totalCount / length);
				}

				output.flush();
				output.close();
				input.close();

			} catch(IOException e) {
				return DOWNLOAD_IO_ERROR ;
			}
		}
		if(cancelled) {
			destination.delete() ;
			return DOWNLOAD_CANCELLED ;
		} else {
			return length ;
		}
	}

	private ErrorClass extractTiff(File zipFile) {

		File tmpFolder = cache.getTempFile() ;
		tmpFolder.mkdir() ;
		if(FileUnZipper.unzipFiles(zipFile, tmpFolder)) {
			File tiffFile = findTiffFile(tmpFolder) ;
			if(tiffFile != null) {
                //FIXME this fails and causes halt
				Bitmap img = TiffBitmapFactory.decodeFile(tiffFile);

				int tiffHeight = img.getHeight();
				int tiffWidth = img.getWidth();
				Log.i(TAG, "opened tiff, height = " + tiffHeight + " width = " + tiffWidth) ;

				int blockHeight = tiffHeight / 3 ;
				try {
					for(int i=0; i<3; i++) {
						Bitmap b = Bitmap.createBitmap(img, i*blockHeight*tiffWidth, tiffWidth, tiffWidth, blockHeight) ;
						splitBitmapInto4(b, NTSMapSheet.MAP_BLOCK[2-i]) ;

						b.recycle();
						b = null ;
					}
				} catch(IOException e) {
					img.recycle();
					img = null ;
					return ErrorClass.TIFF_PROCESSING_ERROR ;
				}
                img.recycle();
                img = null ;
				return ErrorClass.NO_ERROR ;
			} else {
				//can't find tiff file from extracted files
				return ErrorClass.NO_TIFF_FILE ;
			}
		} else {
			//zip error
			return ErrorClass.ZIP_ERROR ;
		}
	}

	private File findTiffFile(File tmpFolder) {
		for(File f: tmpFolder.listFiles()) {
			if(f.isDirectory()) {
				File result = findTiffFile(f) ;
				if(result != null)
					return result ;
			} else {
				String name = f.getName() ;
				int lastDot = name.lastIndexOf(".") ;
				if(lastDot != -1) {
					String ext = name.substring(lastDot) ;
					if(".tif".equals(ext) || ".tiff".equals(ext))
						return f ;
				}
			}
		}
		return null ;
	}

	private void splitBitmapInto4(Bitmap b, String[] blockIds) throws IOException {
		int newWidth = b.getWidth() / 4 ;
		for(int i=0; i<4; i++) {
			Bitmap b1 = Bitmap.createBitmap(b, newWidth*i, 0, newWidth, b.getHeight()) ;
			String newId = sheet.getNtsId() + "-" + blockIds[i] ;
			NTSMapSheet newSheet = NTSMapSheet.getSheetById(newId) ;
			File destination = cache.getMapFile(newSheet) ;
			FileOutputStream fos = new FileOutputStream(destination) ;
			boolean happened = b1.compress(CompressFormat.JPEG, 70, fos) ;
			fos.close();
			b1.recycle();
			b1 = null ;
			if(!happened)
				throw new IOException("Could not compress bitmap") ;
		}
	}

	private String getPreviewUrl() {
		String format = "http://ftp2.cits.nrcan.gc.ca/pub/toporama/50k/images/toporama_%s%s%s.jpg" ;
		String[] n = sheet.getNtsId().split("-") ;
		return String.format(format, n[0], n[1].toLowerCase(Locale.US), n[2]) ;
	}

	private String getHiResUrl() {
		String format = "http://ftp2.cits.nrcan.gc.ca/pub/toporama/50k_geo_tif/%s/%s/toporama_%s%s%s_geo.zip" ;
		String[] n = sheet.getNtsId().split("-") ;
		return String.format(format, n[0], n[1].toLowerCase(Locale.US), n[0], n[1].toLowerCase(Locale.US), n[2]) ;
	}

}
