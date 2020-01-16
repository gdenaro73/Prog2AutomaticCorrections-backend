package com.prog2.automaticCorrections.utilis;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.prog2.automaticCorrections.models.FeedbackResponse;

import checkers.helpers.Checker;
import checkers.helpers.ISourceParsingChecker;
import main.CheckSchedule;


public class RunUtil {
	
	private static final Logger LOG = Logger.getLogger(RunUtil.class);
	
	private String LibDirPath;
	@Autowired
    public RunUtil(@Value("${run.dir.lib}") String LibDirPath) {
        this.LibDirPath = LibDirPath;
    }
	/** 
	 * Run checker and retrun a feedback
	 * 
	 * @param folderStudent
	 * @param compileOutputDir
	 * @return FeedbackResponse : feedback message from checker
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	
  
	public FeedbackResponse runCorrection(String folderStudent, File compileOutputDir)
				throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		
		FeedbackResponse feedbackResponse = new FeedbackResponse();	
		
        URL url_compileOutputDir = compileOutputDir.toURI().toURL();
        URL[] urls_lib_array = getJarFiles(LibDirPath);
        
        // It's important to set the context loader as parent, otherwise the test runs will fail
        ClassLoader classLoader = new URLClassLoader(urls_lib_array,Thread.currentThread().getContextClassLoader());
        Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(classLoader, url_compileOutputDir);
        
        
       
        for(URL url3: ((URLClassLoader)classLoader).getURLs()){
			LOG.debug("Classloader del correttore: " +url3.getFile());
		}  
		
		Class<?> scheduleClass = classLoader.loadClass("CheckingScheduleImpl");
		CheckSchedule schedule = (CheckSchedule) scheduleClass.newInstance();
		Object[] checkers = schedule.getCheckers();
		
		for (Object c: checkers) {
			LOG.debug("Checkers: "+c.getClass().getName());
			if (c instanceof ISourceParsingChecker) {
				((ISourceParsingChecker) c).setSrcFolderPath(folderStudent);
			}
			
			else if(c instanceof Checker) {
				((Checker) c).setLogOnConsole(true);
				if(((Checker) c).isSomeErrorPresent()) {
					//if found an error, return checker feedback and stop execution
					LOG.error(((Checker) c).getFeedbackOnErrors());
					feedbackResponse.setCheckerResponse(((Checker) c).getFeedbackOnErrors());
					return feedbackResponse;
					//break;
				}
					
				LOG.debug("Risultato checking: "+((Checker) c).runCheck());
			}
			else {
				JUnitCore junit = new JUnitCore();
				TestPassCounter passCounter = new TestPassCounter();
				junit.addListener(passCounter/*TextListener(System.out)*/);
				junit.run(c.getClass());
				LOG.debug("Risultati test: "+ passCounter.counter);		
			}		
		}
		//code is okay
		feedbackResponse.setCheckerResponse("Your code is okay!");
		return feedbackResponse;
	}
		
	/**	
	 * 
	 * @param jarDir: jar files' directory 
	 * @return URL[] with urls of jar files
	 */
	
		private URL[] getJarFiles (String jarDir) {
			
			 // search for JAR files in the given directory
		    FileFilter jarFilter = new FileFilter() {
		        public boolean accept(File pathname) {
		            return pathname.getName().endsWith(".jar");
		        }
		    };
			
			 // create URL for each JAR file found
		    File[] jarFiles = new File(jarDir).listFiles(jarFilter);
		    URL[] urls;
		 
		    if (null != jarFiles) {
		        urls = new URL[jarFiles.length];
		 
		        for (int i = 0; i < jarFiles.length; i++) {
		            try {
		                urls[i] = jarFiles[i].toURI().toURL();
		                //LOG.debug("Aggiungo jar: "+urls[i].getFile());
		            } catch (MalformedURLException e) {
		                throw new RuntimeException("Could not get URL for JAR file: " + jarFiles[i], e);
		            }
		        }
		 
		    } else {
		        // no JAR files found
		        urls = new URL[0];
		    }	    
		    return urls;
		}
		
		
		private static class TestPassCounter extends RunListener {
			int counter = 0;
			@Override
			public void testFinished(Description description) throws Exception {
				super.testFinished(description);
				counter++;
				//System.out.println(counter + " done: " + description );
			}
			@Override
			public void testFailure(Failure failure) throws Exception {
				super.testFailure(failure);
				counter--;
				//System.out.println(counter + " failed: " + failure );
			}
		}
				

	 

}
