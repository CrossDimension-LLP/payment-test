package in.octosolutions.paymentgatewaymanagement.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.octosolutions.paymentgatewaymanagement.PaymentGatewayService;
import in.octosolutions.paymentgatewaymanagement.models.OrderRequest;
import in.octosolutions.paymentgatewaymanagement.models.OrderResponse;
import in.octosolutions.paymentgatewaymanagement.models.PaymentResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping("/paymentManagement/v1.0")
public class PaymentGatewayController {
    private RazorpayClient client;

    @Value("${razorpay.key}")
    private String key;

    @Value("${razorpay.secret}")
    private String secret;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    public PaymentGatewayController() throws RazorpayException {
        this.client = new RazorpayClient("rzp_test_6i2006Za1fnyi8", "Bh9WhUaadKNizBX7vQgLZ6tO");
    }

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse razorPay = null;
        try {
            // The transaction amount is expressed in the currency subunit, such
            // as paise (in case of INR)
            String amountInPaise = convertRupeeToPaise(orderRequest.getAmount());
            // Create an order in RazorPay and get the order id
            Order order = createRazorPayOrder(amountInPaise);
            razorPay = getOrderResponse((String) order.get("id"), amountInPaise);
            // Save order in the database
            //paymentGatewayService.saveOrder(razorPay.getRazorpayOrderId(), user.getUser().getId());
        } catch (RazorpayException e) {
            return ResponseEntity.ok(e.getMessage());
        }
        return ResponseEntity.ok(razorPay);
    }

    @PostMapping("/payment")
    public ResponseEntity<?> updateOrder(@RequestBody PaymentResponse paymentResponse) {
        String errorMsg = paymentGatewayService.validateAndUpdateOrder(paymentResponse.getRazorpayOrderId(),
                paymentResponse.getRazorpayPaymentId(), paymentResponse.getRazorpaySignature(), "Bh9WhUaadKNizBX7vQgLZ6tO");
        if (errorMsg != null) {
            //return new ResponseEntity<>(new ApiResponse(false, errorMsg), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(paymentResponse.getRazorpayPaymentId());
    }

    private OrderResponse getOrderResponse(String orderId, String amountInPaise) {
        OrderResponse razorPay = new OrderResponse();
        razorPay.setApplicationFee(amountInPaise);
        razorPay.setRazorpayOrderId(orderId);
        razorPay.setSecretKey(secret);
        return razorPay;
    }

    private Order createRazorPayOrder(String amount) throws RazorpayException {
        JSONObject options = new JSONObject();
        options.put("amount", amount);
        options.put("currency", "INR");
        options.put("receipt", "txn_123456");
        return client.Orders.create(options);
    }

    private String convertRupeeToPaise(String paise) {
        BigDecimal b = new BigDecimal(paise);
        BigDecimal value = b.multiply(new BigDecimal("100"));
        return value.setScale(0, RoundingMode.UP).toString();
    }
}
