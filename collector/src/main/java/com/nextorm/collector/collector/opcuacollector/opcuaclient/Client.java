package com.nextorm.collector.collector.opcuacollector.opcuaclient;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

import java.util.function.Predicate;

public interface Client {

	default String getEndpointUrl() {
		return "opc.tcp://192.168.122.91:4890/PRIAMUS";
	}

	default Predicate<EndpointDescription> endpointFilter() {
		return e -> true;
	}

	default SecurityPolicy getSecurityPolicy() {
		return SecurityPolicy.None;
	}

	default IdentityProvider getIdentityProvider() {
		//        return new UsernameProvider("user", "password");
		return new AnonymousProvider();
	}

	void run(
		ClientRunner clientRun,
		OpcUaClient client
	);
}
