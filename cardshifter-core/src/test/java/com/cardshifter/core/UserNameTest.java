package com.cardshifter.core;

import com.cardshifter.core.username.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UserNameTest {

    @RunWith(Parameterized.class)
    public static class ValidTest {

        @Parameterized.Parameters(name = "{index}: \"{0}\"")
        public static Iterable<String[]> data() {
            return Arrays.asList(new String[][] {
                    {"a"}, {"abcdefghijklmnopqrst"}, {"0123456789"}, {"_____"}, {"abc def"}
            });
        }

        @Parameterized.Parameter
        public String input;

        @Test
        public void testValid() throws InvalidUserNameException {
            UserName.create(input);
        }

    }

    @RunWith(Parameterized.class)
    public static class InvalidTest {

        @Parameterized.Parameters(name = "{index}: name=\"{0}\", expected error=\"{1}\"")
        public static Iterable<String[]> data() {
            return Arrays.asList(new String[][] {
                    { " abcdef", "Starts or ends with space"},
                    { "abcdef ", "Starts or ends with space"},
                    { "", "Too short" },
                    { "abcdefghijklmnopqrstu", "Too long" },
            });
        }

        @Parameterized.Parameter(value=0)
        public String input;

        @Parameterized.Parameter(value=1)
        public String expectedError;

        @Test
        public void testInvalid() {
            try {
                UserName.create(input);
                fail("Did not throw InvalidUserNameException");
            } catch (InvalidUserNameException e) {
               assertTrue(e.getMessage().contains(expectedError));
            }
        }

    }

}
