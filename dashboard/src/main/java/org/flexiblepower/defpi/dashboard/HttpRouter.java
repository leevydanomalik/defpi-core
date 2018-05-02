package org.flexiblepower.defpi.dashboard;

import org.flexiblepower.defpi.dashboard.gateway.http.proto.Gateway_httpProto.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRouter implements HttpHandler {

    private final static Logger LOG = LoggerFactory.getLogger(HttpRouter.class);

    private final HttpStaticContentHandler staticContentHandler;
    private final FullWidgetAndPageManager fullWidgetManager;

    public HttpRouter(final FullWidgetAndPageManager fullWidgetManager) {
        this.fullWidgetManager = fullWidgetManager;
        this.staticContentHandler = new HttpStaticContentHandler();
    }

    @Override
    public void handle(final HttpTask httpTask) {
        HttpRouter.LOG.info(httpTask.getRequest().getMethod() + ": " + httpTask.getRequest().getUri());

        // Rewrite?
        if (httpTask.getPath().equals("/")) {
            httpTask.respond(HTTPResponse.newBuilder()
                    .setId(httpTask.getRequest().getId())
                    .putHeaders("Location", "/dashboard/index.html")
                    .setStatus(301)
                    .build());
            return;
        }

        // Dynamic?
        this.fullWidgetManager.handle(new HttpTask(httpTask.getRequest(), (httpTask1, response) -> {
            // If the dynamic handler could not handle the request, try to serve static
            // content
            if (response.getStatus() == 404) {
                HttpRouter.this.staticContentHandler.handle(httpTask1.getOriginalTask());
            } else {
                // Just answer
                httpTask1.getOriginalTask().respond(response);
            }
        }, httpTask));
    }

}
