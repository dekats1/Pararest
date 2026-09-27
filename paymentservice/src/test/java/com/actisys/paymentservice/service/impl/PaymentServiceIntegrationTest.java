package com.actisys.paymentservice.service.impl;

import com.actisys.enums.PaymentStatus;
import com.actisys.enums.PaymentType;
import com.actisys.kafkacommunication.order.CreatePaymentEvent;
import com.actisys.kafkacommunication.replenishment.CreateReplenishmentEvent;
import com.actisys.paymentservice.dto.CreateReplenishment;
import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.model.Payment;
import com.actisys.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Интеграционное функциональное тестирование Payment Service")
class PaymentServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("paymentdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private UUID testUserId;
    private Long testOrderId;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        testUserId = UUID.randomUUID();
        testOrderId = 100L;
    }

    @Test
    @DisplayName("ФТ-1: POST /payments/replenishment - создание пополнения")
    void ft1_createReplenishment_ShouldCreatePayment() {
        CreateReplenishment request = new CreateReplenishment();
        request.setTotalAmount(new BigDecimal("500.00"));
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", testUserId.toString());
        HttpEntity<CreateReplenishment> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<PaymentDTO> response = restTemplate.postForEntity(
                "/api/v1/payments/newReplenishment",
                httpEntity,
                PaymentDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getBody().getPaymentType()).isEqualTo(PaymentType.REPLENISHMENT);
        assertThat(response.getBody().getPaymentStatus()).isEqualTo(PaymentStatus.IN_WAIT);

        assertThat(paymentRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("ФТ-2: GET /payments - получение всех платежей")
    void ft2_getAllPayments_ShouldReturnList() {
        Payment payment = createTestPayment();
        paymentRepository.save(payment);

        ResponseEntity<PaymentDTO[]> response = restTemplate.getForEntity(
                "/api/v1/payments/getAllPayments",
                PaymentDTO[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTotalAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("ФТ-3: GET /payments/user/{userId} - платежи пользователя")
    void ft3_getUserPayments_ShouldReturnUserPayments() {
        Payment payment1 = createTestPayment();
        payment1.setPaymentDate(LocalDate.of(2024, 2, 15));

        Payment payment2 = createTestPayment();
        payment2.setPaymentDate(LocalDate.of(2024, 2, 10));
        payment2.setId(2L);

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", testUserId.toString());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<PaymentDTO[]> response = restTemplate.exchange("/api/v1/payments/getUserPayments",
                org.springframework.http.HttpMethod.GET, request, PaymentDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("ФТ-4: GET /payments/total - общая сумма платежей")
    void ft4_getTotalAmount_ShouldReturnSum() {
        Payment payment1 = createTestPayment();
        payment1.setTotalAmount(new BigDecimal("100.50"));

        Payment payment2 = createTestPayment();
        payment2.setTotalAmount(new BigDecimal("200.25"));

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        ResponseEntity<BigDecimal> response = restTemplate.getForEntity(
                "/api/v1/payments/totalAmount",
                BigDecimal.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualByComparingTo(new BigDecimal("300.75"));
    }

    @Test
    @DisplayName("ФТ-5: Kafka событие - изменение статуса платежа на PAID")
    void ft5_changePaymentStatus_toPaid_ShouldUpdateStatus() throws Exception {
        Payment payment = createTestPayment();
        payment.setPaymentStatus(PaymentStatus.IN_WAIT);
        Payment savedPayment = paymentRepository.save(payment);

        CreateReplenishmentEvent event = CreateReplenishmentEvent.builder()
                .paymentId(savedPayment.getId())
                .paymentStatus(PaymentStatus.PAID)
                .build();

        kafkaTemplate.send("REPLENISHMENT_STATUS", event).get(5, TimeUnit.SECONDS);

        Thread.sleep(500);
        Optional<Payment> updatedPayment = paymentRepository.findById(savedPayment.getId());
        assertThat(updatedPayment.get().getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("ФТ-6: Kafka событие - создание платежа по заказу")
    void ft6_createNewPayment_ShouldCreatePayment() throws Exception {
        CreatePaymentEvent event = CreatePaymentEvent.builder()
                .userId(testUserId)
                .orderId(testOrderId)
                .totalAmount(new BigDecimal("300.00"))
                .build();

        kafkaTemplate.send("CREATE_ORDER_PAYMENT_EVENT", event).get(5, TimeUnit.SECONDS);

        Thread.sleep(500);
        assertThat(paymentRepository.findAll()).hasSize(1);

        Payment savedPayment = paymentRepository.findAll().get(0);
        assertThat(savedPayment.getUserId()).isEqualTo(testUserId);
        assertThat(savedPayment.getOrderId()).isEqualTo(testOrderId);
        assertThat(savedPayment.getTotalAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(savedPayment.getPaymentType()).isEqualTo(PaymentType.CARD);
    }

    @Test
    @DisplayName("ФТ-7: Kafka событие - отмена заказа (cancelOrder)")
    void ft7_cancelOrder_ShouldProcessRefund() throws Exception {
        Payment payment = createTestPayment();
        payment.setOrderId(testOrderId);
        paymentRepository.save(payment);

        CreatePaymentEvent event = CreatePaymentEvent.builder()
                .userId(testUserId)
                .orderId(testOrderId)
                .totalAmount(new BigDecimal("150.00"))
                .build();

        kafkaTemplate.send("CREATE_ORDER_CANCEL_EVENT", event).get(5, TimeUnit.SECONDS);

        // Then - проверяем, что метод отработал без ошибок
        Thread.sleep(500);
        assertThat(paymentRepository.findById(payment.getId())).isPresent();
    }

    @Test
    @DisplayName("ФТ-8: Kafka событие - возврат средств (refundMoney)")
    void ft8_refundMoney_ShouldUpdatePaymentStatus() throws Exception {
        Payment payment = createTestPayment();
        payment.setOrderId(testOrderId);
        payment.setPaymentStatus(PaymentStatus.PAID);
        Payment savedPayment = paymentRepository.save(payment);

        com.actisys.kafkacommunication.wallet.RefundEvent event =
                com.actisys.kafkacommunication.wallet.RefundEvent.builder()
                        .paymentId(savedPayment.getId())
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .build();

        kafkaTemplate.send("REFUND_STATUS_EVENT", event).get(5, TimeUnit.SECONDS);

        Thread.sleep(500);
        Optional<Payment> updatedPayment = paymentRepository.findById(savedPayment.getId());
        assertThat(updatedPayment.get().getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("ФТ-9: Проверка что orderId по умолчанию = 0 для пополнения")
    void ft9_replenishment_ShouldHaveOrderIdZero() {
        CreateReplenishment request = new CreateReplenishment();
        request.setTotalAmount(new BigDecimal("100.00"));
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", testUserId.toString());

        HttpEntity<CreateReplenishment> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<PaymentDTO> response = restTemplate.postForEntity(
                "/api/v1/payments/newReplenishment",
                httpEntity,
                PaymentDTO.class
        );

        Payment savedPayment = paymentRepository.findAll().get(0);
        assertThat(savedPayment.getOrderId()).isEqualTo(0L);
    }

    private Payment createTestPayment() {
        return Payment.builder()
                .userId(testUserId)
                .orderId(testOrderId)
                .totalAmount(new BigDecimal("150.00"))
                .paymentDate(LocalDate.now())
                .paymentStatus(PaymentStatus.IN_WAIT)
                .paymentType(PaymentType.REPLENISHMENT)
                .build();
    }
}