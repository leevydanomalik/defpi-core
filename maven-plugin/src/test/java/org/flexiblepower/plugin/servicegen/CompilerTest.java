/**
 * File CompilerTest.java
 *
 * Copyright 2017 TNO
 */
package org.flexiblepower.plugin.servicegen;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * CompilerTest
 *
 * @author coenvl
 * @version 0.1
 * @since Jun 27, 2017
 */
@SuppressWarnings("static-method")
public class CompilerTest {

    @Test
    public void testGetArchitectures() {
        System.out.println(ProtoCompiler.getOsName());
        System.out.println(ProtoCompiler.getArchitecture());
    }

    @Test
    public void testXjcCompiler() throws IOException {
        final XjcCompiler compiler = new XjcCompiler();
        compiler.setBasePackageName("org.flexiblepower.test.xml");
        compiler.compile(Paths.get("src/test/resources/books.xsd"), Paths.get("target/xjc-test-results"));
    }

    @Test
    public void testProtoCompiler() throws IOException {
        final ProtoCompiler compiler = new ProtoCompiler("3.3.0");
        compiler.compile(Paths.get("src/test/resources/echoProtocol.proto"), Paths.get("target/protoc-test-results"));
    }

}
