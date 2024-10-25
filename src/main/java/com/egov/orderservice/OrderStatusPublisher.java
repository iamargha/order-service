package com.egov.orderservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusPublisher.class);

    //@Autowired
    //private Sinks.Many<OrderEvent> orderSinks;

    private static final String TOPIC = "order-events";

    @Autowired //DEPENDENCY INJECTION PROMISE FULFILLED AT RUNTIME
    private KafkaTemplate<String, String> kafkaTemplate ;



    public void publishOrderEvent(OrderRequestDto orderRequestDto, OrderStatus orderStatus) {
        OrderEvent orderEvent=new OrderEvent(orderRequestDto,orderStatus);
        //orderSinks.tryEmitNext(orderEvent);
        ObjectMapper objectMapper = new ObjectMapper();
        String datum = null;
        try {
            datum = objectMapper.writeValueAsString(orderEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        logger.info(String.format("#### -> Producing Order message -> %s", datum));
        this.kafkaTemplate.send(TOPIC,"order-key-1"+orderEvent.getEventId(), datum);
    }
}
