package wikilink.numerical;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import wikilink.CountingInputStream;

public class NumericalIndex {
	
	public ArrayList<WikiEntry> entries = new ArrayList<>(10000000);
	
	/**
	 * Simple load time test.
	 */
	public static void main(String[] args) throws Exception {
		@SuppressWarnings("unused")
		NumericalIndex normal = new NumericalIndex(new File("numericalIndex"));
		System.out.println("Normal Loaded");
	}
	
	public NumericalIndex() {
		
	}
	
	public NumericalIndex(File index) throws IOException {
		long length = index.length();
		CountingInputStream count = new CountingInputStream(new BufferedInputStream(new FileInputStream(index), 1024 * 1024));
		
		DataInputStream in = new DataInputStream(count);
		long start = System.currentTimeMillis();
		byte[] data = new byte[1000];
		while (count.bytesRead < length) {
			String title = in.readUTF();
			int pointers = in.readUnsignedShort();
			
			WikiEntry entry = new WikiEntry();
			
			entry.name = title;
			entry.values = new int[pointers];
			
			int bytes = pointers * 3;
			if (data.length < bytes) data = new byte[bytes * 2];
			in.read(data, 0, bytes);
			for (int i = 0; i < pointers; i++) {
				entry.values[i] = (((data[i * 3 + 0] & 0xFF) << 16) + ((data[i * 3 + 1] & 0xFF) << 8) + ((data[i * 3 + 2] & 0xFF) << 0));
			}
			
			entries.add(entry);
			
		}
		System.out.println(System.currentTimeMillis() - start + " ms load.");
		in.close();
		entries.trimToSize();
	}
	
	/**
	 * Write the lower 3 bytes of an integer.
	 */
	public static void writeTriByte(int n, OutputStream out) throws IOException {
		out.write((n >>> 16) & 0xFF);
		out.write((n >>> 8) & 0xFF);
		out.write((n >>> 0) & 0xFF);
	}
	
	/**
	 * Read 3 bytes as an unsigned integer.
	 */
	public static int readTriByte(InputStream in) throws IOException {
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch2 | ch3 | ch4) < 0) throw new EOFException();
		return ((ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}
	
	/**
	 * Get the id of a title.
	 */
	public int getID(String title) {
		int entry = binarySearch(0, entries.size(), NumDataExtractor.normalizeTitle(title));
		return entry;
	}
	
	/**
	 * Get the title from an id.
	 */
	public String getTitle(int id) {
		return entries.get(id).name;
	}
	
	/**
	 * Get all ids pointed to by an id. Do not modify this array.
	 */
	public int[] getLinks(int id) {
		return entries.get(id).values;
	}
	
	/**
	 * Internal binary search for finding entries.
	 */
	private int binarySearch(int fromIndex, int toIndex, String key) {
		int low = fromIndex;
		int high = toIndex - 1;
		
		while (low <= high) {
			int mid = (low + high) >>> 1;
			WikiEntry midEntry = entries.get(mid);
			String midVal = midEntry.name;
			int compare = midVal.compareTo(key);
			if (compare <= -1)
				low = mid + 1;
			else if (compare >= 1)
				high = mid - 1;
			else
				return mid;  // key found
		}
		return -1; // key not found.
	}
	
}

/**
 * Minimal data entry for a Wikipedia page.
 */
class WikiEntry {
	
	public int[] values;
	//	int id;
	public String name;
	
}