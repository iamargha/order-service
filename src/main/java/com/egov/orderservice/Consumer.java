package com.egov.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class Consumer
{
    private final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private static final String TOPIC = "social-events";


    @KafkaListener(topics = TOPIC, groupId = "social_group_id")
    public void consume(String message) throws IOException
    {

        //analytics_counter.increment();

        ObjectMapper mapper  = new ObjectMapper();

        SocialEvent1 datum =  mapper.readValue(message,SocialEvent1.class);
        logger.info(message);
        logger.info(datum.toString());

        /*switch (datum.getType())
        {
            case "CREDENTIAL"   : logger.info(String.format("#### -> Consumed message -> %s", datum.getMessage()));break;
            case "USERDETAIL"   : logger.info( datum.getMessage()+" ---> "+mapper.readValue(datum.getPayload(), Userdetail.class));break;
            case "PRODUCTOFFER" : logger.
                    info(datum.getMessage()+" ---> "+mapper.readValue(datum.getPayload(), Productoffer.class));offer_counter.increment();break;
            default: logger.info(message);
        }*/
    }
}
