package wikilink;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Pass-through stream for counting bytes read.
 */
public class CountingInputStream extends FilterInputStream {
	
	public CountingInputStream(InputStream in) {
		super(in);
	}
	
	public volatile long bytesRead = 0;
	
	public int read() throws IOException {
		bytesRead++;
		return super.in.read();
	}
	
	public int read(byte b[], int off, int len) throws IOException {
		int i = in.read(b, off, len);
		if (i > 0) bytesRead += i;
		return i;
	}
	
}