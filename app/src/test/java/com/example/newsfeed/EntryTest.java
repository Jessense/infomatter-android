package com.example.newsfeed;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EntryTest {
    Entry entry;
    @Before
    public void setUp() throws Exception {
        System.out.println("开始测试");
        this.entry = new Entry("title", "link", "2019-01-07T10:30:16.000Z");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("结束测试");
    }

    @Test
    public void geLocalPubTimeTest() {
        assertEquals("18:30", this.entry.geLocalPubTime());
        assertEquals("10:30", this.entry.geLocalPubTime());
    }
}