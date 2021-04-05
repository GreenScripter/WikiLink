package wikilink.numerical;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wikilink.CountingInputStream;

/**
 * Methods for creating a WikiLink database from a wikipedia dump.
 */
public class NumDataExtractor {
	
	public static void main(String[] args) throws Exception {
		//Steps required to complete the generation.
		extractLinksAndRedirects();
		resolveRedirects();
		removeBrokenLinks();
		createNumericalIndex();
	}
	
	/**
	 * Create WikiLink's numerical index based on cleaned link data. Any links missing destinations
	 * will cause an exception.
	 */
	@SuppressWarnings("resource")
	public static void createNumericalIndex() throws Exception {
		Map<String, Long> valid = new HashMap<>();
		{
			long length = new File("cleanLinkData").length();
			CountingInputStream count = new CountingInputStream(new BufferedInputStream(new FileInputStream(new File("cleanLinkData"))));
			DataInputStream in = new DataInputStream(count);
			int n = 0;
			while (count.bytesRead < length) {
				long start = count.bytesRead;
				String title = in.readUTF();
				
				valid.put(title, start);
				int c = in.readInt();
				for (int i = 0; i < c; i++) {
					in.readUTF();
				}
				n++;
				
				if (n % 50000 == 0) {
					System.out.println(count.bytesRead + " / " + length);
				}
			}
			in.close();
		}
		
		String[] values = new String[valid.size()];
		Map<String, Integer> keys = new HashMap<>();
		int si = 0;
		for (String s : valid.keySet()) {
			values[si] = s;
			si++;
		}
		Arrays.sort(values);
		
		for (int i = 0; i < values.length; i++) {
			keys.put(values[i], i);
		}
		
		RandomAccessFile in = new RandomAccessFile(new File("cleanLinkData"), "r");
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File("numericalIndex"))));
		for (int n = 0; n < values.length; n++) {
			in.seek(valid.get(values[n]));
			String title = in.readUTF();
			int c = in.readInt();
			Set<String> links = new HashSet<>();
			for (int i = 0; i < c; i++) {
				String link = in.readUTF();
				links.add(link);
			}
			out.writeUTF(title);
			out.writeShort(links.size());
			int[] intLinks = new int[links.size()];
			int i = 0;
			for (String s : links) {
				intLinks[i] = keys.get(s);
				if (intLinks[i] == -1) {
					throw new Exception(s + " not found.");
				}
				i++;
			}
			RadixSort.radixSort(intLinks);
			for (int s : intLinks) {
				NumericalIndex.writeTriByte(s, out);
			}
			
			if (n % 50000 == 0) {
				System.out.println(n + " / " + values.length);
			}
		}
		//		out2.flush();
		in.close();
		out.close();
	}
	
	/**
	 * Remove any links which have no destinations to prevent clutter.
	 */
	public static void removeBrokenLinks() throws Exception {
		Set<String> valid = new HashSet<>();
		{
			long length = new File("plainLinkData").length();
			CountingInputStream count = new CountingInputStream(new BufferedInputStream(new FileInputStream(new File("plainLinkData"))));
			DataInputStream in = new DataInputStream(count);
			int n = 0;
			while (count.bytesRead < length) {
				String title = in.readUTF();
				valid.add(title);
				int c = in.readInt();
				for (int i = 0; i < c; i++) {
					in.readUTF();
				}
				n++;
				
				if (n % 50000 == 0) {
					System.out.println(count.bytesRead + " / " + length);
				}
			}
			in.close();
		}
		long length = new File("plainLinkData").length();
		CountingInputStream count = new CountingInputStream(new BufferedInputStream(new FileInputStream(new File("plainLinkData"))));
		DataInputStream in = new DataInputStream(count);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File("cleanLinkData"))));
		int n = 0;
		while (count.bytesRead < length) {
			String title = in.readUTF();
			int c = in.readInt();
			Set<String> links = new HashSet<>();
			for (int i = 0; i < c; i++) {
				
				String link = in.readUTF();
				if (valid.contains(link)) {
					links.add(link);
				} else {
					//					System.out.println(link);
				}
			}
			out.writeUTF(title);
			out.writeInt(links.size());
			for (String s : links) {
				out.writeUTF(s);
			}
			n++;
			
			if (n % 50000 == 0) {
				System.out.println(count.bytesRead + " / " + length);
			}
		}
		in.close();
		out.close();
	}
	
	/**
	 * Replace all links to redirects with the page that is redirected to.
	 */
	public static void resolveRedirects() throws Exception {
		Map<String, String> redirects = new HashMap<>();
		{
			long length = new File("redirectData").length();
			CountingInputStream count = new CountingInputStream(new BufferedInputStream(new FileInputStream(new File("redirectData"))));
			DataInputStream in = new DataInputStream(count);
			int n = 0;
			while (count.bytesRead < length) {
				String title = in.readUTF();
				String replacement = in.readUTF();
				redirects.put(title, replacement);
				n++;
				
				if (n % 50000 == 0) {
					System.out.println(count.bytesRead + " / " + length);
				}
			}
			in.close();
			System.out.println("Checking for double redirects.");
			for (int i = 0; i < 4; i++) {
				System.out.println("Pass " + i);
				for (String s : redirects.keySet()) {
					String result = redirects.get(redirects.get(s));
					if (result != null) {
						redirects.put(s, result);
					}
				}
			}
		}
		long length = new File("linkData").length();
		CountingInputStream count = new CountingInputStream(new BufferedInputStream(new FileInputStream(new File("linkData"))));
		DataInputStream in = new DataInputStream(count);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File("plainLinkData"))));
		int n = 0;
		while (count.bytesRead < length) {
			String title = in.readUTF();
			int c = in.readInt();
			Set<String> links = new HashSet<>();
			for (int i = 0; i < c; i++) {
				
				String link = in.readUTF();
				String resolved = redirects.get(link);
				if (resolved == null) {
					if (!link.isEmpty()) links.add(link);
				} else {
					if (!resolved.isEmpty()) {
						links.add(resolved);
					}
				}
			}
			out.writeUTF(title);
			out.writeInt(links.size());
			for (String s : links) {
				out.writeUTF(s);
			}
			n++;
			
			if (n % 50000 == 0) {
				System.out.println(count.bytesRead + " / " + length);
			}
		}
		in.close();
		out.close();
	}
	
	/**
	 * Parse the entire wikipedia xml named output.xml and extract a list of links and redirects.
	 */
	public static void extractLinksAndRedirects() throws Exception {
		CountingInputStream in = new CountingInputStream(new BufferedInputStream(new FileInputStream(new File("output.xml")), 1024 * 1024 * 100));
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File("linkData")), 8192 * 2));
		DataOutputStream redirectsOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File("redirectData"))));
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		boolean inPage = false;
		boolean inArticle = false;
		int i = 0;
		long l = new File("output.xml").length();
		
		String title = "";
		String redirect = "";
		Set<String> links = new HashSet<>();
		while ((line = reader.readLine()) != null) {
			if (!inPage) {
				if (line.contains("<page>")) {
					inPage = true;
				}
			} else {
				if (line.length() < 20 && line.contains("</page>")) {
					inPage = false;
				} else {
					if (!inArticle) {
						if (line.contains("<title>")) {
							title = getPageTitle(line);
							if (isSubNameSpace(title)) {
								inPage = false;
							}
						} else if (line.contains("<redirect title=")) {
							redirect = getPageRedirect(line);
							redirectsOut.writeUTF(title);
							redirectsOut.writeUTF(redirect);
							inPage = false;
						} else if (line.contains("<text ")) {
							inArticle = true;
						}
					} else {
						if (line.contains("</text>")) {
							inArticle = false;
							out.writeUTF(title);
							out.writeInt(links.size());
							for (String s : links) {
								out.writeUTF(s);
							}
							links.clear();
						} else {
							getPageLinks(line, links);
						}
					}
				}
			}
			if (i % 50000 == 0) {
				System.out.println(in.bytesRead / 1000 / 1000 + " / " + l / 1000 / 1000);
			}
			i++;
		}
		out.close();
		redirectsOut.close();
		reader.close();
	}
	
	/**
	 * Extract the links from a wikipedia page to a set.
	 */
	public static void getPageLinks(String page, Set<String> links) {
		StringBuilder chunk = new StringBuilder(20);
		boolean collecting = false;
		for (int i = 0; i < page.length(); i++) {
			if (!collecting) {
				if (i + 1 < page.length()) if (page.charAt(i) == '[' && page.charAt(i + 1) == '[') {
					i++;
					collecting = true;
				}
			} else {
				if (page.charAt(i) != ']') {
					chunk.append(page.charAt(i));
				}
				if (i + 1 < page.length()) if (page.charAt(i) == ']' && page.charAt(i + 1) == ']') {
					i++;
					collecting = false;
					String link = chunk.toString();
					
					if (link.contains("|")) {
						link = link.substring(0, link.indexOf("|"));
					}
					if (link.contains("#")) {
						link = link.substring(0, link.indexOf("#"));
					}
					if (!isSubNameSpace(link)) {
						if (link.length() > 0) {
							link = normalizeTitle(link);
							if (link.length() < 200) {
								links.add(unescape(link));
							}
						}
					}
					//            chunk.delete(0, chunk.capacity());
					chunk = new StringBuilder(20);
				}
			}
		}
	}
	
	/**
	 * Check if an article name is not in the main name space.
	 */
	public static boolean isSubNameSpace(String name) {
		int c = name.indexOf(":");
		if (c == -1) return false;
		if (c < name.length() - 1) {
			if (name.charAt(c + 1) != ' ') {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Find the result of an XML redirect wikipedia page.
	 */
	public static String getPageRedirect(String page) {
		if (page.contains("<redirect title=")) {
			return normalizeTitle(unescape(page.substring(page.indexOf("<redirect title=\"") + 17, page.indexOf("\" />"))));
		} else {
			return null;
		}
	}
	
	/**
	 * Find the title of an XML wikipedia page.
	 */
	public static String getPageTitle(String page) {
		String title = page.substring(page.indexOf("<title>") + 7, page.indexOf("</title>"));
		title = unescape(title);
		title = normalizeTitle(title);
		//(title.charAt(0) + "").toUpperCase() + title.substring(1);
		
		return title;
	}
	
	/**
	 * The first character of a wikipedia title is case insensitive, while the rest of the title is
	 * case sensitive. This function will capitalize the first letter to make them all match.
	 */
	public static String normalizeTitle(String title) {
		if (Character.isUpperCase(title.charAt(0))) {
			return title;
		}
		return Character.toUpperCase(title.charAt(0)) + title.substring(1);
	}
	
	private static final Pattern unescape = Pattern.compile("&#([0-9]+);|&(amp|lt|gt|apos|quot|nbsp);");
	
	/**
	 * Simple function for unescaping the XML text.
	 */
	public static String unescape(String text) {
		Matcher m = unescape.matcher(text);
		StringBuilder sb = new StringBuilder();
		int lastPos = 0;
		while (m.find()) {
			if (m.group(1) != null) {
				sb.append(text, lastPos, m.start(1) - 2);
				char replacement = (char) Integer.parseInt(m.group(1));
				//        System.out.println(Integer.parseInt(m.group(1)));
				sb.append(replacement);
				lastPos = m.end(1) + 1;
			} else {
				sb.append(text, lastPos, m.start(2) - 1);
				
				String g = m.group(2);
				if (g.equals("amp")) {
					sb.append("&");
				} else if (g.equals("lt")) {
					sb.append("<");
				} else if (g.equals("gt")) {
					sb.append(">");
				} else if (g.equals("apos")) {
					sb.append("'");
				} else if (g.equals("quot")) {
					sb.append("\"");
				} else if (g.equals("nbsp")) {
					sb.append(" ");
				}
				lastPos = m.end(2) + 1;
			}
		}
		sb.append(text, lastPos, text.length());
		
		return sb.toString();
		
	}
}
