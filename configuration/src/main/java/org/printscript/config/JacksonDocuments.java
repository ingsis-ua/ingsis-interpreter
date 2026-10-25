package org.printscript.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

final class JacksonDocuments {

    private JacksonDocuments() {}

    static Map<String, Object> read(ObjectMapper mapper, InputStream source) throws IOException {
        JsonNode document = mapper.readTree(source);
        if (document == null || document.isMissingNode() || document.isNull()) {
            return Map.of();
        }
        if (!document.isObject()) {
            throw new IOException("la configuración tiene que ser un objeto de claves y valores");
        }
        return mapper.convertValue(document, new TypeReference<Map<String, Object>>() {});
    }
}
