package com.prog2.automaticCorrections.utilis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


public class myCompiler {
	 
	private static final Logger LOG = Logger.getLogger(myCompiler.class);
	
	public File compileOutputDir;
	@SuppressWarnings("rawtypes")
	private List<Diagnostic> compilationErrors = new ArrayList<>();	
	private String LibDirPath;
	private String compilerOutputPrexif;
	 
	
	@Autowired
    public myCompiler(@Value("${compile.dir.lib}") String LibDirPath, 
    				  @Value("${upload.dir.compiled}") String compilerOutputPrexif) {
        this.LibDirPath = LibDirPath;
        this.compilerOutputPrexif = compilerOutputPrexif;
    }
	
	/**
	 * Compile student and teacher files 
	 * @param assignmentFolderPath: assignment path, eg: /temp/assignment_01
	 * @param correctionFolderPath:	correction path prefix -> correctionFiles
	 * @param studentFolderPath:	student path prefix -> consegna_		
	 * @param studentID:			unique studentID eg:20
	 */
	
	
	public void compile(String assignmentFolderPath,	
			   			String correctionFolderPath,
			   			String studentFolderPath,
			   			String studentID) {
		
		String teacherPath = assignmentFolderPath + File.separator + correctionFolderPath;
		String studentPath = assignmentFolderPath + File.separator + studentFolderPath + studentID;

		List <File> teacherFiles = getFiles(teacherPath);
		List <File> studentFiles = getFiles(studentPath);
		LOG.debug("Student path in compiler: "+studentFolderPath);
		List <File> filesToCompile = new ArrayList<>();
				
		filesToCompile.addAll(teacherFiles);
		filesToCompile.addAll(studentFiles);
		
		compileOutputDir = new File(assignmentFolderPath + File.separator + compilerOutputPrexif + studentID);
				
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);        

        File[] fileArray = new File[filesToCompile.size()];
        fileArray = filesToCompile.toArray(fileArray);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(fileArray);
        
        
        //add to classpath libraries Junit and Prog2AutomaticCorrections
        List<String> optionList = new ArrayList<>();
        optionList.add("-d");
        optionList.add(compileOutputDir.getAbsolutePath());
        optionList.add("-classpath");
        String cp = buildClassPath(LibDirPath, compileOutputDir.getAbsolutePath());
        LOG.debug("La classpath per compilazione è: " + cp);
        optionList.add(cp);
        
        if(!compileOutputDir.exists())
        	compileOutputDir.mkdir();
        

        //compilo i file 
        JavaCompiler.CompilationTask task = compiler.getTask(null,
        													 fileManager, 
        													 null, 
        													 optionList, 
        													 null, 
        													 compilationUnits);
        
        Boolean compileResult = task.call();
        //If the compilation failed, remove the failed file from the pathsToCompile list and try to compile again without this file
        if (!compileResult) {
            File currentFile = new File(((JavaFileObject) compilationErrors.get(compilationErrors.size() - 1).getSource()).toUri().getPath());
            LOG.warn("Compilation of file '" + currentFile.getPath() + "' failed");
            filesToCompile.removeIf(file -> file.getPath().equalsIgnoreCase(currentFile.getPath()));
            if (filesToCompile.size() > 0) {
                compile(assignmentFolderPath, correctionFolderPath, studentFolderPath, studentID);
            }
        }
         
     }

		
		/**
	     * This function builds a classpath from the passed Strings.
	     * We need this because the JUnit4 and Hamcrest libraries needs to be added.
	     *
	     * @param paths classpath elements
	     * @return returns the complete classpath with wildcards expanded
	     */
	    private static String buildClassPath(String... paths) {
	        StringBuilder sb = new StringBuilder();
	        for (String path : paths) {
	            if (path.endsWith("*")) {
	                path = path.substring(0, path.length() - 1);
	                File pathFile = new File(path);
	                // TODO pathFile can be null if no lib folder is given
	                for (File file : pathFile.listFiles()) {
	                    if (file.isFile() && file.getName().endsWith(".jar")) {
	                        sb.append(path);
	                        sb.append(file.getName());
	                        sb.append(System.getProperty("path.separator"));
	                    }
	                }
	            } else {
	                sb.append(path);
	                sb.append(System.getProperty("path.separator"));
	            }
	        }
	       
	        return sb.toString();
	    }
	    
	    
	    
	    private List<File> getFiles(String FolderPath) {
	       
	        LOG.debug("Path da cui recuperare i file: "+FolderPath);
	        File Dir = new File(FolderPath);
	        File[] FilesArray = Dir.listFiles();
	        List<File> Files = new ArrayList<>();
	        Collections.addAll(Files, FilesArray);
	        return Files;
	    }
	    
	    
	    public File getCompileOutputDir() {
	        return compileOutputDir;
	    }

}
