package org.flexiblepower.plugin.servicegen;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.flexiblepower.plugin.servicegen.model.InterfaceDescription;
import org.flexiblepower.plugin.servicegen.model.ServiceDescription;
import org.flexiblepower.plugin.servicegen.model.Type;

public class Templates {

    public String servicePackage;
    public Map<String, String> hashes;

    public Templates() {
    }

    private String getTemplate(final String name) {
        String result = "";
        try {
            final URL url = this.getClass().getClassLoader().getResource(name + "Template.tpl");
            try (final Scanner scanner = new Scanner(url.openStream())) {
                result = scanner.useDelimiter("\\A").next();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String parseServiceImplementation(final Set<InterfaceDescription> interfaces) {
        String handlers = "";
        for (final InterfaceDescription i : interfaces) {
            final Map<String, String> handlerReplace = new HashMap<>();
            handlerReplace.put("name", i.getName());
            handlerReplace.put("subscribe", this.getHash(i.getSubscribe()));
            handlerReplace.put("publish", this.getHash(i.getPublish()));
            handlerReplace.put("package", this.servicePackage);
            handlers += Templates.replaceMap(this.getTemplate("Handler"), handlerReplace);
        }
        final Map<String, String> replace = new HashMap<>();
        replace.put("package", this.servicePackage);
        replace.put("handlers", handlers);
        return Templates.replaceMap(this.getTemplate("ServiceImplementation"), replace);
    }

    public String parseFactory(final InterfaceDescription i) {
        final HashMap<String, String> replace = new HashMap<>();
        replace.put("package", this.servicePackage);
        replace.put("name", i.getClassPrefix());
        replace.put("interfaceName", i.getName());
        replace.put("subscribeHandler", "null");
        replace.put("publishHandler", "null");
        if (i.getSubscribe() != null) {
            replace.put("subscribeHandler", "new " + i.getClassPrefix() + "SubscribeHandler(s)");
        }
        if (i.getPublish() != null) {
            replace.put("publishHandler", "new " + i.getClassPrefix() + "PublishHandler(s)");
        }
        return Templates.replaceMap(this.getTemplate("Factory"), replace);
    }

    public String parseSubscribeHandler(final InterfaceDescription i) {
        final HashMap<String, String> replace = new HashMap<>();
        replace.put("package", this.servicePackage);
        replace.put("name", i.getClassPrefix());
        String subscribeClasses = "";
        String imports = "";
        for (final Type type : i.getSubscribe()) {
            subscribeClasses += type.getType() + ".class, ";
            if (type.getDescriptorObject().getSource().equalsIgnoreCase("protobuf")) {
                replace.put("descriptorSource", "Protobuf");
                replace.put("type", "GeneratedMessage");
                imports += "import " + this.servicePackage + ".protobuf." + type.getDescriptor() + "Proto."
                        + type.getType() + ";\n";
            } else {
                replace.put("descriptorSource", "XSD");
                replace.put("type", "Object");
                imports = "import " + this.servicePackage + ".xml.*;\n";
            }
        }
        if (replace.get("descriptorSource").equals("Protobuf")) {
            imports += "import com.google.protobuf.GeneratedMessage;\n";
        }
        subscribeClasses = subscribeClasses.substring(0, subscribeClasses.length() - 2);
        replace.put("subscribeClasses", subscribeClasses);
        replace.put("imports", imports);
        return Templates.replaceMap(this.getTemplate("SubscribeHandler"), replace);
    }

    public String parsePublishHandler(final InterfaceDescription i) {
        final HashMap<String, String> replace = new HashMap<>();
        replace.put("package", this.servicePackage);
        replace.put("name", i.getClassPrefix());
        String publishClasses = "";
        String imports = "";
        for (final Type type : i.getPublish()) {
            publishClasses += type.getType() + ".class, ";
            if (type.getDescriptorObject().getSource().equalsIgnoreCase("protobuf")) {
                replace.put("descriptorSource", "Protobuf");
                replace.put("type", "GeneratedMessage");
                imports += "import " + this.servicePackage + ".protobuf." + type.getDescriptor() + "Proto."
                        + type.getType() + ";\n";
            } else {
                replace.put("descriptorSource", "XSD");
                replace.put("type", "Object");
                imports = "import " + this.servicePackage + ".xml.*;\n";
            }
        }
        if (replace.get("descriptorSource").equals("Protobuf")) {
            imports += "import com.google.protobuf.GeneratedMessage;\n";
        }
        publishClasses = publishClasses.substring(0, publishClasses.length() - 2);
        replace.put("publishClasses", publishClasses);
        replace.put("imports", imports);
        return Templates.replaceMap(this.getTemplate("PublishHandler"), replace);
    }

    public String parseDockerfile(final String platform, final ServiceDescription service) {
        final String interfaceTemplate = "{\"name\":\"%s\",\"cardinality\":%d,\"autoConnect\":%s,\"subscribeHash\":\"%s\",\"publishHash\":\"%s\"},";
        String interfaces = "[";
        for (final InterfaceDescription i : service.getInterfaces()) {
            interfaces += String.format(interfaceTemplate,
                    i.getName(),
                    i.getCardinality(),
                    i.isAutoConnect(),
                    this.getHash(i.getSubscribe()),
                    this.getHash(i.getPublish()));
        }
        interfaces = interfaces.substring(0, interfaces.length() - 1).replace("\"", "\\\"") + "]";
        String mappings = "[]";
        if (service.getMappings() != null) {
            mappings = "[";
            for (final String mapping : service.getMappings()) {
                mappings += "\"" + mapping + "\",";
            }
            mappings = mappings.substring(0, mappings.length() - 1).replace("\"", "\\\"") + "]";
        }

        final Map<String, String> replace = new HashMap<>();
        if (platform.equals("x86")) {
            replace.put("from", "java:alpine");
        } else {
            replace.put("from", "larmog/armhf-alpine-java:jdk-8u73");
        }
        if ((service.getPorts() != null) && !service.getPorts().isEmpty()) {
            replace.put("ports", "EXPOSE " + String.join(" ", service.getPorts()) + "\n");
            replace.put("portsLabel", String.join(", ", service.getPorts()));
        } else {
            replace.put("ports", "");
            replace.put("portsLabel", "");
        }
        replace.put("name", service.getName());
        replace.put("interfaces", interfaces);
        replace.put("mappings", mappings);
        return Templates.replaceMap(this.getTemplate("Dockerfile"), replace);
    }

    private static String replaceMap(final String template, final Map<String, String> replace) {
        String ret = template;
        for (final Entry<String, String> entry : replace.entrySet()) {
            if (entry.getValue() != null) {
                ret = ret.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        return ret;
    }

    private String getHash(final List<Type> keys) {
        final String descriptor = keys.get(0).getDescriptor();
        if (this.hashes.containsKey(descriptor)) {
            String baseHash = this.hashes.get(descriptor);
            for (final Type key : keys) {
                baseHash += ";" + key.getType();
            }
            return Templates.SHA256(baseHash);
        }
        return "";
    }

    public static String SHA256(final String body) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");

            md.update(body.getBytes("UTF-8"));
            final byte[] mdbytes = md.digest();

            final StringBuffer sb = new StringBuffer();
            for (final byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
