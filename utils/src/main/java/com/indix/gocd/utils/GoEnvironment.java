package com.indix.gocd.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static com.indix.gocd.utils.Constants.*;

/**
 * Wrapper around Go's Environment variables
 */
public class GoEnvironment {
    private Map<String, String> environment = new HashMap<String, String>();

    public GoEnvironment() {
        this.environment.putAll(System.getenv());
    }

    public GoEnvironment putAll(Map<String, String> existing) {
        environment.putAll(existing);
        return this;
    }

    public String get(String name) {
        return environment.get(name);
    }

    public boolean has(String name) {
        return environment.containsKey(name) && isNotEmpty(get(name));
    }

    public boolean isAbsent(String name) {
        return !has(name);
    }

    public String traceBackUrl() {
        String serverUrl = get(GO_SERVER_DASHBOARD_URL);
        String pipelineName = get("GO_PIPELINE_NAME");
        String pipelineCounter = get("GO_PIPELINE_COUNTER");
        String stageName = get("GO_STAGE_NAME");
        String stageCounter = get("GO_STAGE_COUNTER");
        String jobName = get("GO_JOB_NAME");
        return String.format("%s/go/tab/build/detail/%s/%s/%s/%s/%s", serverUrl, pipelineName, pipelineCounter, stageName, stageCounter, jobName);
    }

    public String triggeredUser() {
        return get("GO_TRIGGER_USER");
    }

    /**
     * Version Format on S3 is <code>pipeline/stage/job/pipeline_counter.stage_counter</code>
     */
    public String artifactsLocationTemplate() {
    	
    	String s3Path = get(S3_PATH);
    	if (StringUtils.isNotBlank(s3Path)) {
    		return replaceEnv(s3Path);
    	}
    	
        String pipeline = get("GO_PIPELINE_NAME");
        String stageName = get("GO_STAGE_NAME");
        String jobName = get("GO_JOB_NAME");

        String pipelineCounter = get("GO_PIPELINE_COUNTER");
        String stageCounter = get("GO_STAGE_COUNTER");
        return artifactsLocationTemplate(pipeline, stageName, jobName, pipelineCounter, stageCounter);
    }

    public String artifactsLocationTemplate(String pipeline, String stageName, String jobName, String pipelineCounter, String stageCounter) {
        return String.format("%s/%s/%s/%s.%s", pipeline, stageName, jobName, pipelineCounter, stageCounter);
    }
    
    private String replaceEnv(String path) {
    	int idx = path.indexOf("${");
    	if (idx < 0) {
    		return path;
    	}
    	StringBuilder sb = new StringBuilder();
    	sb.append(path.substring(0, idx));
    	int lastIdx = 0;
    	while (idx >= 0) {
    		int endIdx = path.indexOf("}", idx);
    		
    		String envName = path.substring(idx+2, endIdx);
    		sb.append(get(envName));
    		
    		lastIdx = endIdx + 1;
    		idx = path.indexOf("${", lastIdx);
    		if (idx > 0) {
    			sb.append(path.substring(lastIdx, idx));
    		} else {
    			sb.append(path.substring(lastIdx));
    		}
    	}
    	return sb.toString();
    }
}
