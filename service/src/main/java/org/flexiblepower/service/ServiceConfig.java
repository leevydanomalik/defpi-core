/**
 * File ServiceConfig.java
 *
 * Copyright 2017 TNO
 */
package org.flexiblepower.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServiceConfig
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 24 aug. 2017
 */
public class ServiceConfig {

    @SuppressWarnings("unchecked")
    static <T> T generateConfig(final Class<T> clazz, final Map<String, String> rawValues) {
        return (T) Proxy
                .newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, new GeneratedConfigHandler(rawValues));
    }

    /**
     * GeneratedConfigHandler
     *
     * @author leeuwencjv
     * @version 0.1
     * @since 24 aug. 2017
     */
    private final static class GeneratedConfigHandler implements InvocationHandler {

        private static final Logger log = LoggerFactory.getLogger(GeneratedConfigHandler.class);

        private final Map<String, String> values;

        /**
         * @param rawValues
         */
        public GeneratedConfigHandler(final Map<String, String> rawValues) {
            this.values = rawValues;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method,
         * java.lang.Object[])
         */
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final String methodName = method.getName();

            if (!methodName.startsWith("get")) {
                throw new IllegalArgumentException("Can only invoke 'getters'");
            }

            // We assume camelCaps
            final String key = methodName.substring(3, 4).toLowerCase() + methodName.substring(4, methodName.length());

            String rawValue = "";
            if (!this.values.containsKey(key)) {
                if (method.isAnnotationPresent(DefaultValue.class)) {
                    rawValue = method.getAnnotation(DefaultValue.class).value();
                } else {
                    GeneratedConfigHandler.log.warn("No parameter found with name \"{}\", returning type default", key);
                }
            } else {
                rawValue = this.values.get(key);
            }

            if (method.getReturnType().equals(String.class)) {
                return rawValue;
            } else if (method.getReturnType().equals(boolean.class)) {
                return rawValue.isEmpty() ? false : Boolean.parseBoolean(rawValue);
            } else if (method.getReturnType().equals(byte.class)) {
                return rawValue.isEmpty() ? (byte) 0 : Byte.decode(rawValue);
            } else if (method.getReturnType().equals(char.class)) {
                return rawValue.isEmpty() ? (char) 0 : rawValue.isEmpty() ? (char) 0 : rawValue.charAt(0);
            } else if (method.getReturnType().equals(short.class)) {
                return rawValue.isEmpty() ? (short) 0 : Short.decode(rawValue);
            } else if (method.getReturnType().equals(int.class)) {
                return rawValue.isEmpty() ? 0 : Integer.decode(rawValue);
            } else if (method.getReturnType().equals(long.class)) {
                return rawValue.isEmpty() ? 0L : Long.decode(rawValue);
            } else if (method.getReturnType().equals(float.class)) {
                return rawValue.isEmpty() ? (float) 0.0 : Float.parseFloat(rawValue);
            } else if (method.getReturnType().equals(double.class)) {
                return rawValue.isEmpty() ? 0.0 : Double.parseDouble(rawValue);
            } else {
                throw new IllegalArgumentException(
                        "Unable to return parameter of type '" + method.getReturnType() + "'");
            }
        }

    }

}
