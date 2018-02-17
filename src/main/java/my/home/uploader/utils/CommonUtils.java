package my.home.uploader.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.config.undertow.BufferCache;
import org.wildfly.swarm.config.undertow.FilterConfiguration;
import org.wildfly.swarm.config.undertow.ServletContainer;
import org.wildfly.swarm.config.undertow.configuration.ResponseHeader;
import org.wildfly.swarm.config.undertow.server.HTTPListener;
import org.wildfly.swarm.config.undertow.server.host.FilterRef;
import org.wildfly.swarm.config.undertow.servlet_container.JSPSetting;
import org.wildfly.swarm.config.undertow.servlet_container.WebsocketsSetting;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * Константы и методы общие для и для клиента и для сервера.
 */
public final class CommonUtils {

    public static final String SETTINGS_FILE_PROPERY = "settings_file";
    public static final String SWARM_HTTP_PORT_SETTING = "httpport";
    public static final String SWARM_MANAGMENT_PORT_SETTING = "managementport";
    public static final String SWARM_HTTP_PORT_PROPERTY = "swarm.http.port";
    public static final String SWARM_MANAGMENT_PORT_PROPERTY = "swarm.management.http.port";

    public static final String SWARM_HTTP_PORT_DEFAULT = "8085";
    public static final String SWARM_MANAGMENT_PORT_DEFAULT = "9997";

    public static final String WAR_NAME = "ImageUploader.war";
    public static final String CONTEXT_ROOT = "uploader";
    public static final String SETTINGS_FILE = "./settings.yml";

    public static final String SWARM_LOG_SETTING = "swarmlog";

    public static final String MY_TOPIC = "/jms/topic/my-topic";

    public static final String SERVER_HEADER = "server-header";
    public static final String X_POWERED_BY_HEADER = "x-powered-by-header";
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
    public static final String ACCESS_CONTROL_MAX_AGE_HEADER = "Access-Control-Max-Age";

    public static final String SERVER_HEADER_VALUE = "WildFly/10";
    public static final String X_POWERED_BY_HEADER_VALUE = "Undertow/1";
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_VALUE = "*";
    public static final String ACCESS_CONTROL_ALLOW_METHODS_VALUE = "origin, content-type, accept, authorization, x-requested-with";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS_VALUE = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_VALUE = "true";
    public static final String ACCESS_CONTROL_MAX_AGE_VALUE = "1209600";

    public static UndertowFraction createUndertowFraction() {
        List<FilterRef> filterRefs = new ArrayList<>();
        filterRefs.add(new FilterRef(SERVER_HEADER));
        filterRefs.add(new FilterRef(X_POWERED_BY_HEADER));
        filterRefs.add(new FilterRef(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER));
        filterRefs.add(new FilterRef(ACCESS_CONTROL_ALLOW_METHODS_HEADER));
        filterRefs.add(new FilterRef(ACCESS_CONTROL_ALLOW_HEADERS_HEADER));
        filterRefs.add(new FilterRef(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER));
        filterRefs.add(new FilterRef(ACCESS_CONTROL_MAX_AGE_HEADER));
        List<ResponseHeader> headers = new ArrayList<>();
        headers.add(new ResponseHeader(SERVER_HEADER).headerName("Server")
                .headerValue(SERVER_HEADER_VALUE));
        headers.add(new ResponseHeader(X_POWERED_BY_HEADER).headerName("X-Powered-By")
                .headerValue(X_POWERED_BY_HEADER_VALUE));
        headers.add(
                new ResponseHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER).headerName(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER)
                        .headerValue(ACCESS_CONTROL_ALLOW_ORIGIN_VALUE));
        headers.add(
                new ResponseHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER).headerName(ACCESS_CONTROL_ALLOW_METHODS_HEADER)
                        .headerValue(ACCESS_CONTROL_ALLOW_METHODS_VALUE));
        headers.add(
                new ResponseHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER).headerName(ACCESS_CONTROL_ALLOW_HEADERS_HEADER)
                        .headerValue(ACCESS_CONTROL_ALLOW_HEADERS_VALUE));
        headers.add(new ResponseHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER)
                .headerName(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER)
                .headerValue(ACCESS_CONTROL_ALLOW_CREDENTIALS_VALUE));
        headers.add(new ResponseHeader(ACCESS_CONTROL_MAX_AGE_HEADER).headerName(ACCESS_CONTROL_MAX_AGE_HEADER)
                .headerValue(ACCESS_CONTROL_MAX_AGE_VALUE));

        return new UndertowFraction()
                .server("default-server", (server) -> {
                    server.httpListener(new HTTPListener("default")
                            .socketBinding("http")
                    );
                    server.host("default-host", (host) -> {
                        host.filterRefs(filterRefs);
                    });
                })
                .bufferCache(new BufferCache("default"))
                .servletContainer(new ServletContainer("default")
                        .websocketsSetting(new WebsocketsSetting())
                        .jspSetting(new JSPSetting())
                )
                .filterConfiguration(new FilterConfiguration().responseHeaders(headers));
    }

    public static LoggingFraction createLoggingFraction(String logFile) {
        return new LoggingFraction().fileHandler("FILE", f -> {
            Map<String, String> fileProps = new HashMap<>();
            fileProps.put("path", logFile);
            f.file(fileProps);
            f.level(Level.INFO);
            f.formatter("%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
        }).rootLogger(Level.INFO, "FILE");
    }

    public static MessagingFraction createMessageFraction() {
        return new MessagingFraction()
                .defaultServer(server -> {
                    server.jmsTopic("my-topic");
                    server.jmsQueue("my-queue");
                });
    }
}
