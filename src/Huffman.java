/*The MIT License (MIT)

Copyright (c) 2015 Ai Onda

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.*/

import java.io.*;
import java.util.*;

public class Huffman {
	public static final char EOF = (char) 253;
	public static final char DELIMITER = (char) 254;

	public static class Node implements Comparator<Node> {
		public char ch;
		public int weight;
		public Node left;
		public Node right;

		public Node() {

		}

		public Node(int weight) {
			this.weight = weight;
		}

		public Node(char ch, int weight) {
			this.ch = ch;
			this.weight = weight;
		}

		@Override
		public int compare(Node o1, Node o2) {
			// TODO Auto-generated method stub
			return o1.weight - o2.weight;
		}
	}

	// ENCODE
	public static void encode(String in_file, String out_file, String table_file)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(in_file));
		char[] input_arr = new char[4096];
		StringBuilder input = new StringBuilder();
		HashMap<Character, Integer> map = new HashMap<Character, Integer>();
		int length;
		while ((length = br.read(input_arr)) != -1) {
			input = new StringBuilder();
			input.append(input_arr);

			// storing frequency of each character in a HashMap
			char ch;
			for (int i = 0; i < input.length(); i++) {
				ch = input.charAt(i);
				if (map.containsKey(ch)) {
					map.put(ch, map.get(ch) + 1);
				} else {
					map.put(ch, 1);
				}
			}
		}
		br.close();
		map.put(EOF, 1);

		// debug
		// System.out.println("printing the frequency of each character");
		// for (Map.Entry<Character, Integer> entry : map.entrySet()) {
		// System.out.println(entry.getKey() + " " + entry.getValue());
		// }
		// System.out.println();

		// create min heap of Nodes
		PriorityQueue<Node> queue = new PriorityQueue<Node>(map.size(),
				new Node());
		for (Map.Entry<Character, Integer> entry : map.entrySet()) {
			queue.add(new Node(entry.getKey(), entry.getValue()));
		}

		// creating huffman tree
		Node min1, min2, newNode;
		while (queue.size() > 1) {
			min1 = queue.poll();
			min2 = queue.poll();
			newNode = new Node(min1.weight + min2.weight);
			newNode.left = min1;
			newNode.right = min2;
			queue.add(newNode);
		}

		Node tree = queue.poll();

		// create encoding table by traversing tree
		HashMap<Character, String> encoding = new HashMap<Character, String>();
		String s = "";
		traverse(tree, s, encoding);

		// debug
		// System.out.println("printing out encoding table");
		// for (Map.Entry<Character, String> entry : encoding.entrySet()) {
		// System.out.println(entry.getKey() + " " + entry.getValue());
		// }
		// System.out.println();

		// look up encoding for each character and convert to byte
		StringBuilder sb = new StringBuilder();
		FileOutputStream out = new FileOutputStream(out_file);
		byte[] output = null;

		br = new BufferedReader(new FileReader(in_file));
		while ((length = br.read(input_arr)) != -1) {
			System.out.println("LENGHT=" + length);
			input = new StringBuilder();
			sb = new StringBuilder();
			input.append(input_arr, 0, length);
			// debug
			// System.out.println("printing out each encoding");
			for (int i = 0; i < input.length(); i++) {
				sb.append(encoding.get(input.charAt(i)) + "");
				// debug
				// System.out.println(str.charAt(i) + " "
				// + encoding.get(str.charAt(i)));
			}
			// debug
			// System.out.println();
			if (length < 4096 && length % 8 != 0) {
				System.out.println("EOF=" + encoding.get(EOF));
				sb.append(encoding.get(EOF));
				System.out.println("in here");
				while (sb.length() % 8 != 0) {
					sb.append('0');
					System.out.println("in in here, length=" + sb.length());
				}
				
				
			}

			output = new byte[(int) Math.ceil(sb.length() / 8.0)];
			// converting string to bytes
			for (int i = 0; i < (sb.length() - 8); i = i + 8) {
				output[i / 8] = (byte) Integer.parseInt(sb.substring(i, i + 8),
						2);
			}

			out.write(output);
		}
		br.close();
		// pad string

		// debug
		// System.out.println("testing out output");
		// System.out.println(sb);
		out.close();

		BufferedWriter bw = new BufferedWriter(new FileWriter(table_file));

		for (Map.Entry<Character, String> entry : encoding.entrySet()) {
			bw.write(entry.getKey() + entry.getValue() + DELIMITER);
		}
		bw.close();
	}

	// traverse huffman tree to create encoding table
	public static void traverse(Node n, String s,
			HashMap<Character, String> encoding) {
		if (n.right == null && n.left == null) {
			encoding.put(n.ch, s);
		}
		if (n.left != null) {
			traverse(n.left, s + "0", encoding);
		}
		if (n.right != null) {
			traverse(n.right, s + "1", encoding);
		}
	}

	public static void decode(String in_file, String table_file, String out_file)
			throws IOException {
		HashMap<String, Character> encoding = new HashMap<String, Character>();
		BufferedReader br = new BufferedReader(new FileReader(table_file));
	
		char[] table = new char[4096];
		StringBuilder table_str = new StringBuilder();
		int length;
		while ((length = br.read(table)) != -1) {
			for(int i=0; i<length; i++){
				table_str.append(table[i]);
			}
		}
		//System.out.println("size:"+table_str.length());
		br.close();

		//System.out.println("string:"+table_str);
		String[] entries = table_str.toString().split("" + DELIMITER);
		//System.out.println("entries:"+entries.length);
		// add to HashMap
		for (String str : entries) {
			encoding.put(str.substring(1), str.charAt(0));
			System.out.println(encoding.get(str.substring(1)) + "=" + str.substring(1));

		}
	
		for (String str : encoding.keySet()){
			if(encoding.get(str) == EOF){
				System.out.println("EOF= " + EOF + " = " + str);
			}
		}
		
		FileInputStream in = new FileInputStream(in_file);
		byte[] input = new byte[4096];
		// look up sequence of bits in encoding table to find character
		StringBuilder binary = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		BufferedWriter bw = new BufferedWriter(new FileWriter(out_file));
		boolean flag = true;
		while (flag && (length = in.read(input)) != -1) {
			//System.out.println("length "+length);
			binary = new StringBuilder();
			for (int i = 0; i < length; i++) {
				// System.out.println("OUTPUT" + (byte) input[i]);
				binary.append(toBinary((0x000000FF & input[i])));
			}
			//System.out.println("length: "+binary.length());
			
			for (int i = 0; i < binary.length(); i++) {
				sb.append(binary.charAt(i));
				//System.out.println("sb" + sb);
				System.out.println("ENCODING. " + encoding.get(sb.toString()) + "=" + sb.toString());

				if (encoding.containsKey(sb.toString())) {
					if (encoding.get(sb.toString()) == EOF) {
						System.out.println("gets here");
						flag = false;
						break;
					}
//					System.out.println("Char: " + encoding.get(sb.toString()).toString());
					bw.write(encoding.get(sb.toString()).toString());
					sb = new StringBuilder();
				}
			}
		}

		in.close();
		bw.close();
	}

	public static String toBinary(int value) {
		int item = value;
		StringBuilder str = new StringBuilder();
		int r;
		while (str.length() < 8) {
			r = item % 2;
			item = (char) (item / 2);
			str.append(r);
		}
		return str.reverse().toString();
	}

	public static void main(String[] args) throws IOException {
		long startTime = 0, stopTime = 0;
		if (args[0].equals("encode")) {
			if (args.length == 2) {
				startTime = System.nanoTime();
				Huffman.encode(args[1], "encoded.txt", "encoding_table.txt");
				stopTime = System.nanoTime();
			} else {
				startTime = System.nanoTime();
				Huffman.encode(args[1], args[2], args[3]);
				stopTime = System.nanoTime();
			}
		}

		if (args[0].equals("decode")) {
			if (args.length == 2) {
				startTime = System.nanoTime();
				Huffman.decode(args[1], "encoding_table.txt", "decoded.txt");
				stopTime = System.nanoTime();
			} else {
				startTime = System.nanoTime();
				Huffman.decode(args[1], args[2], args[3]);
				stopTime = System.nanoTime();
			}
		}

		double seconds = (double) (stopTime - startTime) / 1000000000.0;
		System.out.println("Time: " + seconds + " seconds");
	}
}