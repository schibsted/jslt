package no.priv.garshol.jslt.playground;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PlaygroundServer {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static class JsltHandler extends AbstractLifeCycle implements Handler {
        private Server server;

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception {
            String target = request.getHttpURI().getPath();

            if (!target.equals("/jslt")) {
                return false;
            }

            String method = request.getMethod();

            if (method.equals("GET")) {
                // Set headers before writing content
                response.setStatus(HttpStatus.OK_200);
                response.getHeaders().add("Content-Type", "text/html");

                String INDEX_HTML = "lambda.html";
                try (InputStream stream = Parser.class.getClassLoader().getResourceAsStream(INDEX_HTML)) {
                    if (stream == null) {
                        response.setStatus(HttpStatus.NOT_FOUND_404);
                        callback.succeeded();
                        return true;
                    }

                    // Read the entire file into memory first
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = stream.read(buf)) != -1) {
                        baos.write(buf, 0, bytesRead);
                    }

                    // Write the content all at once
                    byte[] content = baos.toByteArray();
                    response.write(true, ByteBuffer.wrap(content), callback);
                } catch (IOException e) {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    callback.succeeded();
                    return true;
                }

                return true;
            }

            if (method.equals("POST")) {
                try {
                    // Use the Request.asInputStream() utility method to get an InputStream of the request
                    try (InputStream inputStream = Request.asInputStream(request)) {
                        // Read the request body into memory
                        ByteArrayOutputStream requestContent = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            requestContent.write(buffer, 0, bytesRead);
                        }
                        String requestBody = requestContent.toString(StandardCharsets.UTF_8);

                        JsonNode body = mapper.readTree(requestBody);
                        JsonNode input = mapper.readTree(body.get("json").asText());
                        String jslt = body.get("jslt").asText();

                        Expression template = Parser.compileString(jslt);
                        JsonNode output = template.apply(input);

                        // Set status before writing the response
                        response.setStatus(HttpStatus.OK_200);
                        response.getHeaders().add("Content-Type", "application/json");

                        // Write the response in one go
                        byte[] responseBytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(output);
                        response.write(true, ByteBuffer.wrap(responseBytes), callback);
                    }
                } catch (Exception e) {
                    response.setStatus(HttpStatus.BAD_REQUEST_400);
                    response.getHeaders().add("Content-Type", "text/plain");

                    byte[] errorBytes = e.toString().getBytes();
                    response.write(true, ByteBuffer.wrap(errorBytes), callback);
                }

                return true;
            }

            return false;
        }

        @Override
        public Server getServer() {
            return server;
        }

        @Override
        public void setServer(Server server) {
            this.server = server;
        }

        @Override
        public void destroy() {
            // Implementation for Destroyable interface
            // Clean up any resources if needed
        }
    }

    public static void main(String[] argv) throws Exception {
        Server server = new Server(Integer.parseInt(argv[0]));

        // Create a context handler for the /jslt path
        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        context.setHandler(new JsltHandler());

        server.setHandler(context);

        server.start();
        server.join();
    }
}