package org.flexiblepower.defpi.dashboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.flexiblepower.defpi.dashboard.gateway.http.proto.Gateway_httpProto.HTTPRequest.Method;
import org.flexiblepower.defpi.dashboard.gateway.http.proto.Gateway_httpProto.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

public class HttpStaticContentHandler implements HttpHandler {

    private final static Logger LOG = LoggerFactory.getLogger(HttpStaticContentHandler.class);

    @Override
    public void handle(final HttpTask httpTask) {
        final String filename = "/static/" + httpTask.getPath();
        final File file = new File(filename);
        if (!file.exists() || !file.isFile() || filename.contains("..")) {
            HttpUtils.notFound(httpTask);
            return;
        }
        try {
            if (!httpTask.getRequest().getMethod().equals(Method.GET)) {
                HttpUtils.badRequest(httpTask,
                        "Method" + httpTask.getRequest().getMethod().toString() + " not allowed on this location");
                return;
            }

            try (final FileInputStream inputStream = new FileInputStream(file)) {
                final ByteString body = ByteString.copyFrom(IOUtils.toByteArray(inputStream));
                httpTask.respond(HTTPResponse.newBuilder()
                        .setId(httpTask.getRequest().getId())
                        .setStatus(200)
                        .putHeaders(HttpUtils.CONTENT_TYPE, HttpUtils.getContentType(filename))
                        .setBody(body)
                        .build());
            }
        } catch (final IOException e) {
            HttpStaticContentHandler.LOG.error("Could not serve static content " + httpTask.getRequest().getUri(), e);
            HttpUtils.internalError(httpTask);
        }
    }

}
