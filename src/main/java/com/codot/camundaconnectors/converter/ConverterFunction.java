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

	Response response = new Response();

	@Override
	public void execute(DelegateExecution delegateExecution) {
		boolean debug = Boolean.parseBoolean((String) delegateExecution.getVariable("debugMode"));

		String operation = (String) delegateExecution.getVariable("operation");
		String source = (String) delegateExecution.getVariable("src");

		if (debug) startEvent(operation, source, delegateExecution);

		try {

			switch (operation) {
				case "decode":
					response.setResponse(new String(Base64.getDecoder().decode(source)));
					response.setStatusCode("200");
					response.setStatusMsg("OK");
					break;
				case "encode":
					response.setResponse(Base64.getEncoder().encodeToString(source.getBytes(StandardCharsets.UTF_8)));
					response.setStatusCode("200");
					response.setStatusMsg("OK");
					break;
				default:
					response.setStatusCode("400");
					response.setStatusMsg("Bad request. Invalid operation");
					LOGGER.error(Utility.printLog(response.getStatusMsg(), delegateExecution));
					break;
			}
		} catch (Exception e){
			response.setStatusCode("400");
			response.setStatusMsg(e.getClass().getSimpleName() + ": " + e.getMessage());
			LOGGER.error(Utility.printLog(response.getStatusMsg(), delegateExecution));
		}
		if (debug) endEvent(delegateExecution);
		packRespond(delegateExecution);
	}

	private void packRespond(DelegateExecution delegateExecution){
		delegateExecution.setVariable("status_code", response.getStatusCode());
		delegateExecution.setVariable("status_msg", response.getStatusMsg());
		delegateExecution.setVariable("response", response.getResponse());
	}

	private void startEvent(String operation, String source, DelegateExecution delegateExecution){
		LOGGER.info(Utility.printLog(
				"{operation: " + operation + ", source: " + Utility.normalView(source, 23) + "}",
				delegateExecution));
	}

	private void endEvent(DelegateExecution delegateExecution){
		LOGGER.info(Utility.printLog("{statusCode: " + response.getStatusCode() + ", statusMsg: "+ response.getStatusMsg() +
				", response: " + Utility.normalView(response.getResponse(), 23) + "}",
				delegateExecution));
	}
}
