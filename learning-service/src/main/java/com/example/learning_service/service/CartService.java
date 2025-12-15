package com.example.learning_service.service;

import com.example.learning_service.dto.AddToCartRequest;
import com.example.learning_service.dto.CartDto;
import com.example.learning_service.entity.*;
import com.example.learning_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    public CartDto getCartByUserId(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        
        if (cartOpt.isEmpty()) {
            // Create a new cart if it doesn't exist
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setItems(new HashSet<>());
            cartRepository.save(newCart);
            return convertToDto(newCart);
        }
        
        return convertToDto(cartOpt.get());
    }

    @Transactional
    public CartDto addToCart(Long userId, AddToCartRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if user is already enrolled in this course
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(userId, request.getCourseId());
        if (alreadyEnrolled) {
            throw new RuntimeException("You are already enrolled in this course");
        }

        // Check if user has a completed order for this course (purchased but not yet enrolled)
        boolean hasCompletedOrder = orderRepository.existsByUserIdAndCourseIdAndStatus(
                userId, request.getCourseId(), Order.OrderStatus.COMPLETED);
        if (hasCompletedOrder) {
            throw new RuntimeException("You have already purchased this course");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new HashSet<>());
                    return cartRepository.save(newCart);
                });

        // Check if course is already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndCourse(cart, course);
        if (existingItem.isPresent()) {
            throw new RuntimeException("Course is already in your cart");
        }

        // Add new item to cart
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setCourse(course);
        cartItem.setPrice(course.getPrice());
        cartItem.setAddedAt(LocalDateTime.now());
        cartItemRepository.save(cartItem);

        return getCartByUserId(userId);
    }

    @Transactional
    public void removeFromCart(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to this cart");
        }

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        cartItemRepository.deleteByCart(cart);
    }

    private CartDto convertToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        BigDecimal total = BigDecimal.ZERO;
        Set<CartDto.CartItemDto> itemDtos = new HashSet<>();
        
        // Fetch cart items explicitly to avoid lazy loading issues
        List<CartItem> items = cartItemRepository.findByCart(cart);
        
        if (items != null) {
            for (CartItem item : items) {
                total = total.add(item.getPrice());
                
                CartDto.CartItemDto itemDto = new CartDto.CartItemDto();
                itemDto.setId(item.getId());
                itemDto.setCourseId(item.getCourse().getId());
                itemDto.setCourseTitle(item.getCourse().getTitle());
                itemDto.setCoursePrice(item.getCourse().getPrice());
                itemDto.setPrice(item.getPrice());
                itemDto.setThumbnailImage(item.getCourse().getThumbnailImage());
                itemDto.setAddedAt(item.getAddedAt());
                itemDtos.add(itemDto);
            }
        }
        
        dto.setItems(itemDtos);
        dto.setTotalAmount(total);
        
        return dto;
    }
}

