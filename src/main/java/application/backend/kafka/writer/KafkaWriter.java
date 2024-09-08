package application.backend.kafka.writer;

public interface KafkaWriter {

    public void processMessage(String message, String topic);

    public void close();
}
