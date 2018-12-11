package net.jcflorezr.kafka;

//import kafka.utils.VerifiableProperties;
//import org.apache.kafka.clients.producer.Partitioner;
//import org.apache.kafka.common.Cluster;

import java.util.Map;

public class MyKafkaPartitioner
//        implements Partitioner
{

//    @Override
//    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
//        int partition = 0;
//        String partitionKey = (String) key;
//        int offset = partitionKey.lastIndexOf('.');
//        if (offset > 0) {
////            partition = Integer.parseInt(partitionKey.substring(offset + 1)) % cluster.partitionsForTopic(topic).size();
//        }
//        return partition;
//    }

//    @Override
    public void close() {

    }

//    @Override
    public void configure(Map<String, ?> map) {

    }
}
