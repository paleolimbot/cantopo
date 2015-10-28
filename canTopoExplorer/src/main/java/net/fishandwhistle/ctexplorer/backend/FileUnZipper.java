package net.fishandwhistle.ctexplorer.backend;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;

public class FileUnZipper 
{
	public static final int BUFFER_SIZE = 2048;


	public static boolean unzipFiles(File srcFile, File destDirectory)
	{
		try
		{
			//first make sure that all the arguments are valid and not null
			if(srcFile == null)
			{
				Log.i("dd", "null file passed to unzipper") ;
				return false;
			}
			if(destDirectory == null)
			{
				Log.i("dd", "dest directory null") ;
				return false;
			}
			if(!srcFile.exists())
			{   
				Log.i("dd", "source file does not exist") ;
				return false;
			}
			
			destDirectory.mkdirs() ;

			//now start with unzip process
			BufferedOutputStream dest = null;

			FileInputStream fis = new FileInputStream(srcFile);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

			ZipEntry entry = null;

			while((entry = zis.getNextEntry()) != null)
			{
				String outputFilename = destDirectory + File.separator + entry.getName();

				Log.i("UnZipper", "Extracting file: " + entry.getName());

				createDirIfNeeded(destDirectory, entry);

				int count;

				byte data[] = new byte[BUFFER_SIZE];

				//write the file to the disk
				FileOutputStream fos = new FileOutputStream(outputFilename);
				dest = new BufferedOutputStream(fos, BUFFER_SIZE);

				while((count = zis.read(data, 0, BUFFER_SIZE)) != -1)
				{
					dest.write(data, 0, count);
				}

				//close the output streams
				dest.flush();
				dest.close();
			}

			//we are done with all the files
			//close the zip file
			zis.close();

		}
		catch(Exception e)
		{
			Log.i("dd", "exception while unzipping file", e) ;
			return false;
		}

		return true;
	}
	
    private static void createDirIfNeeded(File destDirectory, ZipEntry entry)
    {
        String name = entry.getName();

        if(name.contains("/"))
        {
            int index = name.lastIndexOf("/");
            String dirSequence = name.substring(0, index);

            File newDirs = new File(destDirectory, dirSequence);

            //create the directory
            newDirs.mkdirs();
        }
    }
	
}