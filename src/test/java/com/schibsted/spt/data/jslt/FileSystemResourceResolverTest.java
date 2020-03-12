package com.schibsted.spt.data.jslt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.schibsted.spt.data.jslt.impl.FileSystemResourceResolver;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FileSystemResourceResolverTest {
  private static final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testResolveImportsFromFilesystem() throws IOException {
    FileSystemResourceResolver resolver = new FileSystemResourceResolver();
    Expression e = parse("./src/test/resources/import-from-fs/working1.jslt", resolver);
    assertEquals(
      readResource("import-from-fs/working1_expected_result.json"),
      mapper.writerWithDefaultPrettyPrinter().writeValueAsString(e.apply(mapper.readTree("{}"))));
  }

  @Test
  public void testResolveImportsFromFilesystemWithExplicitRootPath() throws IOException {
    FileSystemResourceResolver resolver =
      new FileSystemResourceResolver(new File("src/test/resources/import-from-fs"));
    Expression e = parse("./src/test/resources/import-from-fs/working2.jslt", resolver);
    assertEquals(
      readResource("import-from-fs/working1_expected_result.json"),
      mapper.writerWithDefaultPrettyPrinter().writeValueAsString(e.apply(mapper.readTree("{}"))));
  }

  @Test
  public void testResolveImportsFromFilesystemNotWorking() throws IOException {
    FileSystemResourceResolver resolver = new FileSystemResourceResolver();
    try {
      parse("./src/test/resources/import-from-fs/wrong_relative_path.jslt", resolver);
      fail("Expected " + JsltException.class.getSimpleName());
    } catch (final JsltException e) {
      assertEquals(FileNotFoundException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testResolveImportsFromFilesystemWithEncoding() throws IOException {
    FileSystemResourceResolver resolver = new FileSystemResourceResolver(
      new File("./src/test/resources"), StandardCharsets.ISO_8859_1
    );
    Expression e = parse("./src/test/resources/character-encoding-master.jslt", resolver);

    JsonNode result = e.apply(NullNode.instance);
    assertEquals("Hei på deg", result.asText());
  }

  private Expression parse(String resource, ResourceResolver resolver) throws IOException {
    return new Parser(
        new FileReader(new File(resource))
      )
      .withResourceResolver(resolver)
      .compile();
  }

  private String readResource(final String path) throws IOException {
    try {
      return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                                                     .getResource(path).toURI())), StandardCharsets.UTF_8);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }

}
