/**
 * 
 * Copyright 2014 Paul Scherrer Institute. All rights reserved.
 * 
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This code is distributed in the hope that it will be useful, but without any
 * warranty; without even the implied warranty of merchantability or fitness for
 * a particular purpose. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package ch.psi.receiver;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.psi.receiver.model.ReceiverRequest;

/**
 * 
 */
public class Receiver {

	private final String basedir;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private Future<?> future;
	private FileReceiver receiver;
	
	public Receiver(String basedir){
		this.basedir = basedir;
	}
	
	public void receive(final ReceiverRequest request){
		receiver = new FileReceiver(request.getHostname(), request.getPort(), basedir);
		future = executor.submit(new Runnable() {
			@Override
			public void run() {
				if(request.getNumberOfImages()==null){
					receiver.receive();
				}
				else{
					receiver.receive(request.getNumberOfImages());
				}
			}
		});
	}
	
	public void stop(){
		receiver.terminate();
		executor.shutdownNow();
	}
	
	
	public void awaitTermination(Long timeout) throws InterruptedException, ExecutionException, TimeoutException{
		if(timeout==null){
			future.get();
		}
		else{
			future.get(timeout, TimeUnit.MICROSECONDS);
		}
	}
	
	public boolean isDone(){
		return receiver.isDone();
	}
	
	public int getMessagesReceived(){
		return receiver.getMessagesReceived();
	}
}
