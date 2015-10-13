package org.sdet.junit.extension;

import org.junit.ClassRule;
import org.sdet.junit.extension.rule.Retry;

public abstract class TestBase {
	@ClassRule
    public static Retry retry = new Retry(2);
}