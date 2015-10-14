## junit-extension

### 背景

原生的Junit无法满足我们在自动化测试实践过程中的碰到一些需求，比如

- 管理人员希望统计测试组每个成员开发的用例数目, 要是每个用例都能够有注释
- 在本地调试测试用例时，测试工程师希望只运行于自己开发的用例
- 系统测试时需要执行全量用例，回归测试时希望跑High优先级的用例
- 测试用例运行失败后能够有自动重试的机制
- junit产生的测试报告希望与内部系统集成，有一些定制需求
- ......

### 注解

- ` @Author`注解测试用例的作者
- `@Priority`注解测试用例的优先级，有`High.class`, `Middle.class`, `Low.class`可选
- `@Comment`注解测试用例的注释

以上三种注解和Junit build-in的注解`@Test`配合使用，示例如下   

```java
import org.junit.Test;
import org.sdet.junit.extension.TestBase;
import org.sdet.junit.extension.annotation.Author;
import org.sdet.junit.extension.annotation.High;
import org.sdet.junit.extension.annotation.Middle;
import org.sdet.junit.extension.annotation.Priority;

import static org.junit.Assert.assertEquals;

public class HelloJUnitExtension extends TestBase {
    // 该测试用例的作者是michaelyan，优先级是Middle
    @Test
    @Author("michaelyan")
    @Priority(Middle.class)
    public void shouldFail() {
        assertEquals(1, 0);
    }
    ......
}
```

- `@AuthorFilter`注解目标测试用例的作者，
- `@PriorityFilter`注解目标测试用例的优先级

以上两种注解用来注解自定义的Runner

```java
//
@RunWith(CustomSuite.class)
@AuthorFilter({"michaeljyan"})
@PriorityFilter({High.class, Middle.class})
@Suite.SuiteClasses({HelloJUnitExtension.class})
public class MyRunner {}
```

#### 测试用例加载逻辑 [CustomSuite](https://github.com/yanjian824/junit-extension/blob/master/src/main/java/org/sdet/junit/extension/CustomSuite.java)

类`CustomSuite`的方法`run(RunNotifier notifier)`与基类`ParentRunner<T>`的实现比，只多了一行代码。如果没有这一行，RunListener的回调函数`public void testRunStarted(Description description)`不会执行。
```java
notifier.fireTestRunStarted(description);
```

#### 测试用例过滤逻辑 [CustomFilter](https://github.com/yanjian824/junit-extension/blob/master/src/main/java/org/sdet/junit/extension/CustomFilter.java)

详情请参考下面3个API实现
```java
@Override
public boolean shouldRun(Description description) {}

private boolean shouldfilterByAuthor(Description description) {}

private boolean shouldfilterByPriority(Description description) {}
```

### 失败重试

JUnit中测试用例运行是通过`ParentRunner<T>`的`runLeaf(Statement statement, Description description, RunNotifier notifier)`方法完成的。junit-extension对runLeaf做了一个简单的封装，如果运行失败了，测试用例会重新执行，重试的次数通过classRule动态获取。
只要测试类是从[TestBase](https://github.com/yanjian824/junit-extension/blob/master/src/main/java/org/sdet/junit/extension/TestBase.java)派生的就具备了失败重试的能力了。

```java
@Override
protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);
    if (method.getAnnotation(Ignore.class) != null) {
        notifier.fireTestIgnored(description);
    } else {
//    	  runLeaf(methodBlock(method), description, notifier);
		    	Statement statement = methodBlock(method);
		    	int retry = 1;
					// 获取Retry的次数
		    	for (TestRule rule: classRules()) {
			    		if (Retry.class.isInstance(rule)) {
			    			retry = ((Retry) rule).getCount();
			    	  }
	        }
		    	for (int i = 0; i < retry; i++) {
			    		boolean result = myRunLeaf(statement, description, notifier);
			    		if (result) {
			    				break;
			    		}
					}
    }
}

private boolean myRunLeaf(Statement statement, Description description, RunNotifier notifier) {
		boolean result = false;
		EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
	  eachNotifier.fireTestStarted();
	  try {
	      statement.evaluate();
	      result = true;
	  } catch (AssumptionViolatedException e) {
	      eachNotifier.addFailedAssumption(e);
	  } catch (Throwable e) {
	      eachNotifier.addFailure(e);
	  } finally {
	      eachNotifier.fireTestFinished();
	  }
		return result; 	
}
```

### 报告定制 - [CustomRunListener](https://github.com/yanjian824/junit-extension/blob/master/src/main/java/org/sdet/junit/extension/report/CustomRunListener.java)

理解下面5个回调函数
- testRunStarted 测试套开始执行
- testStarted 单个测试用例开始执行
- testFailure 单个测试用例失败时
- testFinished 单个测试用例结束时
- testRunFinished 测试套结束

```
{
  "name": "org.sdet.junit.MyRunner",
  "start": 1444790830394,
  "end": 1444790830411,
  "result": {
    "org.sdet.junit.HelloJUnitExtension.shouldFail": [
      {
        "start": 1444790830400,
        "end": 1444790830408,
        "pass": false,
        "comment": "运行失败的测试用例",
        "error": "java.lang.AssertionError: expected:\u003c1\u003e but was:\u003c0\u003e\r\n\tat org.junit.Assert.fail(Assert.java:88)\r\n\tat org.junit.Assert.failNotEquals(Assert.java:743)\r\n\tat org.junit.Assert.assertEquals(Assert.java:118)\r\n\tat org.junit.Assert.assertEquals(Assert.java:555)\r\n\tat org.junit.Assert.assertEquals(Assert.java:542)\r\n\tat org.sdet.junit.HelloJUnitExtension.shouldFail(HelloJUnitExtension.java:20)\r\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)\r\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\r\n\tat java.lang.reflect.Method.invoke(Method.java:606)\r\n\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)\r\n\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)\r\n\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:49)\r\n\tat org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)\r\n\tat org.junit.runners.BlockJUnit4ClassRunner.myRunLeaf(BlockJUnit4ClassRunner.java:96)\r\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:82)\r\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:1)\r\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)\r\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)\r\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)\r\n\tat org.junit.runners.ParentRunner.access$0(ParentRunner.java:234)\r\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)\r\n\tat org.sdet.junit.extension.rule.Retry$1.evaluate(Retry.java:28)\r\n\tat org.junit.rules.RunRules.evaluate(RunRules.java:20)\r\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:309)\r\n\tat org.junit.runners.Suite.runChild(Suite.java:127)\r\n\tat org.junit.runners.Suite.runChild(Suite.java:1)\r\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)\r\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)\r\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)\r\n\tat org.junit.runners.ParentRunner.access$0(ParentRunner.java:234)\r\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)\r\n\tat org.sdet.junit.extension.CustomSuite.run(CustomSuite.java:39)\r\n\tat org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50)\r\n\tat org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)\r\n\tat org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:459)\r\n\tat org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:675)\r\n\tat org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:382)\r\n\tat org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:192)\r\n"
      },
      {
        "start": 1444790830408,
        "end": 1444790830410,
        "pass": false,
        "comment": "运行失败的测试用例",
        "error": "java.lang.AssertionError: expected:\u003c1\u003e but was:\u003c0\u003e\r\n\tat org.junit.Assert.fail(Assert.java:88)\r\n\tat org.junit.Assert.failNotEquals(Assert.java:743)\r\n\tat org.junit.Assert.assertEquals(Assert.java:118)\r\n\tat org.junit.Assert.assertEquals(Assert.java:555)\r\n\tat org.junit.Assert.assertEquals(Assert.java:542)\r\n\tat org.sdet.junit.HelloJUnitExtension.shouldFail(HelloJUnitExtension.java:20)\r\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)\r\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\r\n\tat java.lang.reflect.Method.invoke(Method.java:606)\r\n\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)\r\n\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)\r\n\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:49)\r\n\tat org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)\r\n\tat org.junit.runners.BlockJUnit4ClassRunner.myRunLeaf(BlockJUnit4ClassRunner.java:96)\r\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:82)\r\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:1)\r\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)\r\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)\r\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)\r\n\tat org.junit.runners.ParentRunner.access$0(ParentRunner.java:234)\r\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)\r\n\tat org.sdet.junit.extension.rule.Retry$1.evaluate(Retry.java:28)\r\n\tat org.junit.rules.RunRules.evaluate(RunRules.java:20)\r\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:309)\r\n\tat org.junit.runners.Suite.runChild(Suite.java:127)\r\n\tat org.junit.runners.Suite.runChild(Suite.java:1)\r\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)\r\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)\r\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)\r\n\tat org.junit.runners.ParentRunner.access$0(ParentRunner.java:234)\r\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)\r\n\tat org.sdet.junit.extension.CustomSuite.run(CustomSuite.java:39)\r\n\tat org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50)\r\n\tat org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)\r\n\tat org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:459)\r\n\tat org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:675)\r\n\tat org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:382)\r\n\tat org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:192)\r\n"
      }
    ],
    "org.sdet.junit.HelloJUnitExtension.shouldPass": [
      {
        "start": 1444790830411,
        "end": 1444790830411,
        "pass": true,
        "comment": "运行成功的测试用例",
        "error": ""
      }
    ]
  }
}
```
