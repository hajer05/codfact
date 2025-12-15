package com.example.learning_service.controller;

import com.example.learning_service.dto.OrderDto;
import com.example.learning_service.dto.PaymentIntentResponse;
import com.example.learning_service.dto.CreatePaymentIntentRequest;
import com.example.learning_service.dto.ConfirmPaymentRequest;
import com.example.learning_service.entity.Order;
import com.example.learning_service.entity.Payment;
import com.example.learning_service.security.JwtTokenProvider;
import com.example.learning_service.service.OrderService;
import com.example.learning_service.service.StripeService;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return tokenProvider.getUserIdFromToken(token);
        }
        return null;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            OrderDto order = orderService.createOrderFromCart(userId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            System.err.println("Error creating order: " + errorMessage);
            return ResponseEntity.badRequest().body(Map.of("error", errorMessage));
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getMyOrders(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            List<OrderDto> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            OrderDto order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/payment-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@RequestBody CreatePaymentIntentRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            OrderDto order = orderService.getOrderById(request.getOrderId());
            
            com.stripe.model.PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                    request.getAmount(),
                    request.getCurrency(),
                    order.getId(),
                    "customer@example.com" // You should get this from user
            );

            PaymentIntentResponse response = new PaymentIntentResponse();
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setPaymentIntentId(paymentIntent.getId());
            response.setOrderId(order.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<OrderDto> confirmOrder(@RequestBody ConfirmPaymentRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            PaymentIntent paymentIntent = stripeService.retrievePaymentIntent(request.getPaymentIntentId());
            
            if (paymentIntent.getStatus().equals("succeeded")) {
                orderService.confirmOrder(request.getOrderId(), request.getPaymentIntentId());
                OrderDto order = orderService.getOrderById(request.getOrderId());
                return ResponseEntity.ok(order);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<OrderDto>> getAllOrders(HttpServletRequest request) {
        try {
            List<OrderDto> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

