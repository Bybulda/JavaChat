package backend.kafka;

public interface KafkaWriter {

    public void processMessage(byte[] message, String topic);

    public void close();
}
