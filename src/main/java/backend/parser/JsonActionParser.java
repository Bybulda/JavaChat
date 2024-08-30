package backend.parser;

import backend.parser.model.JsonAction;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface JsonActionParser {
    public JsonAction processJsonAction(String json) throws JsonProcessingException;

    public String processStringAction(JsonAction json) throws JsonProcessingException;
}
