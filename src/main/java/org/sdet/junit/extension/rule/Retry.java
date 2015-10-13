package org.sdet.junit.extension.rule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class Retry implements TestRule {

	private int count;

	public Retry(int count) {
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}

	@Override
	public Statement apply(final Statement base, final Description description) {
		// TODO Auto-generated method stub
//		return statement(base, description);
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				// TODO Auto-generated method stub
				base.evaluate();			
			}};
	}

}
