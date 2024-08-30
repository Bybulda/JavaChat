package backend.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public interface ConfigLoader {

    static Config load(){
        return ConfigFactory.parseResources("application.conf");
    }
}
