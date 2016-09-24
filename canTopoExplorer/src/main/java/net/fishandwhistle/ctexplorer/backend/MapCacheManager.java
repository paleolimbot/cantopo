package net.fishandwhistle.ctexplorer.backend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import fwe.nts.NTSMapSheet;

public class MapCacheManager {

	private static final String TAG = "MapCacheManager" ;

	private Context context ;
	private List<File> tempFiles ;

	private static Random rand = new Random() ;
	private static final String chars = "abcdefghijklmnopqrstuvwxyz" ;
	private List<String> cachedFileList ;
	private boolean cachingEnabled ;

	public MapCacheManager(Context context) {
		this.context = context ;
		this.getMapCacheFolder().mkdir() ;
		File nomediaFile = new File(this.getMapCacheFolder(), ".nomedia") ;
		if(!nomediaFile.exists()) {
			try {
				nomediaFile.createNewFile() ;
			} catch(IOException e) {
				//oh well. we tried.
			}
		}
		this.getPublicTempDir().mkdir() ;
		tempFiles = new ArrayList<File>() ;
		cachingEnabled = false ;
		cachedFileList = new ArrayList<String>() ;
		File[] files = context.getCacheDir().listFiles() ;
		if(files != null) {
			for(File file: files) {
				Log.i(TAG, "opening cache manager, uncleaned file: " + file) ;
			}
		}
	}

	public void setCachingEnabled(boolean flag) {
		cachingEnabled = flag ;
		if(flag)
			rebuildCache() ;
		else
			cachedFileList.clear();
	}

	public synchronized void rebuildCache() {
		if(cachingEnabled) {
			if(cachedFileList == null)
				cachedFileList = new ArrayList<String>() ;
			else
				cachedFileList.clear();
			for(File f: getMapCacheFolder().listFiles()) {
				cachedFileList.add(f.getName()) ;
			}
		}
	}

	public File getMapFile(NTSMapSheet sheet) {
		return new File(this.getMapCacheFolder(), sheet.getNtsId() + ".jpg") ;
	}

	public File getMapCacheFolder() {
		File fld = new File(Environment.getExternalStorageDirectory(), "CanadaTopo_maps") ;
        if(!fld.isDirectory()) {
            fld.delete();
        }
		fld.mkdir();
        return fld;
	}

	public File getPublicTempDir() {
		return new File(this.getMapCacheFolder(), "tmp") ;
	}

	public File getTempFile() {
		return getTempFile(context.getCacheDir(), ".tmp") ;
	}

	public File getTempFile(File folder, String ext) {
		File f ;
		do {
			f = new File(folder, randomString() + ext) ;
		} while(f.exists()) ;

		tempFiles.add(f) ;
		Log.i(TAG, "issuing temp file " + f) ;
		return f ;
	}

	public void cleanup() {
		for(File f: tempFiles) {
			delete(f) ;
		}
		tempFiles.clear();
	}

	public boolean hasAllFiles(NTSMapSheet s) {
		int children = s.possibleSubSheets() ;
		if(children > 0) {
			for(NTSMapSheet child: NTSMapSheet.getChildSheets(s)) {
				if(!hasAllFiles(child))
					return false ;
			}
		}
		if(s.getScale() == NTSMapSheet.SCALE_BLOCK || s.getScale() == NTSMapSheet.SCALE_50K)
			return getMapFile(s).exists() ;
		else
			return true ;
	}

	public boolean hasAnyFiles(NTSMapSheet s) {
		if(cachingEnabled)
			return hasAnyFilesCached(s) ;

		int children = s.possibleSubSheets() ;
		if(getMapFile(s).exists()) {
			return true ;
		} else { 
			if(children > 0) {
				for(NTSMapSheet child: NTSMapSheet.getChildSheets(s)) {
					if(hasAnyFiles(child))
						return true ;
				}
			}
		}
		return false ;
	}

	private synchronized boolean hasAnyFilesCached(NTSMapSheet sheet) {
		for(String s: cachedFileList) {
			if(s.contains(sheet.getNtsId()))
				return true ;
		}
		return false ;
	}

	public long[] removeAllFiles(NTSMapSheet s) {
		int removed = 0 ;
		int totalSize = 0 ;
		int children = s.possibleSubSheets() ;
		if(children > 0) {
			for(NTSMapSheet child: NTSMapSheet.getChildSheets(s)) {
				long[] result = removeAllFiles(child) ;
				removed += result[0] ;
				totalSize += result[1] ;
			}
		}
		File f = getMapFile(s) ;
		long deletedSize = 0 ;
		if(f.exists())
			deletedSize = f.length() ;
		if(getMapFile(s).delete()) {
			removed++ ;
			totalSize += deletedSize ;
		}
		return new long[] {removed, totalSize} ;
	}

	public long[] countFiles(NTSMapSheet s) {
		int files = 0 ;
		int totalSize = 0 ;
		int children = s.possibleSubSheets() ;
		if(children > 0) {
			for(NTSMapSheet child: NTSMapSheet.getChildSheets(s)) {
				long[] result = countFiles(child) ;
				files += result[0] ;
				totalSize += result[1] ;
			}
		}
		File f = getMapFile(s) ;
		if(f.exists()) {
			totalSize += f.length() ;
			files++ ;

		}
		return new long[] {files, totalSize} ;
	}

	public long[] cacheSize() {
		int size = 0 ;
		int count = 0 ;
		for(File f: getMapCacheFolder().listFiles()) {
			size += f.length() ;
			count++ ;
		}
		return new long[] {count, size} ;
	}

	public int clearTemporaryFiles() {
		this.cleanup();
		File tmpFolder = this.getPublicTempDir() ;
		int out = delete(tmpFolder) ;
		tmpFolder.mkdir() ;
		return out ;
	}

	public int clearCache() {
		int deleted = delete(this.getMapCacheFolder()) ;
		this.getMapCacheFolder().mkdir() ;
		File nomediaFile = new File(this.getMapCacheFolder(), ".nomedia") ;
		try {
			nomediaFile.createNewFile() ;
		} catch(IOException e) {
			//oh well. we tried.
		}
		return deleted ;
	}

	private static int delete(File f) {
		int deleted = 0 ;
		boolean dir = false ;
		if(f.isDirectory()) {
			dir = true ;
			for(File g: f.listFiles()) {
				deleted += delete(g) ;
			}
			Log.i(TAG, "cleaned directory file " + f) ;
		}

		if(f.delete()) {
			if(!f.isDirectory())
				if(!dir)
					deleted++ ;
			Log.i(TAG, "deleted file " + f) ;
		} else {
			Log.e(TAG, "failed to delete file! " + f) ;
		}
		return deleted ;
	}

	private static String randomString() {
		char[] out = new char[5] ;
		for(int i=0; i<out.length; i++) {
			out[i] = chars.charAt(rint(chars.length())) ;
		}
		return new String(out) ;
	}

	private static int rint(int max) {
		return rand.nextInt(max) ;
	}
}
