package wikilink.numerical;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class NumWikiLink {
	
	public static void main(String[] args) throws Exception {
		NumericalIndex normal = new NumericalIndex(new File("numericalIndex"));
		System.out.println("Normal Loaded");
		@SuppressWarnings("resource")
		Scanner s = new Scanner(System.in);
		Comparator<List<String>> order = NumWikiLink::simplestLast;
		while (true) {
			System.out.print("Command: ");
			String command = s.nextLine().trim();
			if (command.equalsIgnoreCase("paths")) {
				System.out.print("Start: ");
				String a = s.nextLine().trim();
				System.out.print("End: ");
				String b = s.nextLine().trim();
				long start = System.nanoTime();
				List<List<String>> path = findPaths(normal, normal.getID(a), normal.getID(b));
				long time = System.nanoTime() - start;
				printPaths(path, order);
				System.out.println(time(time));
			} else if (command.equalsIgnoreCase("path")) {
				System.out.print("Start: ");
				String a = s.nextLine().trim();
				System.out.print("End: ");
				String b = s.nextLine().trim();
				long start = System.nanoTime();
				List<String> path = findPath(normal, normal.getID(a), normal.getID(b));
				long time = System.nanoTime() - start;
				printPath(path);
				System.out.println(time(time));
			} else if (command.equalsIgnoreCase("depth")) {
				System.out.print("Start: ");
				String a = s.nextLine().trim();
				long start = System.nanoTime();
				List<String> path = findDeepestPath(normal, normal.getID(a));
				long time = System.nanoTime() - start;
				printPath(path);
				System.out.println(time(time));
			} else if (command.equalsIgnoreCase("paththrough") || command.equalsIgnoreCase("pathpast")) {
				System.out.print("Start: ");
				String a = s.nextLine().trim();
				System.out.print("Passthrough: ");
				String b = s.nextLine().trim();
				System.out.print("End: ");
				String c = s.nextLine().trim();
				long start = System.nanoTime();
				List<String> path = findPath(normal, normal.getID(a), normal.getID(b));
				path.addAll(findPath(normal, normal.getID(b), normal.getID(c)));
				long time = System.nanoTime() - start;
				printPath(path);
				System.out.println(time(time));
			} else if (command.equalsIgnoreCase("pathsthrough") || command.equalsIgnoreCase("pathspast")) {
				System.out.print("Start: ");
				String a = s.nextLine().trim();
				System.out.print("Passthrough: ");
				String b = s.nextLine().trim();
				System.out.print("End: ");
				String c = s.nextLine().trim();
				long start = System.nanoTime();
				List<List<String>> path = findPaths(normal, normal.getID(a), normal.getID(b));
				List<List<String>> path2 = findPaths(normal, normal.getID(b), normal.getID(c));
				List<List<String>> result = new ArrayList<>();
				for (List<String> pa : path) {
					pa.remove(pa.size() - 1);
					for (List<String> pb : path2) {
						List<String> next = new ArrayList<>();
						next.addAll(pa);
						next.addAll(pb);
						result.add(next);
					}
				}
				long time = System.nanoTime() - start;
				printPaths(result, order);
				System.out.println(time(time));
			} else if (command.equalsIgnoreCase("order")) {
				System.out.print("Ordering (simplest, alphabetical, reverse alphabetical): ");
				String a = s.nextLine().trim().toLowerCase();
				if ("simplest".startsWith(a)) {
					order = NumWikiLink::simplestLast;
				} else if ("alphabetical".startsWith(a)) {
					order = NumWikiLink::alphabetical;
				} else if ("reverse alphabetical".startsWith(a)) {
					order = NumWikiLink::alphabeticalReverse;
				} else {
					System.out.println("Invalid option.");
				}
			}
			
		}
		
	}
	
	/**
	 * Convert a nanosecond time duration to milliseconds or seconds depending on the length of
	 * time.
	 */
	public static String time(long nanos) {
		if (nanos < 1000000) {
			return nanos + " ns.";
		}
		if (nanos > 1000000 && nanos < 1000000000) {
			return nanos / 1000000.0 + " ms.";
		}
		return nanos / 1000000000.0 + " s.";
	}
	
	/**
	 * Print function for printing a path list.
	 */
	public static void printPaths(List<List<String>> paths, Comparator<List<String>> order) {
		if (paths.isEmpty()) {
			System.out.println("No path");
			return;
		}
		paths.sort(order);
		List<Integer> lengths = new ArrayList<>();
		for (List<String> ls : paths) {
			for (int i = 0; i < ls.size(); i++) {
				if (lengths.size() <= i) {
					lengths.add(0);
				}
				lengths.set(i, Math.max(ls.get(i).length(), lengths.get(i)));
			}
		}
		
		String format = "";
		for (int i : lengths) {
			format += " >> %-" + i + "s";
		}
		for (List<String> ls : paths) {
			System.out.printf(format, ls.toArray());
			System.out.println();
		}
		System.out.println(paths.size() + " paths found.");
	}
	
	/**
	 * Make a string representation of a list of paths.
	 */
	public static String renderPaths(List<List<String>> paths, Comparator<List<String>> order) {
		StringBuilder sb = new StringBuilder();
		if (paths.isEmpty()) {
			return "No path\n";
		}
		paths.sort(order);
		List<Integer> lengths = new ArrayList<>();
		for (List<String> ls : paths) {
			for (int i = 0; i < ls.size(); i++) {
				if (lengths.size() <= i) {
					lengths.add(0);
				}
				lengths.set(i, Math.max(ls.get(i).length(), lengths.get(i)));
			}
		}
		
		String format = "";
		for (int i : lengths) {
			format += " >> %-" + i + "s";
		}
		for (List<String> ls : paths) {
			sb.append(String.format(format, ls.toArray()));
			sb.append("\n");
		}
		sb.append(paths.size() + " paths found.\n");
		return sb.toString();
	}
	
	//Path sort orders.
	public static int simplestLast(List<String> a, List<String> b) {
		int aSize = 0;
		int bSize = 0;
		for (String s : a) {
			aSize += s.length();
		}
		for (String s : b) {
			bSize += s.length();
		}
		return bSize - aSize;
	}
	
	public static int simplestFirst(List<String> a, List<String> b) {
		int aSize = 0;
		int bSize = 0;
		for (String s : a) {
			aSize += s.length();
		}
		for (String s : b) {
			bSize += s.length();
		}
		return aSize - bSize;
	}
	
	public static int alphabetical(List<String> a, List<String> b) {
		int index = 0;
		while (index < a.size()) {
			int comp = a.get(index).compareTo(b.get(index));
			if (comp != 0) {
				return comp;
			}
			index++;
		}
		return 0;
	}
	
	public static int alphabeticalReverse(List<String> a, List<String> b) {
		int index = a.size() - 1;
		while (index >= 0) {
			int comp = a.get(index).compareTo(b.get(index));
			if (comp != 0) {
				return comp;
			}
			index--;
		}
		return 0;
	}
	
	/**
	 * Function to print one path.
	 */
	public static void printPath(List<String> paths) {
		if (paths.isEmpty()) {
			System.out.println("No path");
		}
		paths.forEach(e -> System.out.print(" >> " + e));
		System.out.println();
	}
	
	/**
	 * Make a string representation of a path
	 */
	public static String renderPath(List<String> paths) {
		StringBuilder sb = new StringBuilder();
		if (paths.isEmpty()) {
			sb.append("No path\n");
		}
		paths.forEach(e -> sb.append(" >> " + e));
		return sb.toString();
	}
	
	/**
	 * Find a path between two articles.
	 */
	public static List<String> findPath(NumericalIndex index, int start, int end) {
		if (start == -1 || end == -1) {
			return new ArrayList<>();
		}
		int count = index.entries.size();
		StepQueue queue = new StepQueue(count);
		queue.put(-1, start);
		while (queue.size() != 0) {
			int page = queue.next();
			int[] next = index.getLinks(page);
			queue.put(page, next);
			for (int i : next) {
				if (i == end) {
					queue.start = queue.end;
					break;
				}
			}
			
		}
		System.out.println(queue.lookups + " lookups.");
		
		List<Integer> path = queue.path(end);
		List<String> output = new ArrayList<>();
		for (int i = path.size() - 1; i >= 0; i--) {
			output.add(index.getTitle(path.remove(i)));
		}
		
		return output;
	}
	
	/**
	 * Find the deepest path starting at an article. Kind of always has the same result.
	 */
	public static List<String> findDeepestPath(NumericalIndex index, int start) {
		if (start == -1) {
			return new ArrayList<>();
		}
		int count = index.entries.size();
		StepQueue queue = new StepQueue(count);
		queue.put(-1, start);
		int end = start;
		while (queue.size() != 0) {
			int page = queue.next();
			int[] next = index.getLinks(page);
			queue.put(page, next);
			end = page;
			
		}
		System.out.println(queue.lookups + " lookups.");
		
		List<Integer> path = queue.path(end);
		List<String> output = new ArrayList<>();
		for (int i = path.size() - 1; i >= 0; i--) {
			output.add(index.getTitle(path.remove(i)));
		}
		
		return output;
	}
	
	/**
	 * Find all shortest paths between two articles.
	 */
	public static List<List<String>> findPaths(NumericalIndex index, int start, int end) {
		if (start == -1 || end == -1) {
			return new ArrayList<>();
		}
		Tree base = new Tree();
		base.value = end;
		List<Tree> next = new ArrayList<>();
		next.add(base);
		Map<Integer, Tree> results = new HashMap<>();
		int lookups = 0;
		while (next.size() > 0) {
			Tree fill = next.remove(next.size() - 1);
			Set<Integer> parents = findParents(index, start, fill.value);
			System.out.println(index.getTitle(start) + " -> " + index.getTitle(fill.value));
			lookups++;
			for (int i : parents) {
				if (results.containsKey(i)) {
					fill.children.add(results.get(i));
				} else {
					Tree child = new Tree();
					child.value = i;
					fill.children.add(child);
					if (start != fill.value) next.add(child);
				}
				
			}
			results.put(fill.value, fill);
		}
		System.out.println("Performed " + lookups + " searches.");
		return treeToAllPaths(index, base);
	}
	
	/**
	 * Convert a tree of optimal options to a list of paths.
	 */
	public static List<List<String>> treeToAllPaths(NumericalIndex index, Tree root) {
		if (root.children.isEmpty()) {
			List<List<String>> lines = new ArrayList<>();
			List<String> me = new ArrayList<>();
			me.add(index.getTitle(root.value));
			lines.add(me);
			return lines;
		} else {
			List<List<String>> lines = new ArrayList<>();
			for (Tree b : root.children) {
				lines.addAll(treeToAllPaths(index, b));
			}
			for (List<String> line : lines) {
				line.add(index.getTitle(root.value));
			}
			return lines;
		}
	}
	
	/**
	 * Find all optimal places to look for that link to an end point.
	 */
	public static Set<Integer> findParents(NumericalIndex index, int start, int end) {
		if (start == -1 || end == -1 || start == end) {
			return new HashSet<>();
		}
		int count = index.entries.size();
		StepsQueue queue = new StepsQueue(count);
		queue.put(-1, start);
		Set<Integer> parents = new HashSet<>();
		while (queue.size() != 0) {
			int page = queue.next();
			if (page == -2) {
				if (parents.isEmpty()) {
					continue;
				} else {
					break;
				}
			}
			int[] next = index.getLinks(page);
			queue.put(page, next);
			for (int i : next) {
				if (i == end) {
					parents.add(page);
				}
			}
			
		}
		return parents;
	}
	
}

/**
 * Large cyclic queue for tracking every article's parent in the search as well as what has been
 * passed.
 */
class StepQueue {
	
	int[] backing;
	int[] parents;
	boolean[] found;
	int start = 0;
	int end = 0;
	int lookups = 0;
	
	public StepQueue(int size) {
		backing = new int[size];
		found = new boolean[size];
		parents = new int[size];
	}
	
	public void put(int parent, int... entries) {
		for (int i : entries) {
			if (!found[i]) {
				found[i] = true;
				backing[end] = i;
				parents[i] = parent;
				end++;
				if (end >= backing.length) {
					end = 0;
				}
			}
		}
	}
	
	public List<Integer> path(int endpoint) {
		List<Integer> path = new ArrayList<>();
		while (endpoint != -1 && found[endpoint]) {
			path.add(endpoint);
			endpoint = parents[endpoint];
		}
		return path;
	}
	
	public int next() {
		if (start == end) {
			return -1;
		}
		lookups++;
		int v = backing[start++];
		if (start >= backing.length) {
			start = 0;
		}
		return v;
	}
	
	public int size() {
		return Math.abs(start - end);
	}
}

//An extension of the StepQueue which inserts a -2 every time the path gets longer. 
//Useful for finding all paths of the same length.
class StepsQueue extends StepQueue {
	
	boolean[] lastSet;
	
	public StepsQueue(int size) {
		super(size);
		lastSet = new boolean[size];
	}
	
	public void put(int parent, int... entries) {
		if (parent != -1 && lastSet[parent]) {
			lastSet = new boolean[lastSet.length];
			backing[end] = -2;
			end++;
			if (end >= backing.length) {
				end = 0;
			}
		}
		for (int i : entries) {
			if (!found[i]) {
				found[i] = true;
				backing[end] = i;
				parents[i] = parent;
				lastSet[i] = true;
				end++;
				if (end >= backing.length) {
					end = 0;
				}
			}
		}
		
	}
}

/**
 * Simple tree for storing all possible paths.
 */
class Tree {
	
	List<Tree> children = new ArrayList<>();
	int value;
	
	public void add(int child) {
		Tree next = new Tree();
		next.value = child;
		children.add(next);
	}
}