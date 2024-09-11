package application.backend.parser.impl;

import application.backend.parser.JsonMessageParser;
import application.backend.parser.model.JsonMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonMessageParserImpl implements JsonMessageParser {
    private ObjectMapper mapper = new ObjectMapper();

    public JsonMessageParserImpl() {
        mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public String processJsonMessage(JsonMessage jsonMessage) throws JsonProcessingException {
        return mapper.writeValueAsString(jsonMessage);
    }

    @Override
    public JsonMessage processStringMessage(String jsonMessage) throws JsonProcessingException {
        return mapper.readValue(jsonMessage, JsonMessage.class);
    }
}
