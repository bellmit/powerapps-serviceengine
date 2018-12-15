package com.profitera.services.business.rpm;

import org.apache.commons.logging.Log;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.rpm.AgentFailureException;
import com.profitera.rpm.NoSuchAgentException;
import com.profitera.rpm.RuleEngineException;
import com.profitera.rpm.expression.InvalidExpressionException;


abstract class ProfilerRunner {
	public TransferObject execute(Object o, Log log){
		try {
			runProfiler(o);
			return new TransferObject(Boolean.TRUE);
		} catch (NoSuchAgentException e) {
			return fail(log, "Error occurred loading profiling agent: " + e.getMessage());
		} catch (InvalidExpressionException e) {
			return fail(log, "Error occurred loading profiling rules: " + e.getMessage());
		} catch (RuleEngineException e) {
			return fail(log, "Error occurred loading profiling rules engine: " + e.getMessage());
		} catch (AgentFailureException e) {
			return fail(log, "One or more profiling agents failed: " + e.getMessage());
		}
	}
	
	public static TransferObject fail(Log log, String msg) {
		log.error(msg);
		return new TransferObject(TransferObject.ERROR, msg);
	}
	
	protected abstract void runProfiler(Object profiler)
		throws NoSuchAgentException, InvalidExpressionException, RuleEngineException, AgentFailureException;
}