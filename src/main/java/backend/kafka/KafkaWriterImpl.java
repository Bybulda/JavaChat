package backend.kafka;

import backend.config.ConfigLoader;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
public class KafkaWriterImpl implements KafkaWriter, ConfigLoader {

    private final KafkaProducer<byte[], byte[]> producer;

    public KafkaWriterImpl() {
        Config config = ConfigLoader.load();
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.bootstrap.Servers"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, config.getString("kafka.client.id"));
        props.put("auto.create.topics.enable", "true");
        producer = new KafkaProducer<>(props, new ByteArraySerializer(), new ByteArraySerializer());
    }

    @Override
    public void processMessage(byte[] message, String topic) {
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
