package backend.parser.impl;

import backend.parser.JsonActionParser;
import backend.parser.model.JsonAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonActionParserImpl implements JsonActionParser {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonAction processJsonAction(String json) throws JsonProcessingException {
        return mapper.readValue(json, JsonAction.class);
    }

    @Override
    public String processStringAction(JsonAction json) throws JsonProcessingException {
        return mapper.writeValueAsString(json);
    }
}
