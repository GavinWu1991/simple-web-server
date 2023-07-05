package liteweb.http;

import liteweb.cache.Cache;
import liteweb.cache.ConditionalLRUCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Response {

    private static final Logger log = LogManager.getLogger(Response.class);

    public static final String VERSION = "HTTP/1.0";

    public static final Cache CACHE = new ConditionalLRUCache(10);

    private final List<String> headers = new ArrayList<>();

    private byte[] body;

    public List<String> getHeaders() {
        return new ArrayList<>(headers);
    }

    public Response(Request req) {

        switch (req.getMethod()) {
            case HEAD:
                fillHeaders(Status._200);
                break;
            case GET:
                try {
                    String uri = req.getUri();
                    File file = new File("." + uri);
                    if (file.isDirectory()) {
                        generateResponseForFolder(uri, file);
                    } else if (file.exists()) {
                        generateResponseForFile(uri, file);
                    } else {
                        log.info("File not found: {}", req.getUri());
                        fillHeaders(Status._404);
                        fillResponse(Status._404.toString());
                    }
                } catch (IOException | InterruptedException e) {
                    log.error("Response Error", e);
                    fillHeaders(Status._400);
                    fillResponse(Status._400.toString());
                }
                break;
            default:
                fillHeaders(Status._400);
                fillResponse(Status._400.toString());
        }

    }

    private void generateResponseForFile(String uri, File file) throws IOException, InterruptedException {
        fillHeaders(Status._200);
        setContentType(uri);

        // Try to read file content from Cache
        Optional<byte[]> optionalCachedValue = CACHE.getByteCache(uri);

        if (optionalCachedValue.isPresent()) {
            // Read file content from Cache
            fillResponse(optionalCachedValue.get());
            return;
        }

        // read file
        byte[] bytes = Files.readAllBytes(file.toPath());
        CACHE.putByteCache(uri, bytes);
        fillResponse(bytes);
    }


    private void generateResponseForFolder(String uri, File file) throws InterruptedException {
        fillHeaders(Status._200);

        headers.add(ContentType.of("HTML"));

        // Try to read folder content from Cache
        Optional<byte[]> optionalCachedValue = CACHE.getByteCache(uri);

        if (optionalCachedValue.isPresent()) {
            // Read file content from Cache
            fillResponse(optionalCachedValue.get());
            return;
        }

        StringBuilder result = new StringBuilder("<html><head><title>Index of ");
        result.append(uri);
        result.append("</title></head><body><h1>Index of ");
        result.append(uri);
        result.append("</h1><hr><pre>");

        File[] files = file.listFiles();
        assert files != null;
        for (File subFile : files) {
            result.append(" <a href=\"")
                    .append(subFile.getPath())
                    .append("\">")
                    .append(subFile.getPath())
                    .append("</a>\n");
        }
        result.append("<hr></pre></body></html>");
        CACHE.putByteCache(uri, result.toString().getBytes());
        fillResponse(result.toString());
    }

    private byte[] getBytes(File file) throws IOException {
        int length = (int) file.length();
        byte[] array = new byte[length];
        try (InputStream in = Files.newInputStream(file.toPath())) {
            int offset = 0;
            while (offset < length) {
                int count = in.read(array, offset, (length - offset));
                offset += count;
            }
        }
        return array;
    }

    private void fillHeaders(Status status) {
        headers.add(Response.VERSION + " " + status.toString());
        headers.add("Connection: close");
        headers.add("Server: simple-web-server");
    }

    private void fillResponse(String response) {
        body = response.getBytes();
    }

    private void fillResponse(byte[] response) {
        body = response;
    }

    public void write(OutputStream outputStream) throws IOException {
        try (DataOutputStream output = new DataOutputStream(outputStream)) {
            for (String header : headers) {
                output.writeBytes(header + "\r\n");
            }
            output.writeBytes("\r\n");
            if (body != null) {
                output.write(body);
            }
            output.writeBytes("\r\n");
            output.flush();
        }
    }

    private void setContentType(String uri) {
        try {
            String ext = uri.substring(uri.indexOf(".") + 1);
            headers.add(ContentType.of(ext));
        } catch (RuntimeException e) {
            log.error("ContentType not found:", e);
        }
    }
}
