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
	public static final char EOF = (char) 254;
	public static final char DELIMITER = (char) 255;

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
	public static void encode(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String str = br.readLine();
		br.close();
		HashMap<Character, Integer> map = new HashMap<Character, Integer>();
		// storing frequency of each character in a HashMap
		char ch;
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
			if (map.containsKey(ch)) {
				map.put(ch, map.get(ch) + 1);
			} else {
				map.put(ch, 1);
			}
		}
		map.put(EOF, 1);

		// debug
//		System.out.println("printing the frequency of each character");
//		for (Map.Entry<Character, Integer> entry : map.entrySet()) {
//			System.out.println(entry.getKey() + " " + entry.getValue());
//		}
//		System.out.println();

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
//		System.out.println("printing out encoding table");
//		for (Map.Entry<Character, String> entry : encoding.entrySet()) {
//			System.out.println(entry.getKey() + " " + entry.getValue());
//		}
//		System.out.println();

		// look up encoding for each character and convert to byte
		StringBuilder sb = new StringBuilder();
		// debug
//		System.out.println("printing out each encoding");
		for (int i = 0; i < str.length(); i++) {
			sb.append(encoding.get(str.charAt(i)));
			// debug
//			System.out.println(str.charAt(i) + " "
//					+ encoding.get(str.charAt(i)));
		}
		// debug
//		System.out.println();
		sb.append(encoding.get(EOF));

		// pad string
		while (!(sb.length() % 8 == 0)) {
			sb.append('0');
		}

		// debug
//		System.out.println("testing out output");
//		System.out.println(sb);

		byte[] output = new byte[(int) Math.ceil(sb.length() / 8.0)];
		// converting string to bytes
		for (int i = 0; i < sb.length(); i = i + 8) {
			output[i / 8] = (byte) Integer.parseInt(sb.substring(i, i + 8), 2);
		}

		FileOutputStream out = new FileOutputStream("encoded.txt");
		out.write(output);
		out.close();

		BufferedWriter bw = new BufferedWriter(new FileWriter(
				"encoding_table.txt"));

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

	public static void decode(String filename) throws IOException {
		HashMap<String, Character> encoding = new HashMap<String, Character>();
		BufferedReader br = new BufferedReader(new FileReader(
				"encoding_table.txt"));
		String table = br.readLine();
		br.close();
//		System.out.println("table:" + table);
//		System.out.println("delimiter:" + DELIMITER);
		String[] entries = table.split("" + DELIMITER);
		// add to HashMap
//		System.out.println("entries:" + entries.length);
		for (String str : entries) {
//			System.out.println("str:" + str);
			encoding.put(str.substring(1), str.charAt(0));
		}

		FileInputStream in = new FileInputStream(filename);
		byte[] input = new byte[4096];
		int length = 0;
		// look up sequence of bits in encoding table to find character
		StringBuilder binary = new StringBuilder();

		while ((length = in.read(input)) != -1) {
			for (int i = 0; i < length; i++) {
//				System.out.println("OUTPUT" + (byte) input[i]);
				binary.append(toBinary((0x000000FF & input[i])));
			}
		}

//		System.out.println(binary);

		StringBuilder decoded = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < binary.length(); i++) {
			sb.append(binary.charAt(i));
//			System.out.println("sb" + sb);
			if (encoding.containsKey(sb.toString())) {
				if(encoding.get(sb.toString()).equals(EOF)){
					break;
				}
				decoded.append(encoding.get(sb.toString()));
				sb = new StringBuilder();
			}
		}
		in.close();

		BufferedWriter bw = new BufferedWriter(new FileWriter("decoded.txt"));
		bw.write(decoded.toString());
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
		if(args[0].equals("encode")){
			Huffman.encode(args[1]);
		}
		if(args[0].equals("decode")){
			Huffman.decode(args[1]);
		}
	}
}