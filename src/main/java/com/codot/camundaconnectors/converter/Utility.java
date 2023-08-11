package com.codot.camundaconnectors.converter;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.repository.ProcessDefinition;

class Utility {

	public static String printLog(String msg, DelegateExecution execution) {
		RepositoryService repositoryService = execution.getProcessEngineServices().getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(execution.getProcessDefinitionId());
		String processInstanceId = execution.getProcessInstanceId();
		String source_line = processDefinition.getKey() + ":" + processDefinition.getVersion() + ":" + execution.getCurrentActivityName() + ":" + processInstanceId;
		return " [" + source_line + "]: " + msg;
	}

	public static String normalView(String src, int count){
		return src.length() <= count*2+4 ? src : src.substring(0, count) + "..." + src.substring(src.length() - count, src.length() - 1);
	}
}
