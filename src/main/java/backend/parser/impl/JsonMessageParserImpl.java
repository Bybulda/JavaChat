package backend.parser.impl;

import backend.parser.JsonMessageParser;
import backend.parser.model.JsonMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMessageParserImpl implements JsonMessageParser {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String processJsonMessage(JsonMessage jsonMessage) throws JsonProcessingException {
        return mapper.writeValueAsString(jsonMessage);
    }

    @Override
    public JsonMessage processStringMessage(String jsonMessage) throws JsonProcessingException {
        return mapper.readValue(jsonMessage, JsonMessage.class);
    }
}
