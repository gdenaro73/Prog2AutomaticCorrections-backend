package p2ac.moodle.backend.utilis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;

/**
 * Unzip file
 * @author benedettoraviotta
 * @method unzip : unzipped file 
 * @par sourceFile: file .zip
 * @par destinationPath: destinationPath for unzipped files, used by moveExtractedFiles method 
 */
public class myUnzip {

	private static final Logger LOG = Logger.getLogger(myUnzip.class);
	private static final int BUFFER = 2048;
	private static File dirUnzippedFiles = null;
	private static boolean asSubfolder = false;
	
	
    public static void unzip(File sourceFile, String destinationPath) throws IOException{
    	
    	String destination = sourceFile.getParentFile().getAbsolutePath();
    	
        try {
            File root = new File(destination);
            if(!root.exists()){
            	LOG.debug("Create, if not extist, folder: "+destination);
                root.mkdir();
            }
            //BufferedOutputStream bos = null;
            // zipped input
            FileInputStream fis = new FileInputStream(sourceFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();
                //skip subfolder create by macos zip
                if(fileName.contains("_MAC"))
                	continue;
                
                File file = new File(destination + File.separator + fileName);
                LOG.debug("unzipping: "+ file.getAbsolutePath());
                
               
                if (!entry.isDirectory()) {
                	//fileName.contains(".java")
                	if(FilenameUtils.getExtension(fileName).equals("java")) {
                		
                		if(!new File(file.getParent()).exists()) {
                			
                			asSubfolder = true;
                			dirUnzippedFiles = new File(file.getParentFile().getAbsolutePath());
                			LOG.debug("create subfolder dir: "+ file.getParent());
                			new File(file.getParent()).mkdirs();	
                		}
                		
                		extractFileContentFromArchive(file, zis);
                		
                	}
       		
                	else
                		{continue;}
                }
                else{
                	
                	asSubfolder = true;
                	LOG.debug("directory: " +file.getAbsolutePath());
                	//path unzipped files to delete beceasue moved
                	dirUnzippedFiles = file;
                    if(!file.exists()){
                        file.mkdirs();
                    }
                }
                zis.closeEntry();
               
            }
            zis.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        if (sourceFile.exists()) {
        	
        	LOG.debug("delete zip file...");
            sourceFile.delete();
        }
        if(asSubfolder) {
        	
        	LOG.debug("move files...");
         	org.apache.commons.io.FileUtils.copyDirectory(dirUnzippedFiles, new File(destinationPath));
     		LOG.debug("delete old folder: "+dirUnzippedFiles);
     		FileUtils.deleteDirectory(dirUnzippedFiles);  	
        }
       
 
    }
    
    private static void extractFileContentFromArchive(File file, ZipInputStream zis) throws IOException{
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);
        int len = 0;
        byte data[] = new byte[BUFFER];
        while ((len = zis.read(data, 0, BUFFER)) != -1) {
            bos.write(data, 0, len);
        }
        bos.flush();
        bos.close();
    }
    
	
}