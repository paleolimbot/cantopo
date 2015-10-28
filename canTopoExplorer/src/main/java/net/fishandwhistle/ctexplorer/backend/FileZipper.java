package net.fishandwhistle.ctexplorer.backend;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.util.Log;

public class FileZipper {
	private ArrayList<File> files ;
	private ArrayList<String> relativePaths ;
	private String baseDir ;

	public FileZipper(ArrayList<File> files, ArrayList<String> relativePaths) {
		this.files = files ;
		this.relativePaths = relativePaths ;
	}
	
	public FileZipper(File directory) {
		files = new ArrayList<File>() ;
		relativePaths = new ArrayList<String>() ;
		if(directory.isDirectory()) {
			baseDir = directory.getPath() + "/" ;
			this.addItems(directory) ;
		} else {
			throw new IllegalArgumentException("File was not a directory") ;
		}
	}

	private void addItems(File folder) {
		File[] list = folder.listFiles();
		for(int j = 0; j < list.length; j++) {
			File f = list[j] ;
			files.add(f);
			String relativePath = f.getPath().replace(baseDir, "") ;
			relativePaths.add(relativePath) ;
			if(f.isDirectory())
				addItems(f);
		}
	}

	public void writeZip(OutputStream o) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(o) ;
		for(int i=0; i<files.size(); i++) {
			File f = files.get(i) ;
			String path = relativePaths.get(i) ;
			Log.i("dd", "zipping entry " + f.getPath()) ;
			Log.i("dd", "Writing to entry " + path) ;
			if(!f.isDirectory()) {
				zip.putNextEntry(new ZipEntry(path)) ;
				this.writeFile(f, zip) ;
			} else {
				String dirName = path + "/" ;
				zip.putNextEntry(new ZipEntry(dirName)) ;
			}
			zip.flush() ;
			zip.closeEntry() ;
		}
		zip.flush() ;
		zip.finish() ;
	}

	private void writeFile(File f, ZipOutputStream z) throws IOException {
		BufferedInputStream b = new BufferedInputStream(new FileInputStream(f)) ;
		byte[] data = new byte[1000];
		int count;
		while ((count = b.read(data, 0, 1000)) != -1) {
			z.write(data, 0, count);
		}
		b.close();
	}

}
