package wikilink.numerical;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class RadixSort {
	
	/**
	 * Simple test.
	 */
	public static void main(String[] args) {
		int[] array = new int[1000000];
		for (int i = 0; i < array.length; i++) {
			//Random int in the entire range of integers.
			array[i] = ThreadLocalRandom.current().nextInt();
		}
		//wait a second for the JVM to fully start for more accurate timing.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long start = System.currentTimeMillis();
		radixSort(array);
		System.out.println(System.currentTimeMillis() - start + " ms to sort");
		System.out.println("Is the large random array sorted? " + isSorted(array));
		
		array = new int[10];
		for (int i = 0; i < array.length; i++) {
			array[i] = ThreadLocalRandom.current().nextInt(-100, 100);
		}
		
		System.out.println("Short input: " + Arrays.toString(array));
		radixSort(array);
		System.out.println("Short output: " + Arrays.toString(array));
		
	}
	
	public static boolean isSorted(int[] list) {
		if (list.length <= 1) return true;
		int last = list[0];
		for (int i = 1; i < list.length; i++) {
			if (last > list[i]) {
				return false;
			}
			last = list[i];
		}
		return true;
	}
	
	public static void radixSort(int[] list) {
		int[] originalList = list;
		boolean hasNegative = false;
		boolean hasNextDigit = true;
		
		int[] outputList = new int[list.length];
		int[] counts = new int[16];
		int digitIndex = 0;
		
		while (hasNextDigit) {
			hasNextDigit = false;
			//A bit list that will remove all bits at or under the current hex digit as well as the sign bit when used with bitwise and.
			int nextCheck = (0xFFFFFFFF << Math.min(31, digitIndex * 4 + 4)) & 0x7FFFFFFF;
			for (int i = 0; i < list.length; i++) {
				//extract one hex digit
				int digit = (list[i] >> (digitIndex * 4)) & 0xF;
				
				counts[digit]++;
				
				//If there is already another check coming stop testing AND skip negative check since it can be done later.
				if (!hasNextDigit) {
					//If the number is larger than the passed hex digits can represent then another iteration is needed.
					if ((list[i] & nextCheck) != 0) {
						hasNextDigit = true;
					}
					
					if (!hasNegative && list[i] < 0) {
						hasNegative = true;
					}
				}
			}
			//Convert the count array to indexes.
			for (int i = 1; i < counts.length; i++) {
				counts[i] += counts[i - 1];
			}
			//Reorder the elements
			for (int i = list.length - 1; i >= 0; i--) {
				//extract one hex digit
				int digit = (list[i] >> (digitIndex * 4)) & 0xF;
				int index = (counts[digit] = counts[digit] - 1);
				
				outputList[index] = list[i];
			}
			for (int i = 0; i < counts.length; i++) {
				counts[i] = 0;
			}
			digitIndex++;
			//Swap the lists for the next pass.
			int[] swap = list;
			list = outputList;
			outputList = swap;
		}
		//Handle the negative "digit"
		if (hasNegative) {
			for (int i = 0; i < list.length; i++) {
				int digit = list[i] >= 0 ? 1 : 0;
				
				counts[digit]++;
			}
			counts[1] += counts[0];
			for (int i = list.length - 1; i >= 0; i--) {
				int digit = list[i] >= 0 ? 1 : 0;
				int index = (counts[digit] = counts[digit] - 1);
				
				outputList[index] = list[i];
			}
			int[] swap = list;
			list = outputList;
			outputList = swap;
		}
		//If the original list is not the one last written to then the output must be copied over.
		if (originalList == outputList) {
			System.arraycopy(list, 0, outputList, 0, list.length);
		}
	}
}
