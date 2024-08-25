package backend.kafka.writer;

public interface KafkaWriter {

    public void processMessage(byte[] message, String topic);

    public void close();
}
