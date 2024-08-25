package backend.parser;

import backend.parser.model.JsonMessage;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface JsonMessageParser {

    public String processJsonMessage(JsonMessage jsonMessage) throws JsonProcessingException;

    public JsonMessage processStringMessage(String jsonMessage) throws JsonProcessingException;
}
