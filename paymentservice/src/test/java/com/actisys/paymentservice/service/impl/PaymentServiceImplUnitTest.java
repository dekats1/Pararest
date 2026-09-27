package com.actisys.paymentservice.service.impl;

import com.actisys.enums.PaymentStatus;
import com.actisys.enums.PaymentType;
import com.actisys.kafkacommunication.order.CreateOrderEvent;
import com.actisys.kafkacommunication.order.CreatePaymentEvent;
import com.actisys.kafkacommunication.replenishment.CreateReplenishmentEvent;
import com.actisys.kafkacommunication.wallet.CreateWalletEvent;
import com.actisys.kafkacommunication.wallet.RefundEvent;
import com.actisys.kafkacommunication.wallet.RefundMoneyEvent;
import com.actisys.paymentservice.dto.CreateReplenishment;
import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.exception.PaymentNotFoundException;
import com.actisys.paymentservice.mapper.PaymentMapper;
import com.actisys.paymentservice.model.Payment;
import com.actisys.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Интеграционное тестирование PaymentService")
class PaymentServiceImplUnitTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID testUserId;
    private Long testPaymentId;
    private Long testOrderId;
    private Payment testPayment;
    private PaymentDTO testPaymentDTO;
    private CreateReplenishment createReplenishment;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testPaymentId = 1L;
        testOrderId = 100L;

        testPayment = Payment.builder()
                .id(testPaymentId)
                .userId(testUserId)
                .orderId(testOrderId)
                .totalAmount(new BigDecimal("150.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.IN_WAIT)
                .paymentType(PaymentType.REPLENISHMENT)
                .build();

        testPaymentDTO = PaymentDTO.builder()
                .id(testPaymentId)
                .userId(testUserId)
                .orderId(testOrderId)
                .totalAmount(new BigDecimal("150.00"))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.IN_WAIT)
                .paymentType(PaymentType.REPLENISHMENT)
                .build();

        createReplenishment = new CreateReplenishment();
        createReplenishment.setTotalAmount(new BigDecimal("100.00"));
    }

    @Nested
    @DisplayName("Получение всех платежей")
    class GetAllPaymentsTests {

        @Test
        @DisplayName("Должен вернуть список всех платежей")
        void getAllPayments_ShouldReturnListOfPaymentDTOs() {
            Payment payment2 = Payment.builder()
                    .id(2L)
                    .userId(UUID.randomUUID())
                    .totalAmount(new BigDecimal("200.00"))
                    .build();

            PaymentDTO paymentDTO2 = PaymentDTO.builder()
                    .id(2L)
                    .totalAmount(new BigDecimal("200.00"))
                    .build();

            when(paymentRepository.findAll()).thenReturn(List.of(testPayment, payment2));
            when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);
            when(paymentMapper.toDTO(payment2)).thenReturn(paymentDTO2);

            List<PaymentDTO> result = paymentService.getAllPayments();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(PaymentDTO::getId)
                    .containsExactlyInAnyOrder(testPaymentId, 2L);
        }
    }

    @Nested
    @DisplayName("Получение платежей пользователя")
    class GetUserPaymentsTests {

        @Test
        @DisplayName("Должен вернуть платежи пользователя в порядке убывания даты")
        void getUserPayments_ShouldReturnUserPaymentsSortedByDateDesc() {
            Payment payment1 = Payment.builder()
                    .id(1L)
                    .userId(testUserId)
                    .paymentDate(LocalDateTime.of(2024, 1, 15, 12, 0))
                    .build();
            Payment payment2 = Payment.builder()
                    .id(2L)
                    .userId(testUserId)
                    .paymentDate(LocalDateTime.of(2024, 1, 10, 12, 0))
                    .build();

            PaymentDTO dto1 = PaymentDTO.builder().id(1L).build();
            PaymentDTO dto2 = PaymentDTO.builder().id(2L).build();

            when(paymentRepository.findAllByUserIdOrderByPaymentDateDesc(testUserId))
                    .thenReturn(List.of(payment1, payment2));
            when(paymentMapper.toDTO(payment1)).thenReturn(dto1);
            when(paymentMapper.toDTO(payment2)).thenReturn(dto2);

            List<PaymentDTO> result = paymentService.getUserPayments(testUserId);

            assertThat(result).hasSize(2);
            verify(paymentRepository).findAllByUserIdOrderByPaymentDateDesc(testUserId);
        }
    }

    @Nested
    @DisplayName("Создание пополнения счета")
    class CreateReplenishmentTests {

        @Test
        @DisplayName("Должен создать платеж и отправить событие в Kafka")
        void createNewReplenishment_ShouldCreatePaymentAndSendKafkaEvent() {
            // Given
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
                Payment savedPayment = invocation.getArgument(0);
                savedPayment.setId(1L);
                return savedPayment;
            });

            when(paymentMapper.toDTO(any(Payment.class))).thenAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                return PaymentDTO.builder()
                        .id(payment.getId())
                        .userId(payment.getUserId())
                        .orderId(payment.getOrderId())
                        .totalAmount(payment.getTotalAmount())
                        .paymentDate(payment.getPaymentDate())
                        .paymentStatus(payment.getPaymentStatus())
                        .paymentType(payment.getPaymentType())
                        .build();
            });

            PaymentDTO result = paymentService.createNewReplenishment(createReplenishment, testUserId);

            assertThat(result).isNotNull();
            assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.IN_WAIT);
            assertThat(result.getPaymentType()).isEqualTo(PaymentType.REPLENISHMENT);

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment savedPayment = paymentCaptor.getValue();
            assertThat(savedPayment.getUserId()).isEqualTo(testUserId);
            assertThat(savedPayment.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(savedPayment.getOrderId()).isEqualTo(0L);

            ArgumentCaptor<CreateWalletEvent> eventCaptor = ArgumentCaptor.forClass(CreateWalletEvent.class);
            verify(kafkaTemplate).send(eq("REPLENISHMENT_EVENT"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getUserId()).isEqualTo(testUserId);
            assertThat(eventCaptor.getValue().getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("Изменение статуса платежа")
    class ChangePaymentStatusTests {

        @Test
        @DisplayName("При статусе PAID должен отправить CREATE_PAYMENT_ORDER_EVENT")
        void changePaymentStatus_WhenPaid_ShouldSendOrderEvent() {
            CreateReplenishmentEvent event = CreateReplenishmentEvent.builder()
                    .paymentId(testPaymentId)
                    .paymentStatus(PaymentStatus.PAID)
                    .build();

            when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            paymentService.changePaymentStatus(event);

            verify(paymentRepository).save(argThat(payment ->
                    payment.getPaymentStatus() == PaymentStatus.PAID
            ));

            ArgumentCaptor<CreateOrderEvent> eventCaptor = ArgumentCaptor.forClass(CreateOrderEvent.class);
            verify(kafkaTemplate).send(eq("CREATE_PAYMENT_ORDER_EVENT"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getPaymentId()).isEqualTo(testPaymentId);
            assertThat(eventCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        }

        @Test
        @DisplayName("При статусе NOT_ENOUGH_MONEY должен отправить CREATE_PAYMENT_ORDER_EVENT")
        void changePaymentStatus_WhenNotEnoughMoney_ShouldSendOrderEvent() {
            testPayment.setOrderId(100L);
            CreateReplenishmentEvent event = CreateReplenishmentEvent.builder()
                    .paymentId(testPaymentId)
                    .paymentStatus(PaymentStatus.NOT_ENOUGH_MONEY)
                    .build();

            when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            paymentService.changePaymentStatus(event);

            ArgumentCaptor<CreateOrderEvent> eventCaptor = ArgumentCaptor.forClass(CreateOrderEvent.class);
            verify(kafkaTemplate).send(eq("CREATE_PAYMENT_ORDER_EVENT"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getOrderId()).isEqualTo(100L);
            assertThat(eventCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.NOT_ENOUGH_MONEY);
        }

        @Test
        @DisplayName("При статусе ERROR должен отправить CREATE_PAYMENT_ORDER_EVENT")
        void changePaymentStatus_WhenError_ShouldSendOrderEvent() {
            testPayment.setOrderId(100L);
            CreateReplenishmentEvent event = CreateReplenishmentEvent.builder()
                    .paymentId(testPaymentId)
                    .paymentStatus(PaymentStatus.ERROR)
                    .build();

            when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            paymentService.changePaymentStatus(event);

            ArgumentCaptor<CreateOrderEvent> eventCaptor = ArgumentCaptor.forClass(CreateOrderEvent.class);
            verify(kafkaTemplate).send(eq("CREATE_PAYMENT_ORDER_EVENT"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.ERROR);
        }

        @Test
        @DisplayName("При несуществующем платеже должно выбросить исключение")
        void changePaymentStatus_WhenPaymentNotFound_ShouldThrowException() {
            CreateReplenishmentEvent event = CreateReplenishmentEvent.builder()
                    .paymentId(999L)
                    .paymentStatus(PaymentStatus.PAID)
                    .build();

            when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.changePaymentStatus(event))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(kafkaTemplate, never()).send(anyString(), any());
        }
    }

    @Nested
    @DisplayName("Создание нового платежа по заказу")
    class CreateNewPaymentTests {

        @Test
        @DisplayName("Должен создать платеж и отправить PAYMENT_EVENT в Kafka")
        void createNewPayment_ShouldCreatePaymentAndSendKafkaEvent() {
            CreatePaymentEvent event = CreatePaymentEvent.builder()
                    .userId(testUserId)
                    .orderId(testOrderId)
                    .totalAmount(new BigDecimal("250.00"))
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            paymentService.createNewPayment(event);

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment savedPayment = paymentCaptor.getValue();
            assertThat(savedPayment.getUserId()).isEqualTo(testUserId);
            assertThat(savedPayment.getOrderId()).isEqualTo(testOrderId);
            assertThat(savedPayment.getTotalAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
            assertThat(savedPayment.getPaymentType()).isEqualTo(PaymentType.CARD);
            assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.IN_WAIT);

            ArgumentCaptor<CreatePaymentEvent> eventCaptor = ArgumentCaptor.forClass(CreatePaymentEvent.class);
            verify(kafkaTemplate).send(eq("PAYMENT_EVENT"), eventCaptor.capture());
        }
    }

    @Nested
    @DisplayName("Отмена заказа и возврат средств")
    class CancelOrderTests {

        @Test
        @DisplayName("Должен отправить REFUND_EVENT в Kafka")
        void cancelOrder_ShouldSendRefundEvent() {
            CreatePaymentEvent event = CreatePaymentEvent.builder()
                    .userId(testUserId)
                    .orderId(testOrderId)
                    .totalAmount(new BigDecimal("150.00"))
                    .build();

            when(paymentRepository.findByOrderId(testOrderId)).thenReturn(Optional.of(testPayment));

            paymentService.cancelOrder(event);

            ArgumentCaptor<RefundMoneyEvent> eventCaptor = ArgumentCaptor.forClass(RefundMoneyEvent.class);
            verify(kafkaTemplate).send(eq("REFUND_EVENT"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getUserId()).isEqualTo(testUserId);
            assertThat(eventCaptor.getValue().getPaymentId()).isEqualTo(testPaymentId);
            assertThat(eventCaptor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        }

        @Test
        @DisplayName("При отсутствии платежа должно выбросить исключение")
        void cancelOrder_WhenPaymentNotFound_ShouldThrowException() {
            CreatePaymentEvent event = CreatePaymentEvent.builder()
                    .orderId(999L)
                    .build();

            when(paymentRepository.findByOrderId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.cancelOrder(event))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(kafkaTemplate, never()).send(anyString(), any());
        }
    }

    @Nested
    @DisplayName("Возврат денег по статусу")
    class RefundMoneyTests {

        @Test
        @DisplayName("Должен обновить статус платежа и отправить REFUND_EVENT_END")
        void refundMoney_ShouldUpdatePaymentStatusAndSendEvent() {
            RefundEvent event = RefundEvent.builder()
                    .paymentId(testPaymentId)
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .build();

            testPayment.setOrderId(testOrderId);
            when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            paymentService.refundMoney(event);

            verify(paymentRepository).save(argThat(payment ->
                    payment.getPaymentStatus() == PaymentStatus.SUCCESS
            ));

            ArgumentCaptor<CreateOrderEvent> eventCaptor = ArgumentCaptor.forClass(CreateOrderEvent.class);
            verify(kafkaTemplate).send(eq("REFUND_EVENT_END"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getOrderId()).isEqualTo(testOrderId);
            assertThat(eventCaptor.getValue().getPaymentId()).isEqualTo(testPaymentId);
            assertThat(eventCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        @DisplayName("При отсутствии платежа должно выбросить исключение")
        void refundMoney_WhenPaymentNotFound_ShouldThrowException() {
            RefundEvent event = RefundEvent.builder()
                    .paymentId(999L)
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .build();

            when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.refundMoney(event))
                    .isInstanceOf(PaymentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Получение общей суммы всех платежей")
    class GetTotalAmountTests {

        @Test
        @DisplayName("Должен вернуть сумму всех платежей")
        void getTotalAmount_ShouldReturnSumOfAllPayments() {
            Payment payment1 = Payment.builder().totalAmount(new BigDecimal("100.50")).build();
            Payment payment2 = Payment.builder().totalAmount(new BigDecimal("200.25")).build();
            Payment payment3 = Payment.builder().totalAmount(new BigDecimal("50.25")).build();

            when(paymentRepository.findAll()).thenReturn(List.of(payment1, payment2, payment3));

            BigDecimal result = paymentService.getTotalAmount();

            assertThat(result).isEqualByComparingTo(new BigDecimal("351.00"));
        }

        @Test
        @DisplayName("При пустом списке должен вернуть 0")
        void getTotalAmount_WhenNoPayments_ShouldReturnZero() {
            when(paymentRepository.findAll()).thenReturn(List.of());

            BigDecimal result = paymentService.getTotalAmount();

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}