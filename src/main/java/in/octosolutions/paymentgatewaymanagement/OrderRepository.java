package in.octosolutions.paymentgatewaymanagement;

import in.octosolutions.paymentgatewaymanagement.models.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, Long> {
    Order findByRazorpayOrderId(String orderId);
}
