package edu.ucsd.cse110.zooseeker.data.search;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class MultiWayTree<V> {

	Class<V> clazz;

	List<String> keys;
	Node<V> root;

	public MultiWayTree(Class<V> clazz)
	{
		this.clazz = clazz;
		keys = new ArrayList<String>();
		root = new Node<V>(-1,0,0);
	}

	public String getKey(int id)
	{
		return keys.get(id);
	}

	public void insertUseId(String key) throws UnsupportedOperationException
	{
		if(clazz.equals(Integer.class))
			insert(key, (V)new Integer(keys.size()));
		else
			throw new UnsupportedOperationException("insertUseId(String key) can only be called in SuffixTree that holds the Integer type");
	}

	public void insert(String key, V... v)
	{
		insert(key, Arrays.asList(v));
	}
	public void insert(String key, Collection<V> v)
	{
		if(!key.endsWith("$"))
			key = key + '$';
		keys.add(key);
		int id = keys.size() - 1;

		Node<V> curr = root;
		int len = 0;
		for(int j = 0; j < key.length(); j++)
		{
			if(curr.length() == len)
			{
				//Next Node
				if(curr.children.containsKey(key.charAt(j)))
				{
					//Move
					curr = curr.children.get(key.charAt(j));
					len = 1;

					//Place values
					if(j == key.length()-1 && len == curr.length())
					{
						curr.values.addAll(v);
					}
				}
				else
				{
					//Place leaf
					Node<V> leaf = new Node<V>(id, j, key.length(),v);
					leaf.parent = curr;
					curr.children.put(key.charAt(j), leaf);

					break;
				}
			}
			else
			{
				//Step Edge
				int pos = curr.beg + len;
				if(keys.get(curr.id).charAt(pos) == key.charAt(j))
				{
					//Move
					len++;

					//Place values
					if(j == key.length()-1 && len == curr.length())
					{
						curr.values.addAll(v);
					}
				}
				else
				{
					//Place values
					Node<V> trunk = new Node<V>(curr.id, pos, curr.end);
					trunk.children = curr.children;
					for(Node<V> node : trunk.children.values())
						node.parent = trunk;
					trunk.values = curr.values;
					curr.end = pos;
					curr.children = new HashMap<Character,Node>();
					curr.children.put(keys.get(curr.id).charAt(curr.end),trunk);
					trunk.parent = curr;

					Node<V> branch = new Node<V>(id,j,key.length(),v);
					curr.children.put(key.charAt(j), branch);
					branch.parent = curr;
					break;
				}
			}
		}
	}

	private Node<V> navigate(String key)
	{
		Node<V> curr = root;
		int len = 0;
		for(char c : key.toCharArray())
		{
			if(curr.length() == len)
			{
				if(curr.children.containsKey(c))
				{
					curr = curr.children.get(c);
					len = 1;
				}
				else
					return null;
			}
			else
			{
				if(keys.get(curr.id).charAt(curr.beg + len) == c)
				{
					len++;
				}
				else
					return null;
			}
		}
		return curr;
	}

	public boolean contains(String str)
	{
		if(!str.endsWith("$"))
			str = str + '$';
		return navigate(str) != null;
	}

	private String getPrefixTo(Node leaf)
	{
		String ret = "";
		if(leaf.parent != null)
			ret += getSubstringOf(leaf) + getPrefixTo(leaf.parent);
		return ret;
	}

	private String getSubstringOf(Node<V> leaf)
	{
		if(leaf == root)
			return "";
		return keys.get(leaf.id).substring(leaf.beg,leaf.end);
	}

	private void putPossibleSuffixes(Node<V> root, Set<String> possible, String prefix)
	{
		if(root.children.size() == 0) {
			String s = prefix + getSubstringOf(root);
			possible.add(s);
		}
		else
			for(Node child : root.children.values())
				putPossibleSuffixes(child, possible, prefix + getSubstringOf(root));
	}

	public Set<String> getPossibleSuffixes(String key)
	{
		Set<String> possible = new HashSet<String>();
		Node<V> start = navigate(key);
		if(start != null)
			putPossibleSuffixes(start, possible, start.parent != null ? getPrefixTo(start.parent) : "");
		return possible;
	}

	public Set<V> getPossibleValues(String key)
	{
		Set<V> possible = new HashSet<V>();
		Stack<Node<V>> stack = new Stack<Node<V>>();
		Node<V> curr = navigate(key);
		stack.add(curr);
		if(curr != null)
			while(stack.size() > 0)
			{
				curr = stack.pop();
				if (curr.children.size() != 0) {
					for (Node<V> child : curr.children.values())
						stack.add(child);
				} else
					possible.addAll(curr.values);
			}
		return possible;
	}

	public String getSubstring(Node<V> node)
	{
		return keys.get(node.id).substring(node.beg, node.end);
	}

	private void debug(char ca, Node<V> root, Writer out, String s) throws IOException {
		if(root != this.root)
			out.write(String.format("*%s%c:%s%s%n",s,ca,this.getSubstring(root),root.children.size()==0?(root.values.toString()):""));
		else
			out.write("*\n");
		if(root.children.size() != 0)
			for(Character c : root.children.keySet())
				debug(c,root.children.get(c), out, s + "--");
	}

	@VisibleForTesting
	public String debug()
	{
		StringWriter out = new StringWriter();
		String s = "";
		try {
			debug('\0',root, out, "");
			s = out.toString();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}

	public static class Node<V> {
		//[beg,end)
		Node parent = null;
		int id, beg, end;
		Map<Character,Node> children;
		List<V> values;

		Node(int id, int beg, int end, @Nullable Collection<V> v)
		{
			this.id = id;
			this.beg = beg;
			this.end = end;
			children = new HashMap<Character,Node>();
			values = new ArrayList<V>();
			if (v != null)
				values.addAll(v);
		}

		Node(int id, int beg, int end) {
			this(id, beg, end, null);
		}

		public int length()
		{
			return end - beg;
		}

		public List<V> getValues()
		{
			return values;
		}

		public boolean storeValues()
		{
			return values != null;
		}
	}
}
