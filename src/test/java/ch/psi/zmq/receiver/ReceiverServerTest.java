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
package ch.psi.zmq.receiver;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.psi.zmq.receiver.model.ReceiverRequest;

public class ReceiverServerTest {

	@Before
	public void setUp() throws Exception {
	
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void terminateReceiverTest() throws InterruptedException{
		Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
		WebTarget target = client.target("http://emac:8090");
		
		ReceiverRequest request = new ReceiverRequest();
		request.setHostname("emac");
		request.setPort(8888);
		
		target = target.path("receiver").path("abcdefg");
		
		for(int i=0;i<1000;i++){
			System.out.println(i);
			target.path("abcdefg").request().accept(MediaType.APPLICATION_JSON).put(Entity.entity(request, MediaType.APPLICATION_JSON));
			Thread.sleep(1000);
			target.request().delete();
		}
		
	}
	
	
	
}
