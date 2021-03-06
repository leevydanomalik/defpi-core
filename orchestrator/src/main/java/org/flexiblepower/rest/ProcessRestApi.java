/*-
 * #%L
 * dEF-Pi REST Orchestrator
 * %%
 * Copyright (C) 2017 - 2018 Flexible Power Alliance Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.flexiblepower.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.bson.types.ObjectId;
import org.flexiblepower.api.ProcessApi;
import org.flexiblepower.connectors.MongoDbConnector;
import org.flexiblepower.exceptions.ApiException;
import org.flexiblepower.exceptions.AuthorizationException;
import org.flexiblepower.exceptions.InvalidObjectIdException;
import org.flexiblepower.exceptions.ProcessNotFoundException;
import org.flexiblepower.exceptions.ServiceNotFoundException;
import org.flexiblepower.model.Interface;
import org.flexiblepower.model.InterfaceVersion;
import org.flexiblepower.model.Process;
import org.flexiblepower.model.Service;
import org.flexiblepower.orchestrator.ServiceManager;
import org.flexiblepower.orchestrator.UserManager;
import org.flexiblepower.process.ProcessManager;

import lombok.extern.slf4j.Slf4j;

/**
 * ProcessRestApi
 *
 * @version 0.1
 * @since Mar 30, 2017
 */
@Slf4j
public class ProcessRestApi extends BaseApi implements ProcessApi {

    private static final Map<String, Function<Process, Comparable<?>>> SORT_MAP = new HashMap<>();
    static {
        ProcessRestApi.SORT_MAP.put("name", Process::getName);
        ProcessRestApi.SORT_MAP.put("id", Process::getId);
        ProcessRestApi.SORT_MAP.put("serviceId", Process::getServiceId);
        ProcessRestApi.SORT_MAP.put("state", Process::getState);
        ProcessRestApi.SORT_MAP.put("userId", p -> UserManager.getInstance().getUser(p.getUserId()).getUsername());
    }

    /**
     * Create the REST API with the headers from the HTTP request (will be injected by the HTTP server)
     *
     * @param httpHeaders The headers from the HTTP request for authorization
     */
    protected ProcessRestApi(@Context final HttpHeaders httpHeaders) {
        super(httpHeaders);
    }

    @Override
    public List<Process> listProcesses(final int page,
            final int perPage,
            final String sortDir,
            final String sortField,
            final String filters) throws AuthorizationException {
        if ((page < 0) || (perPage < 0)) {
            this.addTotalCount(0);
            return Collections.emptyList();
        }

        List<Process> processes;
        if (this.sessionUser == null) {
            throw new AuthorizationException();
        } else if (this.sessionUser.isAdmin()) {
            processes = ProcessManager.getInstance().listProcesses();
        } else {
            processes = ProcessManager.getInstance().listProcessesForUser(this.sessionUser);
        }

        final Map<String, Object> filter = MongoDbConnector.parseFilters(filters);
        RestUtils.filterMultiContent(processes, Process::getId, filter, "ids[]");
        RestUtils.filterContent(processes, Process::getName, filter, "name");
        RestUtils.filterContent(processes, Process::getUserId, filter, "userId");

        if (filter.containsKey("hashpair") && filter.get("hashpair").toString().contains(";")) {
            // Select processes with specific hashpairs
            final String[] split = filter.get("hashpair").toString().split(";");
            final Iterator<Process> it = processes.iterator();
            while (it.hasNext()) {
                final Process p = it.next();
                boolean matches = false;
                try {
                    final Service s = ServiceManager.getInstance().getService(p.getServiceId());
                    outerloop: for (final Interface itfs : s.getInterfaces()) {
                        for (final InterfaceVersion itfsv : itfs.getInterfaceVersions()) {
                            if (itfsv.getSendsHash().equals(split[0]) && itfsv.getReceivesHash().equals(split[1])) {
                                matches = true;
                                break outerloop;
                            }
                        }
                    }
                } catch (final ServiceNotFoundException e) {
                    // ignore
                }
                if (!matches) {
                    it.remove();
                }
            }
        }

        // Now do the sorting
        RestUtils.orderContent(processes, ProcessRestApi.SORT_MAP.get(sortField), sortDir);
        this.addTotalCount(processes.size());

        // And finally pagination
        return RestUtils.paginate(processes, page, perPage);
    }

    @Override
    public Process getProcess(final String id) throws ProcessNotFoundException,
            InvalidObjectIdException,
            AuthorizationException {
        final ObjectId oid = MongoDbConnector.stringToObjectId(id);
        final Process ret = ProcessManager.getInstance().getProcess(oid);

        // If not, use the logged in user
        this.assertUserIsAdminOrEquals(ret.getUserId());
        return ret;
    }

    @Override
    public Process newProcess(final Process process) throws AuthorizationException {
        this.assertUserIsAdminOrEquals(process.getUserId());

        ProcessRestApi.log.info("Creating new process {}", process);
        return ProcessManager.getInstance().createProcess(process);
    }

    @Override
    public Process updateProcess(final String id, final Process process) throws AuthorizationException,
            InvalidObjectIdException,
            ProcessNotFoundException {
        // Immediately do all relevant checks...
        final Process currentProcess = this.getProcess(id);

        if (!currentProcess.getId().equals(process.getId())) {
            throw new ApiException(Status.FORBIDDEN, "Cannot change the ID of a process");
        }

        ProcessRestApi.log.info("Updating process {}", process);
        ProcessManager.getInstance().updateProcess(process);
        return process;
    }

    @Override
    public void removeProcess(final String id) throws ProcessNotFoundException,
            InvalidObjectIdException,
            AuthorizationException {
        // Immediately do all relevant checks...
        final Process process = this.getProcess(id);

        ProcessRestApi.log.info("Removing process {}", process);
        ProcessManager.getInstance().deleteProcess(process);
    }

    @Override
    public void triggerProcessConfig(final String id) throws ProcessNotFoundException,
            InvalidObjectIdException,
            AuthorizationException {
        final ObjectId oid = MongoDbConnector.stringToObjectId(id);
        final Process referencedProcess = ProcessManager.getInstance().getProcess(oid);

        // See if we can do token-based authentication. This is the most common way to use this function
        final Process authorizedProcess = this.getTokenProcess();
        if ((authorizedProcess != null) && authorizedProcess.getId().equals(referencedProcess.getId())) {
            ProcessManager.getInstance().triggerConfig(referencedProcess);
            return;
        }

        // If that was not the case, see if and authorized person is manually trying to update the process
        this.assertUserIsAdminOrEquals(referencedProcess.getUserId());
        ProcessManager.getInstance().triggerConfig(referencedProcess);
    }
}
