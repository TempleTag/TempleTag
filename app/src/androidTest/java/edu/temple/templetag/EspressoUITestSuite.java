package edu.temple.templetag;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SignUpTest.class,
        SignInTest.class,
        LogoutTest.class,
        RecyclerViewTest.class,
        CreateTagTest.class,
        })
public class EspressoUITestSuite {
}
