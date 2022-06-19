package edu.ucsd.cse110.zooseeker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.ucsd.cse110.zooseeker.data.search.MultiWayTree;
import edu.ucsd.cse110.zooseeker.data.search.SuffixTree;

public class SuffixTreeTester {

    @Test
    public void multiWayTree()
    {
        MultiWayTree<Integer> tree = new MultiWayTree<Integer>(Integer.class);
        tree.insert("hello$",0);
        tree.insert("llo$",1);
        tree.insert("helium$",2);
        tree.insert("lost$",3);

        Set<String> set = tree.getPossibleSuffixes("h");
        assertEquals(2, set.size());
        assertTrue(set.contains("hello$"));
        assertTrue(set.contains("helium$"));

        set = tree.getPossibleSuffixes("e");
        assertEquals(0, set.size());

        set = tree.getPossibleSuffixes("l");
        assertEquals(2, set.size());
        assertTrue(set.contains("llo$"));
        assertTrue(set.contains("lost$"));
        assertFalse(set.contains("lo$"));

        assertTrue(tree.contains("hello"));
        assertTrue(tree.contains("llo"));
        assertTrue(tree.contains("helium"));
        assertTrue(tree.contains("lost"));
        assertFalse(tree.contains("lo"));
        assertFalse(tree.contains("lo$"));
    }

    @Test
    public void emptyTree()
    {
        SuffixTree<Integer> tree = new SuffixTree<Integer>(Integer.class);
        assertEquals(1, tree.getPossibleSuffixes("").size());
        assertEquals(0, tree.getPossibleSuffixes("a").size());
        assertEquals(0, tree.getPossibleSuffixes("b").size());
    }

    private void testSingleStringTree(String str)
    {
        SuffixTree<Integer> tree = new SuffixTree<Integer>(Integer.class);
        tree.insert(str, 0);

        Set<String> set = tree.getPossibleSuffixes("");
        assertEquals("Tree contains incorrect number of suffixes", str.length(), set.size());

        for(int i = 0; i < str.length(); i++)
            assertTrue("Tree does not contain " + str.substring(i), set.contains(str.substring(i)));
    }

    @Test
    public void simpleSingleStringTree()
    {
        testSingleStringTree("hello$");
    }

    @Test
    public void complexSingleStringTree()
    {
        //Simple repeats
        testSingleStringTree("aaaaaa$");
        testSingleStringTree("aaabbb$");
        testSingleStringTree("aaabbbaa$");
        testSingleStringTree("aaabbbaabb$");
        testSingleStringTree("aaabbbaaaabbb$");

        //Repeated repeats
        testSingleStringTree("aaabbbcccdddaabbccddabcd$");
        testSingleStringTree("abcdaabbccddaaabbbcccdddaaaabbbbccccdddd$");

        //Significant use of 'a'
        testSingleStringTree("abacadaeafabacadaeafaaaa$");
        testSingleStringTree("aabaacaadaaaeafabacaaadaeaafabcdefabbccddeeff$");

        //No repeats
        testSingleStringTree("abcdefghijklmnopqrstuvwxyz$");
    }

    private void testSingleSuffixFilter(String str)
    {
        SuffixTree<Integer> tree = new SuffixTree<Integer>(Integer.class);
        tree.insert(str, 0);

        for(int i = 0; i < str.length(); i++) {
            Set<String> set = tree.getPossibleSuffixes(str.substring(i));
            assertEquals(str.substring(i)+" should only have 1 possible expansion", 1, set.size());
        }
    }

    @Test
    public void simpleSingleSuffixFilter()
    {
        testSingleSuffixFilter("hello$");
    }

    @Test
    public void complexSingleSuffixFilter()
    {
        //Simple repeats
        testSingleSuffixFilter("aaaaaa$");
        testSingleSuffixFilter("aaabbb$");
        testSingleSuffixFilter("aaabbbaa$");
        testSingleSuffixFilter("aaabbbaabb$");
        testSingleSuffixFilter("aaabbbaaaabbb$");

        //Repeated repeats
        testSingleSuffixFilter("aaabbbcccdddaabbccddabcd$");
        testSingleSuffixFilter("abcdaabbccddaaabbbcccdddaaaabbbbccccdddd$");

        //Significant use of 'a'
        testSingleSuffixFilter("abacadaeafabacadaeafaaaa$");
        testSingleSuffixFilter("aabaacaadaaaeafabacaaadaeaafabcdefabbccddeeff$");

        //No repeats
        testSingleSuffixFilter("abcdefghijklmnopqrstuvwxyz$");
    }

    private void testMultiStringTree(Map<String,Integer> pairs)
    {
        SuffixTree<Integer> tree = new SuffixTree<Integer>(Integer.class);
        for(String key : pairs.keySet())
            tree.insert(key,pairs.get(key));

        Set<String> set = tree.getPossibleSuffixes("");
        Set<String> past = new HashSet<String>();

        for(String key : pairs.keySet())
        {
            for(int i = 0; i < key.length(); i++) {
                past.add(key.substring(i));
                assertTrue("Tree does not contain " + key.substring(i), set.contains(key.substring(i)));
            }
        }
        assertEquals("Tree contains incorrect number of suffixes", past.size(), set.size());
    }

    private void testMultiStringFilter(Map<String,Integer> pairs)
    {
        SuffixTree<Integer> tree = new SuffixTree<Integer>(Integer.class);
        for(String key : pairs.keySet())
            tree.insert(key,pairs.get(key));

        Set<String> set = tree.getPossibleSuffixes("");

        for(String str : set)
        {
            int count = tree.getPossibleSuffixes(str).size();
            assertEquals(String.format("%s should only have 1 possible expansion", str), 1, count);
        }
    }

    private void testMultiStringGet(Map<String,Integer> pairs)
    {
        SuffixTree<Integer> tree = new SuffixTree<Integer>(Integer.class);
        for(String key : pairs.keySet())
            tree.insert(key,pairs.get(key));

        Set<String> set = tree.getPossibleSuffixes("");

        for(String s : set)
        {
            Set<Integer> has = new HashSet<Integer>();
            for(String key : pairs.keySet())
            {
                if(key.endsWith(s))
                    has.add(pairs.get(key));
            }
            Set<Integer> vals = tree.getPossibleValues(s);
            for(Integer val : vals)
                assertTrue(String.format("%s should not be mapped to %s", s, val.toString()), has.contains(val));
            assertEquals(String.format("%s returned incorrect number of values",s), has.size(), vals.size());
        }
    }

    private static Map<String,Integer> pairs;
    private static Map<String,Integer>[] pairsArr;
    @Before
    public void init()
    {
        pairs = new HashMap<String,Integer>();
        pairs.put("hello$", 0);
        pairs.put("bye$", 1);

        pairsArr = new HashMap[4];
        int i = 0;

        //No overlap
        pairsArr[i] = new HashMap<String,Integer>();
        pairsArr[i].put("abc$", 0);
        pairsArr[i].put("def$", 1);
        pairsArr[i].put("ghi$", 2);

        //Every other string overlaps just the first string
        i++;
        pairsArr[i] = new HashMap<String,Integer>();
        pairsArr[i].put("abcabxabcd$", 0);
        pairsArr[i].put("aaaaaaaaaa$", 1);
        pairsArr[i].put("bbbbbbbbbb$", 2);
        pairsArr[i].put("cccccccccc$", 3);
        pairsArr[i].put("dddddddddd$", 4);

        //Sections of overlap
        i++;
        pairsArr[i] = new HashMap<String,Integer>();
        pairsArr[i].put("abc def$", 0);
        pairsArr[i].put("def jkl$", 1);
        pairsArr[i].put("def def$", 2);
        pairsArr[i].put("something generic$", 3);
        pairsArr[i].put("abcdefghijkl$", 4);

        //Identical
        i++;
        pairsArr[i] = new HashMap<String,Integer>();
        pairsArr[i].put("abcabxabcd$", 0);
        pairsArr[i].put("abcabxabcd$", 1);
    }

    @Test
    public void simpleMultiStringTree()
    {
        testMultiStringTree(pairs);
    }

    @Test
    public void simpleMultiStringFilter()
    {
        testMultiStringFilter(pairs);
    }

    @Test
    public void simpleMultiStringGet()
    {
        testMultiStringGet(pairs);
    }

    @Test
    public void complexMultiStringTree()
    {
        for(Map<String,Integer> map : pairsArr)
            testMultiStringTree(map);
    }

    @Test
    public void complexMultiStringFilter()
    {
        for(Map<String,Integer> map : pairsArr)
            testMultiStringFilter(map);
    }

    @Test
    public void complexMultiStringGet()
    {
        for(Map<String,Integer> map : pairsArr)
            testMultiStringGet(map);
    }

    @Test
    public void wrongTypeForId()
    {
        SuffixTree<String> tree = new SuffixTree<String>(String.class);
        assertThrows(UnsupportedOperationException.class, () -> {
            tree.insertUseId("Hello World");
        });
    }

    @Test
    public void correctTypeForId()
    {
        SuffixTree<Integer> tree = new SuffixTree<Integer>(Integer.class);
        tree.insertUseId("Hello World");
    }
}