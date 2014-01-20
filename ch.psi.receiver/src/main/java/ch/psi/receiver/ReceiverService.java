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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;

import ch.psi.receiver.model.ReceiverRequest;

@Path("/")
public class ReceiverService {

	private static final Logger logger = Logger.getLogger(ReceiverService.class.getName());
	
	@Inject
	private ReceiverServerConfiguration configuration;
	
	@Inject
	private ReceiverMap receivers;
	
	@Inject
	private SseBroadcaster broadcaster;
	
	@GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public String getVersion(){
    	String version = getClass().getPackage().getImplementationVersion();
    	if(version==null){
    		version="0.0.0";
    	}
    	return version;
    }
	
	@PUT
	@Path("receiver/{trackingid}")
	public void stream(@PathParam("trackingid") final String trackingid, final ReceiverRequest request) throws IOException{

		logger.info("Add receiver: "+trackingid);
		
		cleanupReceivers(); // Workaround until we have a more reliable way of automatically removing receivers
		
		if(receivers.containsKey(trackingid)){
			logger.info(String.format("Stream %s already exists. Stopping existing stream and starting new one.",trackingid));
			terminate(trackingid);
		}
		
		final Receiver receiver = new Receiver(configuration.getBasedir());
		receiver.receive(request);
		receivers.put(trackingid, receiver);
		
		// Broadcast new stream list
		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.name("receiver")
            .mediaType(MediaType.APPLICATION_JSON_TYPE)
            .data(List.class, getReceivers())
            .build();
        broadcaster.broadcast(event);
	}
	
	@DELETE
	@Path("receiver/{trackingid}")
	public void terminate(@PathParam("trackingid") String trackingid){
		
		logger.info("Delete receiver: "+trackingid);
		
		cleanupReceivers(); // Workaround until we have a more reliable way of automatically removing receivers
		
		Receiver receiver = receivers.get(trackingid);
		if(receiver==null){
			throw new NotFoundException();
		}
		receiver.stop();
		receivers.remove(trackingid);
		
		// Broadcast new stream list
		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.name("receiver")
            .mediaType(MediaType.APPLICATION_JSON_TYPE)
            .data(List.class, getReceivers())
            .build();
        broadcaster.broadcast(event);
	}
	
	@GET
	@Path("receiver/{trackingid}")
	public int getStatus(@PathParam("trackingid") String trackingid) {
		Receiver receiver = receivers.get(trackingid);
		if(receiver==null){
			throw new NotFoundException();
		}
		return receiver.getMessagesReceived();
	}
	
	@GET
	@Path("receiver/{trackingid}/done")
	public void awaitTermination(@PathParam("trackingid") String trackingid) throws InterruptedException, ExecutionException, TimeoutException{
		Receiver receiver = receivers.get(trackingid);
		if(receiver==null){
			throw new NotFoundException();
		}
		receiver.awaitTermination(null);
		
		cleanupReceivers(); // Workaround until we have a more reliable way of automatically removing receivers
	}
	
	@GET
	@Path("receiver")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getReceivers(){
		cleanupReceivers();
		return(new ArrayList<>(receivers.keySet()));
	}
	
	@GET
    @Path("events")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput subscribe() {
        EventOutput eventOutput = new EventOutput();
        broadcaster.add(eventOutput);
        return eventOutput;
    }
	
	private void cleanupReceivers(){
		for(String key : receivers.keySet()){
			if(receivers.get(key).isDone()){
				receivers.remove(key);
				
				// Broadcast new stream list
				OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
				OutboundEvent event = eventBuilder.name("receiver")
					.mediaType(MediaType.APPLICATION_JSON_TYPE)
					.data(List.class, getReceivers())
					.build();
				broadcaster.broadcast(event);
			}
		}
		
		
	}
}
