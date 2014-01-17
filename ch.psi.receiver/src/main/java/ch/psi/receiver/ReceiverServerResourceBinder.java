package ch.psi.receiver;


import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.sse.SseBroadcaster;

public class ReceiverServerResourceBinder extends AbstractBinder {
	
	private ReceiverServerConfiguration config;
	
	public ReceiverServerResourceBinder(ReceiverServerConfiguration config){
		this.config = config;
	}

	@Override
	protected void configure() {
		bind(config).to(ReceiverServerConfiguration.class);
		bind(new ReceiverMap()).to(ReceiverMap.class);
		bind(new SseBroadcaster()).to(SseBroadcaster.class);
	}

}