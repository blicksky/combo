package com.blicksky.combo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class ComboServlet extends HttpServlet {
	
	/**
	 * Auto-generated serial version UID
	 */
	private static final long serialVersionUID = -544151636153874487L;
	
	private static final Map<String,String> extensionContentTypeMap = new HashMap<String,String>();
	static {
		extensionContentTypeMap.put( "js",	"application/javascript; charset=utf-8" );
		extensionContentTypeMap.put( "css",	"text/css; charset=utf-8" );
	}

	/**
	 * Handles get requests
	 */
	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
		
		try {
			String separator = this.getInitParameter("separator");
			
			List<String> fileNames = new ArrayList<String>();
			String comboRequest = this.getComboRequest( request );
			if( comboRequest != null && separator != null ) {
				fileNames.addAll( Arrays.asList( comboRequest.split("\\Q"+separator+"\\E") ) );
			}

			ArrayList<File> files = getFiles( fileNames );
			
			response.setHeader("Age", "0");
			response.setHeader("Cache-Control", "max-age=315360000");
			response.setHeader("Expires", getExpirationDateString() );
			
			if( fileNames.size() != 0 ) {
				String contentType = extensionContentTypeMap.get( getFileExtension( files.get(0).getName() ) );
				if( contentType != null ) {
					response.setHeader("Content-Type", contentType );
				}
				
				ComboWriter comboWriter = new ComboWriter( files );
				comboWriter.write( response.getOutputStream() );
			}
		}
		catch( FileNotFoundException exception ) {
			response.sendError( HttpServletResponse.SC_NOT_FOUND );
		}
		catch( InvalidExtensionException exception ) {
			response.sendError( HttpServletResponse.SC_BAD_REQUEST, exception.getMessage() );
		}
		catch( IOException exception ) {
			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
		}
	}
	
	
	
	/**
	 * Determines the combo request from the path or the query string
	 * @param request
	 * @return
	 */
	private String getComboRequest( HttpServletRequest request ) {
		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		String requestURI = request.getRequestURI();
		String queryString = request.getQueryString();
		
		String path = requestURI.substring(contextPath.length() + servletPath.length());
		return ( path != null && path.length() > 0 ? path : queryString );
	}
	
	/**
	 * Gets a list of files from a list of file names
	 * @param fileNames
	 * @return
	 * @throws FileNotFoundException
	 * @throws InvalidExtensionException
	 */
	private ArrayList<File> getFiles( List<String> fileNames ) throws FileNotFoundException, InvalidExtensionException {
		ArrayList<File> files = new ArrayList<File>( fileNames.size() );
		String firstFileExtension = null;
		
		for( String paramName : fileNames ) {
			// TODO: get the base URL from a servlet parameter
			File file = new File( this.getServletContext().getRealPath( "yui/" + paramName) );
			if( file == null || !file.isFile() || !file.canRead() ) {
				throw new FileNotFoundException();
			}
			
			String fileExtension = getFileExtension( file.getName() );
			
			if( firstFileExtension == null ) {
				firstFileExtension = fileExtension;
			}
			else if( !extensionContentTypeMap.containsKey( fileExtension ) ) {
				throw new InvalidExtensionException("Files of type " + fileExtension + " are not supported");
			}
			else if( !fileExtension.equals( firstFileExtension ) ) {
				throw new InvalidExtensionException("Not all files are of type: " + firstFileExtension);
			}
			
			files.add( file );
		}
		
		return files;
	}
	
	private String getExpirationDateString() {
		Calendar expiresCal = Calendar.getInstance();
		expiresCal.add(Calendar.YEAR, 12);
		DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format.format(expiresCal.getTime());
	}
	
	/**
	 * Gets the extension from a file name
	 * @param fileName
	 * @return
	 */
	private String getFileExtension( String fileName ) {
		if( fileName != null ) {
			int lastDotIndex = fileName.lastIndexOf('.');
			if( lastDotIndex > 0 && lastDotIndex + 1 < fileName.length() ) {
				return fileName.substring( lastDotIndex + 1 );
			}
		}
		
		return "";
	}
}