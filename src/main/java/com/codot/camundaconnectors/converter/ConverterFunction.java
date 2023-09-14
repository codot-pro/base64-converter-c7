package com.codot.camundaconnectors.converter;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

@Component
public class ConverterFunction implements JavaDelegate {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterFunction.class);

	Response response = new Response();

	@Override
	public void execute(DelegateExecution delegateExecution) {
		boolean debug = Boolean.parseBoolean((String) delegateExecution.getVariable("debugMode"));

		String operation = (String) delegateExecution.getVariable("operation");
		String io = (String) delegateExecution.getVariable("in/out");
		String source = delegateExecution.getVariable("src").toString();
		String fileName = (String) delegateExecution.getVariable("fileName");
		if (debug) startEvent(operation, io, source, delegateExecution);

		byte[] sourceAsBytes;
		try {
			if (io.equals("f-f") || io.equals("f-s")){
				File file = new File(System.getProperty("java.io.tmpdir"), source);
				sourceAsBytes = Files.readAllBytes(file.toPath());
			} else {
				sourceAsBytes = source.getBytes(StandardCharsets.UTF_8);
			}
		} catch (IOException e) {
			response.setStatusCode("400");
			response.setStatusMsg(e.getClass().getSimpleName() + ": " + e.getMessage());
			LOGGER.error(Utility.printLog(response.getStatusMsg(), delegateExecution));
			packRespond(delegateExecution);
			return;
		}

		try {
			switch (operation) {
				case "decode":
					sourceAsBytes = Base64.getDecoder().decode(sourceAsBytes);
					response.setStatusCode("200");
					response.setStatusMsg("OK");
					break;
				case "encode":
					sourceAsBytes = Base64.getEncoder().encode(sourceAsBytes);
					response.setStatusCode("200");
					response.setStatusMsg("OK");
					break;
				default:
					response.setStatusCode("400");
					response.setStatusMsg("Bad request. Invalid operation");
					LOGGER.error(Utility.printLog(response.getStatusMsg(), delegateExecution));
					packRespond(delegateExecution);
					return;
			}
		} catch (Exception e){
			response.setStatusCode("400");
			response.setStatusMsg(e.getClass().getSimpleName() + ": " + e.getMessage());
			LOGGER.error(Utility.printLog(response.getStatusMsg(), delegateExecution));
			packRespond(delegateExecution);
			return;
		}

		try {
			File file;
			BufferedOutputStream outputStream;
			switch (io){
				case "s-s":
                case "f-s":
                    response.setResponse(new String(sourceAsBytes, StandardCharsets.UTF_8));
					break;
                case "f-f":
                case "s-f":
                    file = new File(System.getProperty("java.io.tmpdir"), fileName);
					outputStream = new BufferedOutputStream(Files.newOutputStream(file.toPath()));
					for (byte item : sourceAsBytes)
						outputStream.write(item);
					outputStream.close();
					response.setResponse(file.getName());
					break;
                default:
					response.setStatusCode("400");
					response.setStatusMsg("Bad request. Invalid in/out");
					LOGGER.error(Utility.printLog(response.getStatusMsg(), delegateExecution));
			}
		} catch (IOException e) {
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

	private void startEvent(String operation, String io, String source, DelegateExecution delegateExecution){
		LOGGER.info(Utility.printLog(
				"{operation: " + operation + ", in/out: " + io + ", source: " + Utility.normalView(source, 40) + "}",
				delegateExecution));
	}

	private void endEvent(DelegateExecution delegateExecution){
		LOGGER.info(Utility.printLog("{statusCode: " + response.getStatusCode() + ", statusMsg: "+ response.getStatusMsg() +
				", response: " + Utility.normalView(response.getResponse().toString(), 40) + "}",
				delegateExecution));
	}
}
