package com.cardshifter.core;

import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class GameCreateTest {
    public static TestSuite suite() {
        return new TestUtils().testCreateSuite();
    }

}
