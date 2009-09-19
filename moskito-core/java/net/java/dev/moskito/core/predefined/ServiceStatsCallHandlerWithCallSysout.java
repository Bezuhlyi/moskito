/*
 * $Id$
 * 
 * This file is part of the MoSKito software project
 * that is hosted at http://moskito.dev.java.net.
 * 
 * All MoSKito files are distributed under MIT License:
 * 
 * Copyright (c) 2006 The MoSKito Project Team.
 * 
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), 
 * to deal in the Software without restriction, 
 * including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit 
 * persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice
 * shall be included in all copies 
 * or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY
 * OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */	
package net.java.dev.moskito.core.predefined;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import net.java.dev.moskito.core.dynamic.IOnDemandCallHandler;
import net.java.dev.moskito.core.producers.IStats;
import net.java.dev.moskito.core.usecase.running.ExistingRunningUseCase;
import net.java.dev.moskito.core.usecase.running.PathElement;
import net.java.dev.moskito.core.usecase.running.RunningUseCase;
import net.java.dev.moskito.core.usecase.running.RunningUseCaseContainer;

public class ServiceStatsCallHandlerWithCallSysout implements IOnDemandCallHandler{
	
	private AtomicLong callCounter = new AtomicLong(0);
	private static AtomicLong instanceCounter = new AtomicLong(0);
	private long instanceId;
	private static AtomicLong overallCallCounter = new AtomicLong(0);
	
	public ServiceStatsCallHandlerWithCallSysout(){
		instanceId = instanceCounter.incrementAndGet();
	}

	public Object invoke(Object target, Object[] args, Method method, Class<?> targetClass, Class<?>[] declaredExceptions, IStats _defaultStats, IStats _methodStats, String producerId) throws Throwable {
		
		String callId = overallCallCounter.incrementAndGet()+"-"+instanceId + "-"+callCounter.incrementAndGet();
		
		ServiceStats defaultStats = (ServiceStats)_defaultStats;
		ServiceStats methodStats = (ServiceStats)_methodStats;
		
		//now create debug info
		StringBuilder debugOutPreCall = new StringBuilder("--- MSK III ");
		try{
			debugOutPreCall.append(callId);
			debugOutPreCall.append(" --- Calling ");
			debugOutPreCall.append(targetClass.getSimpleName());
			debugOutPreCall.append('.');
			debugOutPreCall.append(method.getName());
			debugOutPreCall.append('(');
			if (args!=null){
				for (int i=0; i<args.length; i++){
					debugOutPreCall.append(args[i] == null ? "null" : args[i]);
					if (i<args.length-1)
						debugOutPreCall.append(", ");
				}
			}
			debugOutPreCall.append(')');
		}catch(Exception e){
			debugOutPreCall.append(" ERROR ").append(e.getMessage());
		}
		
		System.out.println(debugOutPreCall.toString());
		
		StringBuilder debugOutPostCall = new StringBuilder("--- MSK III ").append(callId).append(" --- Return ");
		
		defaultStats.addRequest();
		methodStats.addRequest();
		RunningUseCase aRunningUseCase = RunningUseCaseContainer.getCurrentRunningUseCase();
		PathElement currentElement = null;
		ExistingRunningUseCase runningUseCase = aRunningUseCase.useCaseRunning() ? 
				(ExistingRunningUseCase)aRunningUseCase : null; 
		if (runningUseCase !=null)
			currentElement = runningUseCase.startPathElement(new StringBuilder(producerId).append('.').append(method.getName()).toString());
		long startTime = System.nanoTime();
		try{
			Object ret = method.invoke(target, args);
			debugOutPostCall.append(ret);
			long exTime = System.nanoTime() - startTime;
			defaultStats.addExecutionTime(exTime);
			methodStats.addExecutionTime(exTime);
			return ret;
		}catch(InvocationTargetException e){
			defaultStats.notifyError();
			methodStats.notifyError();
			//System.out.println("exception of class: "+e.getCause()+" is thrown");
			if (currentElement!=null)
				currentElement.setAborted();
			debugOutPostCall.append("ERR (E) ").append(e.getCause().getMessage()).append(" ").append(e.getCause().toString());
			throw e.getCause();
		}catch(Throwable t){
			defaultStats.notifyError();
			methodStats.notifyError();
			if (currentElement!=null)
				currentElement.setAborted();
			debugOutPostCall.append("ERR (T) ").append(t.getMessage()).append(" ").append(t.toString());
			throw t;
		}finally{
			defaultStats.notifyRequestFinished();
			methodStats.notifyRequestFinished();
			System.out.println(debugOutPostCall.toString());
			if (currentElement!=null)
				currentElement.setDuration(System.currentTimeMillis()-startTime);
			if (runningUseCase !=null)
				runningUseCase.endPathElement();
		}
	}
} 