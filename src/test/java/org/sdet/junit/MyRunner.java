package org.sdet.junit;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.sdet.junit.extension.CustomSuite;
import org.sdet.junit.extension.rule.Retry;
import org.sdet.junit.HelloJUnit;

@RunWith(CustomSuite.class)
//@Authors({"michaeljyan"})
//@Priorities({High.class, Middle.class})
@Suite.SuiteClasses({HelloJUnit.class})
public class MyRunner {

}
