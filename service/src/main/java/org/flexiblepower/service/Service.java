/**
 * File Service.java
 *
 * Copyright 2017 TNO
 */
package org.flexiblepower.service;

import java.io.Serializable;
import java.util.Properties;

/**
 * Service
 *
 * @author coenvl
 * @version 0.1
 * @since Apr 24, 2017
 */
public interface Service {

    /**
     * This function is only called if this instance is the resumed version of an old process. It is called with the
     * serialized process state, which was returned from the {@link #suspend()} function. This function is called
     * *before* the {@link #init(Properties)}) function, if it is a resumed instance of an earlier process.
     *
     * @param state
     */
    public void resumeFrom(Serializable state);

    /**
     * This function is called after the constructor (or after the {@link #resumeFrom(Serializable)} if applicable),
     * when the configuration is first available. This method is only called once.
     *
     * @see #modify(Properties props)
     * @param props
     */
    public void init(Properties props);

    /**
     * This function is called when the configuration changes during runtime. It may be called multiple times.
     *
     * @see #init(Properties props)
     * @param props
     */
    public void modify(Properties props);

    /**
     * Marks that this process is about to be suspended. This means the object *will* be destroyed, and may be
     * subsequently created in another iteration. Any data has to be stored now.
     */
    public Serializable suspend();

    /**
     * Marks that this process is about to be terminated. This means the object *will* be destroyed.
     */
    public void terminate();

    MessageHandlerFactory getMessageHandlerFactory();

}
