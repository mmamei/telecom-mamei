
package d4d.pls_format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


// zippa i file. conviene guardare un attion in modo da zipare direttamente senza passare per file intermedi

public class ZipFiles {

	
	
	
    public static void main(String[] args) {
    	
    	File folder = new File("E:\\data\\settimana\\CoordPLS");
    	File[] files = folder.listFiles();
    	String line;
    	for (int i = 0; i < files.length; i++) {
			
		String filename = files[i].getName();
    	String filename2 = filename.substring(filename.indexOf("2_")+2, filename.indexOf(".txt"));
        //String filename2= "PLS"+i+""+ts;
    	String INPUT_FILE = files[i].getAbsolutePath();
    	String OUTPUT_FILE = "C:\\Users\\Alket\\junocode\\D4D\\PLS\\"+filename2+".zip";
         zipFile(new File(INPUT_FILE), OUTPUT_FILE);
         if(i == 100) System.out.println("zippo "+ i);;
    	}

    }

 

    public static void zipFile(File inputFile, String zipFilePath) {

        try {

 

            // Wrap a FileOutputStream around a ZipOutputStream

            // to store the zip stream to a file. Note that this is

            // not absolutely necessary

            FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath);

            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

 

           // a ZipEntry represents a file entry in the zip archive

            // We name the ZipEntry after the original file's name

            ZipEntry zipEntry = new ZipEntry(inputFile.getName());

           zipOutputStream.putNextEntry(zipEntry);

 

            FileInputStream fileInputStream = new FileInputStream(inputFile);

            byte[] buf = new byte[1024];

            int bytesRead;

 

            // Read the input file by chucks of 1024 bytes

            // and write the read bytes to the zip stream

            while ((bytesRead = fileInputStream.read(buf)) > 0) {

                zipOutputStream.write(buf, 0, bytesRead);

            }

            // close ZipEntry to store the stream to the file

            zipOutputStream.closeEntry();

 

            zipOutputStream.close();
            fileOutputStream.close();

 

            System.out.println("Regular file :" + inputFile.getCanonicalPath()+" is zipped to archive :"+zipFilePath);

 

        } catch (IOException e) {

            e.printStackTrace();

        }

 

    }

}
