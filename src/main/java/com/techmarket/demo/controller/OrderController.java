package com.techmarket.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.techmarket.demo.common.ApiResponse;
import com.techmarket.demo.dto.checkout.CheckOutItemDto;
import com.techmarket.demo.dto.checkout.StripeResponse;
import com.techmarket.demo.entity.Order;
import com.techmarket.demo.entity.User;
import com.techmarket.demo.exception.AuthenticationFailException;
import com.techmarket.demo.exception.OrderNotFoundException;
import com.techmarket.demo.service.AuthenticationService;
import com.techmarket.demo.service.OrderService;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthenticationService authenticationService;

    // stripe create session API
    @PostMapping("/create-checkout-session")
    public ResponseEntity<StripeResponse> checkoutList(@RequestBody List<CheckOutItemDto> checkoutItemDtoList)
            throws StripeException {
        System.out.println("TEST1");
        // create the stripe session
        Session session = orderService.createSession(checkoutItemDtoList);
        System.out.println("TEST2");
        StripeResponse stripeResponse = new StripeResponse(session.getId());
        System.out.println("TES3T");
        // send the stripe session id in response
        return new ResponseEntity<StripeResponse>(stripeResponse, HttpStatus.OK);
    }

    // place order after checkout
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> placeOrder(@RequestParam("token") String token,
            @RequestParam("sessionId") String sessionId)
            throws AuthenticationFailException {
        // validate token
        authenticationService.authenticate(token);
        // retrieve user
        User user = authenticationService.getUser(token);
        // place the order
        orderService.placeOrder(user, sessionId);
        return new ResponseEntity<>(new ApiResponse(true, "Order has been placed"), HttpStatus.CREATED);
    }

    // get all orders
    @GetMapping("/")
    public ResponseEntity<List<Order>> getAllOrders(@RequestParam("token") String token)
            throws AuthenticationFailException {
        // validate token
        authenticationService.authenticate(token);
        // retrieve user
        User user = authenticationService.getUser(token);
        // get orders
        List<Order> orderDtoList = orderService.listOrders(user);

        return new ResponseEntity<>(orderDtoList, HttpStatus.OK);
    }

    // get orderitems for an order
    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable("id") Integer id, @RequestParam("token") String token)
            throws AuthenticationFailException {
        // validate token
        authenticationService.authenticate(token);
        try {
            Order order = orderService.getOrder(id);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

}
