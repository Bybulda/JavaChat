package application.backend.kafka.writer;

import application.backend.config.ConfigLoader;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
public class KafkaWriterImpl implements KafkaWriter, ConfigLoader {

    private final KafkaProducer<String, String> producer;

    public KafkaWriterImpl() {
        Config config = ConfigLoader.load();
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.bootstrap.Servers"));
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "104857600");
        props.put("auto.create.topics.enable", "true");
        producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
    }

    @Override
    public void processMessage(String message, String topic) {
        log.info("Sending message to: {}", topic);
        try {
            producer.send(new ProducerRecord<>(topic, message));
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    @Override
    public void close() {
        producer.close();
    }
}
