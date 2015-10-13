package org.sdet.junit;

import org.junit.Test;
import org.sdet.junit.extension.TestBase;
import org.sdet.junit.extension.annotation.Author;
import org.sdet.junit.extension.annotation.High;
import org.sdet.junit.extension.annotation.Middle;
import org.sdet.junit.extension.annotation.Priority;

import static org.junit.Assert.assertEquals;

public class HelloJUnit extends TestBase {
	
//    @Test
//    @Author("michalejyan")
//    @Priority(Middle.class)
//    public void shouldTimeout() {
//    	try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }

    @Test
    @Author("michaelyan")
    @Priority(High.class)
    public void shouldPass() {
        assertEquals(1, 1);
    }

    @Test
    @Author("guojunshi")
    @Priority(Middle.class)
    public void shouldFail() {
        assertEquals(1, 0);
    }

}
