package net.jcflorezr.kafka;

//import kafka.producer.KeyedMessage;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class MyKafkaProducerWithPartitioner {

//    private final KafkaProducer<String, String> producer;

    public MyKafkaProducerWithPartitioner() {
        Properties props = new Properties();

        // Set the broker list for requesting metadata to find the lead	broker
        props.put("bootstrap.servers", "localhost:9092, localhost:9093, localhost:9094");

        // This	specifies the serializer class for keys
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        props.put("partitioner.class", "net.jcflorezr.kafka.MyKafkaPartitioner");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // 1 means the producer	receives an	acknowledgment once the lead replica
        // has received	the	data. This option provides better durability as	the
        // client waits	until the server acknowledges the request as successful.
        props.put("request.required.acks", "1");

//        producer = new KafkaProducer<>(props);
    }

    public static void main(String[] args) {
        int argsCount = args.length;
        if (argsCount == 0 || argsCount == 1)
            throw new IllegalArgumentException("Please provide topic entityName and Message count	as arguments");
        // Topic entityName and the message count	to be published	is passed from the
        // command line http://freepdf-books.com
        String topic = args[0];
        String count = args[1];
        int messageCount = Integer.parseInt(count);
        System.out.println("Topic Name - " + topic);
        System.out.println("Message	Count - " + messageCount);
        MyKafkaProducerWithPartitioner simpleProducer = new MyKafkaProducerWithPartitioner();
        simpleProducer.publishMessage(topic, messageCount);

    }

    private void publishMessage(String topic, int messageCount) {
//        Random random = new Random();
//        for (int mCount = 0; mCount < messageCount; mCount++) {
//            String clientIp = "192.168.14." + random.nextInt(255);
//            String accessTime = new Date().toString();
//            String msg = accessTime + ", kafka.apache.org, " + clientIp;
//            System.out.println(msg);
//            // Creates a KeyedMessage instance
//            KeyedMessage<String, String> data = new KeyedMessage<>(topic, clientIp, msg);
//            // Publish the message
//            producer.send(new ProducerRecord<>(topic, clientIp, msg));
//        }
//        // Close producer connection with broker.
//        producer.close();
    }

}
