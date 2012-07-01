package com.blicksky.combo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import com.blicksky.combo.cache.ComboCache;

public class ComboWriter {

	private final Collection<File> resources;
	private final ComboCache cache;
	
	public ComboWriter( final Collection<File> resources ) {
		this( resources, null );
	}
	
	public ComboWriter( final Collection<File> resources, final ComboCache cache ) {
		this.resources = resources;
		this.cache = cache;
	}
	
	public void write( final OutputStream outputStream ) throws IOException {
		for( final File resource : this.resources ) {
			FileInputStream reader = new FileInputStream( resource );
			byte[] buffer = new byte[2048];
			
			int bytesRead = -1;
			while( ( bytesRead = reader.read(buffer) ) != -1 ) {
				outputStream.write( buffer, 0, bytesRead );
			}
		}
	}
}
