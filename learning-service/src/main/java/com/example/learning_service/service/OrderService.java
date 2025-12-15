package com.example.learning_service.service;

import com.example.learning_service.dto.OrderDto;
import com.example.learning_service.dto.NotificationDto;
import com.example.learning_service.entity.*;
import com.example.learning_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NotificationService notificationService;

    @Transactional
    public OrderDto createOrderFromCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        // Fetch cart items explicitly to avoid lazy loading issues
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<OrderItem> orderItems = new HashSet<>();

        for (CartItem cartItem : cartItems) {
            // Check if user is already enrolled
            boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(
                    userId, cartItem.getCourse().getId());
            
            if (alreadyEnrolled) {
                continue; // Skip this item
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setCourse(cartItem.getCourse());
            orderItem.setPrice(cartItem.getPrice());
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(cartItem.getPrice());
        }

        if (orderItems.isEmpty()) {
            throw new RuntimeException("All courses in cart are already purchased");
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        // Create payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(totalAmount);
        payment.setMethod(Payment.PaymentMethod.STRIPE);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment);

        // Clear cart
        cartItemRepository.deleteByCart(cart);

        return convertToDto(order);
    }

    @Transactional
    public void confirmOrder(Long orderId, String paymentIntentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStripePaymentIntentId(paymentIntentId);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setProcessedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Create enrollments for the user
        User student = order.getUser();
        for (OrderItem item : order.getItems()) {
            // Check if enrollment already exists
            boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(
                    student.getId(), item.getCourse().getId());
            
            if (!alreadyEnrolled) {
                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(student);
                enrollment.setCourse(item.getCourse());
                enrollment.setEnrolledAt(LocalDateTime.now());
                enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
                enrollment.setProgress(0.0);
                enrollmentRepository.save(enrollment);
            }
        }

        // Envoyer email de confirmation de commande (erreurs ignorées pour ne pas bloquer)
        try { emailService.sendOrderCompleted(order); } catch (Exception ignored) {}
        
        // Create system notification about receipt being ready
        try {
            notificationService.createNotification(
                NotificationDto.builder()
                    .recipientId(student.getId())
                    .senderId(student.getId()) // System notification
                    .type("PAYMENT_COMPLETED")
                    .title("Payment Successful - Receipt Ready")
                    .message("Your payment has been completed successfully. Your receipt is ready. Go to your dashboard to view and download your transaction details.")
                    .referenceId(orderId)
                    .referenceType("ORDER")
                    .build()
            );
        } catch (Exception ignored) {}
    }

    public List<OrderDto> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToDto(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUser().getId());
        
        // Add user details for admin view
        if (order.getUser() != null) {
            dto.setUserEmail(order.getUser().getEmail());
            dto.setUserName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
        }
        
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setCompletedAt(order.getCompletedAt());

        // Fetch order items explicitly to avoid lazy loading issues
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        if (items != null && !items.isEmpty()) {
            List<OrderDto.OrderItemDto> itemDtos = items.stream()
                    .map(this::convertItemToDto)
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        if (order.getPayment() != null) {
            OrderDto.PaymentDto paymentDto = new OrderDto.PaymentDto();
            Payment payment = order.getPayment();
            paymentDto.setId(payment.getId());
            paymentDto.setAmount(payment.getAmount());
            paymentDto.setMethod(payment.getMethod());
            paymentDto.setStatus(payment.getStatus());
            paymentDto.setStripePaymentIntentId(payment.getStripePaymentIntentId());
            paymentDto.setCreatedAt(payment.getCreatedAt());
            paymentDto.setProcessedAt(payment.getProcessedAt());
            dto.setPayment(paymentDto);
        }

        return dto;
    }

    private OrderDto.OrderItemDto convertItemToDto(OrderItem item) {
        OrderDto.OrderItemDto dto = new OrderDto.OrderItemDto();
        dto.setId(item.getId());
        dto.setCourseId(item.getCourse().getId());
        dto.setCourseTitle(item.getCourse().getTitle());
        dto.setPrice(item.getPrice());
        dto.setThumbnailImage(item.getCourse().getThumbnailImage());
        return dto;
    }
}

