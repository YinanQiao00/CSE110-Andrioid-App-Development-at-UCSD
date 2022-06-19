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

public class SuffixTree<V> extends MultiWayTree<V> {

	public SuffixTree(Class<V> clazz)
	{
		super(clazz);
	}

	@Override
	public void insert(String key, Collection<V> v)
	{
		if(!key.endsWith("$"))
			key = key + '$';
		for(int i = 0; i < key.length(); i++)
		{
			super.insert(key.substring(i),v);
		}
	}
}
