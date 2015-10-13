package org.sdet.junit.extension.report;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.google.gson.GsonBuilder;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class CustomRunListener extends RunListener {

    private String name;

    private long start;

    private long end;

    private HashMap<String, ArrayList<ExecutionInfo>> result = new HashMap<String, ArrayList<ExecutionInfo>>();
    
    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);
        
        name = description.getDisplayName();
        start = System.currentTimeMillis();
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result);
        
        end = System.currentTimeMillis();
        File file = new File(String.format("%s_%s.log", name, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(start))));
        FileOutputStream out = new FileOutputStream(file);
        byte[] bytes = new GsonBuilder().setPrettyPrinting().create().toJson(this).getBytes();
        out.write(bytes);
        out.flush();
        out.close();
    }

    @Override
    public void testStarted(Description description) throws Exception {
        super.testStarted(description);
        
        String key = String.format("%s.%s", description.getTestClass().getName(), description.getMethodName());
        if (!result.containsKey(key)) {
        	ArrayList<ExecutionInfo> executions = new ArrayList<ExecutionInfo>();
        	result.put(key, executions);
        }
        ArrayList<ExecutionInfo> infoList = result.get(key);
    	ExecutionInfo info = new ExecutionInfo();
    	info.setStart(System.currentTimeMillis());
    	info.setPass(true);
    	info.setError("");
    	infoList.add(info);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);
        
        String key = String.format("%s.%s", description.getTestClass().getName(), description.getMethodName());
        ArrayList<ExecutionInfo> infoList = result.get(key);
        ExecutionInfo info = (ExecutionInfo) infoList.get(infoList.size() - 1);
        info.setEnd(System.currentTimeMillis());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        
        Description description = failure.getDescription();
        String key = String.format("%s.%s", description.getTestClass().getName(), description.getMethodName());
        ArrayList<ExecutionInfo> infoList = result.get(key);
        ExecutionInfo info = (ExecutionInfo) infoList.get(infoList.size() - 1);
        info.setEnd(System.currentTimeMillis());
        info.setPass(false);
        info.setError(failure.getTrace());
    }

}