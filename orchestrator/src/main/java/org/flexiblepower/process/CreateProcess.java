/**
 * File CreateProcess.java
 *
 * Copyright 2017 TNO
 */
package org.flexiblepower.process;

import org.flexiblepower.connectors.DockerConnector;
import org.flexiblepower.connectors.MongoDbConnector;
import org.flexiblepower.connectors.ProcessConnector;
import org.flexiblepower.model.Process;
import org.flexiblepower.model.Process.ProcessState;
import org.flexiblepower.orchestrator.pendingchange.PendingChange;
import org.flexiblepower.orchestrator.pendingchange.PendingChangeManager;
import org.mongodb.morphia.annotations.Entity;

import lombok.extern.slf4j.Slf4j;

/**
 * CreateProcess
 *
 * @author wilco
 * @version 0.1
 * @since Aug 9, 2017
 */
public class CreateProcess {

    @Entity("PendingChange")
    public static class CreateDockerService extends PendingChange {

        private Process process;

        public CreateDockerService() {
        }

        public CreateDockerService(final Process process) {
            super(process.getUserId());
            this.process = process;
        }

        @Override
        public String description() {
            return "Create Docker Service for process " + this.process.getId();
        }

        @Override
        public Result execute() {
            // Create docker service
            final String dockerId = DockerConnector.getInstance().newProcess(this.process);
            if (dockerId == null) {
                return Result.FAILED_TEMPORARY;
            }

            // Update database
            this.process.setState(ProcessState.INITIALIZING);
            this.process.setDockerId(dockerId);
            MongoDbConnector.getInstance().save(this.process);

            // Start next PendingChange
            PendingChangeManager.getInstance().submit(new SendConfiguration(this.process));

            // Report
            return Result.SUCCESS;
        }

    }

    @Slf4j
    @Entity("PendingChange")
    public static class SendConfiguration extends PendingChange {

        private Process process;

        public SendConfiguration() {
        }

        public SendConfiguration(final Process process) {
            super(process.getUserId());
            this.process = process;
        }

        @Override
        public String description() {
            return "Initializing process " + this.process.getId();
        }

        @Override
        public Result execute() {
            SendConfiguration.log.info("Going to configure process " + this.process.getId());
            if (ProcessConnector.getInstance().initNewProcess(this.process.getId())) {
                return Result.SUCCESS;
            } else {
                return Result.FAILED_TEMPORARY;
            }
        }

    }

}
