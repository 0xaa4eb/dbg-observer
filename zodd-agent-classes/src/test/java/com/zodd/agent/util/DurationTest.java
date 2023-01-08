package com.zodd.agent.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class DurationTest {

    @Test
    public void testHumanReadableFormat() {

        assertEquals("53 ns", new Duration(53).toString());
        assertEquals("980 ns", new Duration(980).toString());
        assertEquals("1,1 us", new Duration(1100).toString());
        assertEquals("9,902 us", new Duration(9902).toString());
        assertEquals("1 ms", new Duration(1_000_000).toString());
        assertEquals("1,105 ms", new Duration(1_105_020).toString());
        assertEquals("53,914 ms", new Duration(53_914_020).toString());
        assertEquals("1,055 s", new Duration(1_054_914_020L).toString());
        assertEquals("53,054 s", new Duration(53_054_914_020L).toString());
    }
}