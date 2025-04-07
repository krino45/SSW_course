package com.krino.homework_8.core.service;

import com.krino.homework_8.api.dto.OrderRequest;
import com.krino.homework_8.core.model.*;
import com.krino.homework_8.core.model.paymentType.*;
import com.krino.homework_8.core.model.value.Address;
import com.krino.homework_8.core.model.value.measurements.Quantity;
import com.krino.homework_8.core.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class OrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Long customerId1;
    private Long customerId2;
    private Long itemId1;
    private Long itemId2;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        itemRepository.deleteAll();

        Item item1 = new Item();
        item1.setDescription("Test Item 1");
        itemRepository.save(item1);
        itemId1 = item1.getId();

        Item item2 = new Item();
        item2.setDescription("Test Item 2");
        itemRepository.save(item2);
        itemId2 = item2.getId();

        Customer customer1 = new Customer();
        customer1.setName("John Doe");
        Address address1 = new Address();
        address1.setCity("New York");
        address1.setStreet("123 Broadway");
        address1.setZipcode("10001");
        customer1.setAddress(address1);
        customerRepository.save(customer1);
        customerId1 = customer1.getId();

        Customer customer2 = new Customer();
        customer2.setName("Jane Smith");
        Address address2 = new Address();
        address2.setCity("Los Angeles");
        address2.setStreet("456 Hollywood Blvd");
        address2.setZipcode("90001");
        customer2.setAddress(address2);
        customerRepository.save(customer2);
        customerId2 = customer2.getId();

        Order order1 = new Order();
        order1.setCustomer(customer1);
        order1.setDate(LocalDateTime.now().minusDays(5));
        order1.setStatus("SHIPPED");

        OrderDetail detail1 = new OrderDetail();
        detail1.setItem(item1);
        detail1.setQuantity(new Quantity(2, "piece", "pc"));
        detail1.setTaxStatus("TAXABLE");
        order1.addOrderDetail(detail1);

        Cash cashPayment = new Cash();
        cashPayment.setAmount(100.0f);
        cashPayment.setCashTendered(100.0f);
        cashPayment.setStatus(PaymentStatus.COMPLETED);
        cashPayment.setOrder(order1);
        order1.setPayment(cashPayment);

        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setCustomer(customer2);
        order2.setDate(LocalDateTime.now().minusDays(2));
        order2.setStatus("PROCESSING");

        OrderDetail detail2 = new OrderDetail();
        detail2.setItem(item2);
        detail2.setQuantity(new Quantity(1, "piece", "pc"));
        detail2.setTaxStatus("TAX_EXEMPT");
        order2.addOrderDetail(detail2);

        Credit creditPayment = new Credit();
        creditPayment.setAmount(200.0f);
        creditPayment.setNumber("1234-5678-9012-3456");
        creditPayment.setCardType("VISA");
        creditPayment.setExpDate(LocalDateTime.now().plusYears(2));
        creditPayment.setStatus(PaymentStatus.PENDING);
        creditPayment.setOrder(order2);
        order2.setPayment(creditPayment);

        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setCustomer(customer1);
        order3.setDate(LocalDateTime.now().minusDays(1));
        order3.setStatus("CANCELLED");

        OrderDetail detail3 = new OrderDetail();
        detail3.setItem(item1);
        detail3.setQuantity(new Quantity(3, "piece", "pc"));
        detail3.setTaxStatus("TAXABLE");
        order3.addOrderDetail(detail3);

        Check checkPayment = new Check();
        checkPayment.setAmount(150.0f);
        checkPayment.setName("John Doe");
        checkPayment.setBankID("BANK123");
        checkPayment.setStatus(PaymentStatus.FAILED);
        checkPayment.setOrder(order3);
        order3.setPayment(checkPayment);

        orderRepository.save(order3);
    }

    // Existing tests remain unchanged

    @Test
    void shouldFindOrdersByCustomerName() {
        List<Order> orders = orderService.searchOrders(
                "John", null, null, null,
                null, null, null, null, null);

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getCustomer().getName()).isEqualTo("John Doe");
        assertThat(orders.get(1).getCustomer().getName()).isEqualTo("John Doe");
    }

    @Test
    void shouldFindOrdersByAddress() {
        List<Order> orders = orderService.searchOrders(
                null, "New York", null, null,
                null, null, null, null, null);

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getCustomer().getAddress().getCity()).isEqualTo("New York");
        assertThat(orders.get(1).getCustomer().getAddress().getCity()).isEqualTo("New York");
    }

    @Test
    void shouldFindOrdersByDateRange() {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(3);
        LocalDateTime toDate = LocalDateTime.now();

        List<Order> orders = orderService.searchOrders(
                null, null, null, null,
                fromDate, toDate, null, null, null);

        assertThat(orders).hasSize(2);
    }

    @Test
    void shouldFindOrdersByPaymentType() {
        List<Order> orders = orderService.searchOrders(
                null, null, null, null,
                null, null, "cash", null, null);

        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getPayment()).isInstanceOf(Cash.class);
    }

    @Test
    void shouldFindOrdersByOrderStatus() {
        List<Order> orders = orderService.searchOrders(
                null, null, null, null,
                null, null, null, "SHIPPED", null);

        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getStatus()).isEqualTo("SHIPPED");
    }

    @Test
    void shouldFindOrdersByPaymentStatus() {
        List<Order> orders = orderService.searchOrders(
                null, null, null, null,
                null, null, null, null, "PENDING");

        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getPayment().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void shouldFindOrdersWithMultipleCriteria() {
        List<Order> orders = orderService.searchOrders(
                "John", "New York", null, null,
                null, null, "check", null, null);

        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getCustomer().getName()).isEqualTo("John Doe");
        assertThat(orders.getFirst().getPayment()).isInstanceOf(Check.class);
    }

    // New tests for createOrder and updateOrder

    @Test
    void shouldCreateNewOrder() {
        // Create order request DTO
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId(customerId1);
        orderRequest.setStatus("NEW");
        orderRequest.setDate(LocalDateTime.now());

        // Add order details
        List<OrderRequest.OrderDetailDto> details = new ArrayList<>();
        OrderRequest.OrderDetailDto detailDto = new OrderRequest.OrderDetailDto();
        detailDto.setItemId(itemId1);
        detailDto.setTaxStatus("TAXABLE");

        OrderRequest.QuantityDto quantityDto = new OrderRequest.QuantityDto();
        quantityDto.setAmount(5);
        quantityDto.setUnit("piece");
        quantityDto.setUnitAbbreviation("pc");
        detailDto.setQuantity(quantityDto);

        details.add(detailDto);
        orderRequest.setOrderDetails(details);

        // Add payment
        OrderRequest.PaymentDto paymentDto = new OrderRequest.PaymentDto();
        paymentDto.setPaymentType("CREDIT");
        paymentDto.setAmount(300.0f);
        paymentDto.setStatus("PENDING");
        paymentDto.setCardNumber("4111-1111-1111-1111");
        paymentDto.setCardType("MASTERCARD");
        paymentDto.setExpiryDate(LocalDateTime.now().plusYears(1));
        orderRequest.setPayment(paymentDto);

        // Create the order
        Order createdOrder = orderService.createOrder(orderRequest);

        // Assertions
        assertNotNull(createdOrder.getId());
        assertEquals("NEW", createdOrder.getStatus());
        assertEquals(customerId1, createdOrder.getCustomer().getId());
        assertEquals(1, createdOrder.getOrderDetails().size());
        assertEquals(itemId1, createdOrder.getOrderDetails().getFirst().getItem().getId());
        assertEquals("TAXABLE", createdOrder.getOrderDetails().getFirst().getTaxStatus());
        assertEquals(5, createdOrder.getOrderDetails().getFirst().getQuantity().getValue());

        assertNotNull(createdOrder.getPayment());
        assertInstanceOf(Credit.class, createdOrder.getPayment());
        Credit creditPayment = (Credit) createdOrder.getPayment();
        assertEquals(300.0f, creditPayment.getAmount());
        assertEquals("4111-1111-1111-1111", creditPayment.getNumber());
        assertEquals("MASTERCARD", creditPayment.getCardType());
        assertEquals(PaymentStatus.PENDING, creditPayment.getStatus());
    }

    @Test
    void shouldUpdateExistingOrder() {
        // First, create an order to update
        OrderRequest createRequest = new OrderRequest();
        createRequest.setCustomerId(customerId1);
        createRequest.setStatus("NEW");

        List<OrderRequest.OrderDetailDto> details = new ArrayList<>();
        OrderRequest.OrderDetailDto detailDto = new OrderRequest.OrderDetailDto();
        detailDto.setItemId(itemId1);
        detailDto.setTaxStatus("TAXABLE");

        OrderRequest.QuantityDto quantityDto = new OrderRequest.QuantityDto();
        quantityDto.setAmount(5);
        quantityDto.setUnit("piece");
        quantityDto.setUnitAbbreviation("pc");
        detailDto.setQuantity(quantityDto);

        details.add(detailDto);
        createRequest.setOrderDetails(details);

        OrderRequest.PaymentDto paymentDto = new OrderRequest.PaymentDto();
        paymentDto.setPaymentType("CASH");
        paymentDto.setAmount(100.0f);
        paymentDto.setStatus("PENDING");
        paymentDto.setCashTendered(100.0f);
        createRequest.setPayment(paymentDto);

        Order createdOrder = orderService.createOrder(createRequest);
        Long orderId = createdOrder.getId();

        // Now update the order
        OrderRequest updateRequest = new OrderRequest();
        updateRequest.setStatus("PROCESSING");
        updateRequest.setCustomerId(customerId2); // Change customer

        // Change order details
        details = new ArrayList<>();
        detailDto = new OrderRequest.OrderDetailDto();
        detailDto.setItemId(itemId2); // Change item
        detailDto.setTaxStatus("TAX_EXEMPT"); // Change tax status

        quantityDto = new OrderRequest.QuantityDto();
        quantityDto.setAmount(2); // Change quantity
        quantityDto.setUnit("kilogram");
        quantityDto.setUnitAbbreviation("kg");
        detailDto.setQuantity(quantityDto);

        details.add(detailDto);
        updateRequest.setOrderDetails(details);

        // Change payment
        paymentDto = new OrderRequest.PaymentDto();
        paymentDto.setPaymentType("CHECK");
        paymentDto.setAmount(150.0f);
        paymentDto.setStatus("COMPLETED");
        paymentDto.setName("Jane Smith");
        paymentDto.setBankId("BANK456");
        updateRequest.setPayment(paymentDto);

        // Update the order
        Order updatedOrder = orderService.updateOrder(orderId, updateRequest);

        // Assertions
        assertEquals(orderId, updatedOrder.getId()); // Same ID
        assertEquals("PROCESSING", updatedOrder.getStatus());
        assertEquals(customerId2, updatedOrder.getCustomer().getId());

        assertEquals(1, updatedOrder.getOrderDetails().size());
        assertEquals(itemId2, updatedOrder.getOrderDetails().getFirst().getItem().getId());
        assertEquals("TAX_EXEMPT", updatedOrder.getOrderDetails().getFirst().getTaxStatus());
        assertEquals(2, updatedOrder.getOrderDetails().getFirst().getQuantity().getValue());
        assertEquals("kilogram", updatedOrder.getOrderDetails().getFirst().getQuantity().getName());

        assertNotNull(updatedOrder.getPayment());
        assertInstanceOf(Check.class, updatedOrder.getPayment());
        Check checkPayment = (Check) updatedOrder.getPayment();
        assertEquals(150.0f, checkPayment.getAmount());
        assertEquals("Jane Smith", checkPayment.getName());
        assertEquals("BANK456", checkPayment.getBankID());
        assertEquals(PaymentStatus.COMPLETED, checkPayment.getStatus());
    }

    @Test
    void shouldGetOrdersByCustomerId() {
        List<Order> orders = orderService.getOrdersByCustomerId(customerId1);

        assertThat(orders).hasSize(2);
        for (Order order : orders) {
            assertEquals(customerId1, order.getCustomer().getId());
        }
    }

    @Test
    void shouldGetAllOrders() {
        List<Order> orders = orderService.getAllOrders();

        assertThat(orders).hasSize(3); // Three orders were created in setup
    }
}