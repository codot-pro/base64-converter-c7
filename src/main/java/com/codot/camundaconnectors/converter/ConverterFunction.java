package com.codot.camundaconnectors.converter;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class ConverterFunction implements JavaDelegate {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterFunction.class);

	String status_code = "";
	String status_msg = "";
	String response = "";

	@Override
	public void execute(DelegateExecution delegateExecution) {
		boolean debug = Boolean.parseBoolean((String) delegateExecution.getVariable("debugMode"));

		String operation = (String) delegateExecution.getVariable("operation");
		String source = (String) delegateExecution.getVariable("src");

		if (debug) startEvent(operation,	source, delegateExecution);

		try {

			switch (operation) {
				case "decode":
					response = new String(Base64.getDecoder().decode(source));
					status_code = "200";
					status_msg = "OK";
					break;
				case "encode":
					response = Base64.getEncoder().encodeToString(source.getBytes(StandardCharsets.UTF_8));
					status_code = "200";
					status_msg = "OK";
					break;
				default:
					status_code = "400";
					status_msg = "Bad request. Invalid operation";
					LOGGER.error(Utility.printLog(status_msg, delegateExecution));
					break;
			}
		} catch (Exception e){
			status_code = "400";
			status_msg = e.getClass().getSimpleName() +" : "+e.getMessage();
			LOGGER.error(Utility.printLog(status_msg, delegateExecution));
		}
		if (debug) endEvent(delegateExecution);
		packRespond(delegateExecution);
	}

	private void packRespond(DelegateExecution delegateExecution){
		delegateExecution.setVariable("status_code", status_code);
		delegateExecution.setVariable("status_msg", status_msg);
		delegateExecution.setVariable("response", response);
	}

	private void startEvent(String operation, String source, DelegateExecution delegateExecution){
		LOGGER.info(Utility.printLog(
				"{operation: " + operation + ", source: " + Utility.normalView(source, 23) + "}",
				delegateExecution));
	}

	private void endEvent(DelegateExecution delegateExecution){
		LOGGER.info(Utility.printLog("{statusCode: " + status_code + ", statusMsg: "+ status_msg +
				", response: " + Utility.normalView(response, 23) + "}",
				delegateExecution));
	}
}
