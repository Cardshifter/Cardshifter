package com.cardshifter.core;

import com.cardshifter.core.username.*;
import org.junit.*;

import static org.junit.Assert.fail;

public class UserNameTest {

    @Test
    public void testShort() {
        try {
            UserName.create("a");
        } catch (InvalidUserNameException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testLong() {
        try {
            UserName.create("abcdefghijklmnopqrst");
        } catch (InvalidUserNameException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testNumbers() {
        try {
            UserName.create("0123456789");
        }
        catch (InvalidUserNameException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSymbols() {
        try {
            UserName.create("_____");
        }
        catch (InvalidUserNameException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSpaceMiddle() {
        try {
            UserName.create("abc def");
        }
        catch (InvalidUserNameException e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = InvalidUserNameException.class)
    public void testSpaceFirst() throws InvalidUserNameException {
        UserName.create(" abcdef");
    }

    @Test(expected = InvalidUserNameException.class)
    public void testSpaceLast() throws InvalidUserNameException {
        UserName.create("abcdef ");
    }

    @Test(expected = InvalidUserNameException.class)
    public void testEmpty() throws InvalidUserNameException {
        UserName.create("");
    }

    @Test(expected = InvalidUserNameException.class)
    public void testTooLong() throws InvalidUserNameException {
        UserName.create("abcdefghijklmnopqrstu");
    }

}
