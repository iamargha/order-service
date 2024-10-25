package com.egov.orderservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Tag(name = "Order Service", description = "Order Service APIs")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    RequestIdExtractor requestIdExtractor;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("webClient_2")
    WebClient product_webClient;



    @PostMapping("/create")
    public PurchaseOrder createOrder(@RequestBody OrderRequestDto orderRequestDto){
        return orderService.createOrder(orderRequestDto);
    }

    @GetMapping
    public List<PurchaseOrder> getOrders(){
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable Integer id,
                                                        HttpServletRequest request, HttpServletResponse servletResponse) {

        List<Cookie> cookieList = null;
        String cookieName = "order_status_cookie";
        ObjectMapper objectMapper = new ObjectMapper();
        //Optional<String> healthStatusCookie = Optional.ofNullable(request.getHeader("health_status_cookie"));
        Cookie[] cookies = request.getCookies();
        if(cookies == null)
        {
            cookieList = new ArrayList<>();
        }
        else
        {
            // REFACTOR TO TAKE NULL VALUES INTO ACCOUNT
            cookieList = List.of(cookies);
        }


        if( cookieList.stream().filter(cookie -> cookie.getName().equals(cookieName)).findAny().isEmpty()) // COOKIE_CHECK
        {
            int productId = 1;

            String requestid = requestIdExtractor.getRequestId(request);
            String cookieValue = "ss-1-"+requestid;
            Cookie cookie1 = new Cookie(cookieName, cookieValue);
            cookie1.setMaxAge(3600);

            Mono<Product> responseMono = product_webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/" + productId)  // Append productId to the URI path
                            .build())
                    .retrieve()                // Retrieve the response
                    .bodyToMono(Product.class);

            logger.info("getProductById :: " + responseMono);

            responseMono.subscribe( // ASYNC RESPONSE HANDLER
                    response1 -> { // SUCCESS HANDLER
                        logger.info(response1.toString() + " from the product service");
                        //finalResponse[0] = response;
                        PurchaseOrder purchaseOrder = orderService.getOrder(id);
                        OrderDetails orderDetails = new OrderDetails(purchaseOrder,response1);
                        logger.info(orderDetails.toString() + " from the product service");
                        //orderDetails.setOrderDetails(purchaseOrder,response1);
                        try {
                            redisTemplate.opsForValue().set(cookieValue, objectMapper.writeValueAsString(orderDetails));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    error1 ->
                    {
                        // ROLLBACK + FAILURE MESSAGE UPDATION IN CACHE
                        logger.info("error processing the response " + error1);
                    });

            servletResponse.addCookie(cookie1);
            //return ResponseEntity.ok(orderService.getOrder(id));
            return ResponseEntity.ok("Request is being processed.....");
        }
        else{
            // TO BE MODIFIED TO CHECK FOR COOKIE AND NOT HEADER
            //String cookie = request.getHeader("order_status_cookie");
            Optional<String> cookieValue = cookieList.stream()
                    .filter(cookie -> cookie.getName().equals(cookieName))  // Filter by cookie name
                    .map(Cookie::getValue)  // Get the value of the matched cookie
                    .findFirst();  // Return the first match

            // If the cookie is found, get the value, otherwise return a default value or handle accordingly
            String cookie = cookieValue.orElse(null);
            logger.info("cookie :: " + cookie);
            String response = (String)redisTemplate.opsForValue().get(cookie);
            if(response == null)
            {
                return ResponseEntity.notFound().build();
            }
            else
            {
                OrderDetails orderDetails = null;
                try {
                    orderDetails = objectMapper.readValue(response, OrderDetails.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                return ResponseEntity.ok().body(orderDetails);
            }
        }
    }
}
