package dev.atlasreserve.events;

import dev.atlasreserve.persistence.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxDispatcher {
    private final OutboxRepository outbox;
    private final KafkaTemplate<String,String> kafka;
    private final String topic;
    public OutboxDispatcher(OutboxRepository outbox,KafkaTemplate<String,String> kafka,@Value("${app.outbox.topic:inventory.events}") String topic){
        this.outbox=outbox;this.kafka=kafka;this.topic=topic;
    }
    @Scheduled(fixedDelayString="${app.outbox.poll-ms:2000}")
    @Transactional
    public void publish() throws Exception {
        for(var event:outbox.pending(PageRequest.of(0,20))){
            kafka.send(topic,event.tenantId(),event.payload()).get(5,TimeUnit.SECONDS);
            event.markPublished();
        }
    }
}
