package org.flexiblepower.defpi.dashboardgateway.dashboard.http;

import javax.annotation.Generated;

import org.flexiblepower.defpi.dashboardgateway.dashboard.DashboardConnectionManagerImpl;
import org.flexiblepower.defpi.dashboardgateway.dashboard.http.proto.Dashboard_httpProto.HTTPResponse;
import org.flexiblepower.defpi.dashboardgateway.dashboard.http.proto.Dashboard_httpProto.HTTPRequest;
import org.flexiblepower.serializers.ProtobufMessageSerializer;
import org.flexiblepower.service.ConnectionHandler;
import org.flexiblepower.service.InterfaceInfo;

/**
 * Dashboard_httpConnectionHandler
 *
 * File generated by org.flexiblepower.create-service-maven-plugin. 
 * NOTE: This file will be overwritten when the codegen plugin is re-run; any user-made changes will be overwritten.
 * Template by TNO, 2017
 * 
 * @author wilco
 */
@Generated(value = "org.flexiblepower.plugin.servicegen", date = "Oct 23, 2017 7:55:29 PM")
@InterfaceInfo(name = "Dashboard", version = "HTTP", 
		receivesHash = "aafa92f5e8c919cc004f017d0c7706bf5e72594e656cf04cd67dd47b97cf7c6c", 
		sendsHash = "c46d5961b42774f80194e8308e4a1bec450881925f8d20a08a1f764acf22ed24",
		manager = DashboardConnectionManagerImpl.class,
		serializer = ProtobufMessageSerializer.class, 
		receiveTypes = { HTTPResponse.class }, sendTypes = { HTTPRequest.class })
public interface Dashboard_httpConnectionHandler extends ConnectionHandler {

	public void handleHTTPResponseMessage(HTTPResponse message);

}