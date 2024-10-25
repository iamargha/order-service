package com.egov.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PaymentConsumer
{
    private final Logger logger = LoggerFactory.getLogger(PaymentConsumer.class);

    private static final String TOPIC = "payment-events";

    @Autowired
    private OrderStatusUpdateHandler handler;


    @KafkaListener(topics = TOPIC, groupId = "payment_group_id")
    public void consume(String message) throws IOException
    {

        //analytics_counter.increment();

        ObjectMapper mapper  = new ObjectMapper();
        logger.info("payment Consumer :: "+message);
        PaymentEvent paymentEvent =  mapper.readValue(message,PaymentEvent.class);

        logger.info(paymentEvent.toString());

        // get the user id
        // check the balance availability
        // if balance sufficient -> Payment completed and deduct amount from DB
        // if payment not sufficient -> cancel order event and update the amount in DB
        handler.updateOrder(paymentEvent.getPaymentRequestDto().getOrderId(),po->{
            po.setPaymentStatus(paymentEvent.getPaymentStatus());
        });

    }
}