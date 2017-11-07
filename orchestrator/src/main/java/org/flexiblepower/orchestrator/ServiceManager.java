/**
 * File ServiceManager.java
 *
 * Copyright 2017 FAN
 */
package org.flexiblepower.orchestrator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.flexiblepower.connectors.RegistryConnector;
import org.flexiblepower.exceptions.RepositoryNotFoundException;
import org.flexiblepower.exceptions.ServiceNotFoundException;
import org.flexiblepower.model.Interface;
import org.flexiblepower.model.Service;

/**
 * ServiceManager
 *
 * @version 0.1
 * @since May 22, 2017
 */
@SuppressWarnings("static-method")
public class ServiceManager {

    public final static String SERVICE_REPOSITORY = "services";

    private static ServiceManager instance = null;

    private ServiceManager() {
    }

    public synchronized static ServiceManager getInstance() {
        if (ServiceManager.instance == null) {
            ServiceManager.instance = new ServiceManager();
        }
        return ServiceManager.instance;
    }

    // public List<String> listRepositories() {
    // return RegistryConnector.getInstance().listRepositories();
    // }

    public Service getService(final String id) throws ServiceNotFoundException {
        return RegistryConnector.getInstance().getService(ServiceManager.SERVICE_REPOSITORY, id);
    }

    public List<Service> listServices() {
        try {
            return RegistryConnector.getInstance().listServices(ServiceManager.SERVICE_REPOSITORY);
        } catch (final RepositoryNotFoundException e) {
            // Can't happen
            return null;
        }
    }

    /**
     * @return
     */
    public List<Interface> listInterfaces() {
        final List<Interface> result = new LinkedList<>();
        for (final Service s : this.listServices()) {
            result.addAll(s.getInterfaces());
        }
        return result;
    }

    public Interface getInterfaceById(final String id) {
        for (final Interface i : this.listInterfaces()) {
            if (i.getId().equals(id)) {
                return i;
            }
        }
        return null;
    }

    public List<Service> getServicesThatCanConnectWith(final Interface intface) {
        final List<Service> result = new ArrayList<>();
        for (final Service s : this.listServices()) {
            for (final Interface otherIntf : s.getInterfaces()) {
                if (intface.isCompatibleWith(otherIntf)) {
                    result.add(s);
                    break; // go to the next service
                }
            }
        }
        return result;
    }

}
