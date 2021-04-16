
package no.priv.garshol.jslt.playground;

import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;

public class PlaygroundServer {
  private static ObjectMapper mapper = new ObjectMapper();
  private static String INDEX_HTML = "lambda.html";

  public static class JsltHandler extends AbstractHandler {
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
      if (!target.equals("/jslt"))
        return;

      if (request.getMethod().equals("GET")) {
        try (InputStream stream = Parser.class.getClassLoader().getResourceAsStream(INDEX_HTML)) {
          byte[] buf = new byte[16384];
          int bytes;
          while ((bytes = stream.read(buf)) != -1) {
            response.getOutputStream().write(buf, 0, bytes);
          }

          response.setStatus(HttpServletResponse.SC_OK);
          response.addHeader("Content-type", "text/html");
        } catch (IOException e) {
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        baseRequest.setHandled(true);
        return;
      }

      if (request.getMethod().equals("POST")) {
        try {
          JsonNode body = mapper.readTree(request.getReader());
          JsonNode input = mapper.readTree(body.get("json").asText());
          String jslt = body.get("jslt").asText();

          Expression template = Parser.compileString(jslt);
          JsonNode output = template.apply(input);
          response.setStatus(HttpServletResponse.SC_OK);

          response.getOutputStream().write(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(output));

        } catch (Exception e) {
          try (PrintStream ps = new PrintStream(response.getOutputStream())) {
            e.printStackTrace(ps);
          } catch (IOException e2) {
          }
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        baseRequest.setHandled(true);
      }
    }
  }

  public static void main(String[] argv) throws Exception {
    Server server = new Server(Integer.parseInt(argv[0]));
    HandlerList handlers = new HandlerList();
    handlers.addHandler(new JsltHandler());
    server.setHandler(handlers);

    server.start();
    server.join();
  }

}
