package com.actisys.userservice.service.impl;

import com.actisys.dto.CreateUserRequest;
import com.actisys.dto.UserAddDTO;
import com.actisys.enums.PaymentStatus;
import com.actisys.kafkacommunication.replenishment.CreateReplenishmentEvent;
import com.actisys.kafkacommunication.wallet.CreateWalletEvent;
import com.actisys.kafkacommunication.wallet.RefundEvent;
import com.actisys.kafkacommunication.wallet.RefundMoneyEvent;
import com.actisys.kafkacommunication.wallet.WithdrawEvent;
import com.actisys.userservice.dto.UpdateUserProfileDTO;
import com.actisys.userservice.dto.UserDTO;
import com.actisys.userservice.exception.NotEnoughMoneyException;
import com.actisys.userservice.exception.UserNotFoundException;
import com.actisys.userservice.mapper.UserMapper;
import com.actisys.userservice.model.User;
import com.actisys.userservice.model.UserRole;
import com.actisys.userservice.repository.UserRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульное тестирование UserService")
class UserServiceImplUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID testUserId;
    private User testUser;
    private UserDTO testUserDTO;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        UserAddDTO userDto = UserAddDTO.builder()
                .login("john_doe")
                .email("john@example.com")
                .phone("+1234567890")
                .birthday(LocalDate.of(1990, 1, 1))
                .fullName("John Doe")
                .build();

        createUserRequest = CreateUserRequest.builder()
                .id(testUserId)
                .user(userDto)
                .build();

        testUser = User.builder()
                .id(testUserId)
                .login("john_doe")
                .email("john@example.com")
                .phone("+1234567890")
                .birthday(LocalDate.of(1990, 1, 1))
                .fullName("John Doe")
                .role(UserRole.USER)
                .wallet(BigDecimal.ZERO)
                .roomNumber(null)
                .blocked(false)
                .build();

        testUserDTO = UserDTO.builder()
                .id(testUserId)
                .login("john_doe")
                .email("john@example.com")
                .phone("+1234567890")
                .birthday(LocalDate.of(1990, 1, 1))
                .fullName("John Doe")
                .role(UserRole.USER)
                .wallet(BigDecimal.ZERO)
                .roomNumber(null)
                .blocked(false)
                .build();
    }

    @Nested
    @DisplayName("Регистрация пользователя")
    class RegisterTests {

        @Test
        @DisplayName("Должен создать пользователя с правильными полями при регистрации")
        void register_ShouldCreateUserWithCorrectFields() {
            userService.register(createUserRequest);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getId()).isEqualTo(testUserId);
            assertThat(savedUser.getLogin()).isEqualTo("john_doe");
            assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
            assertThat(savedUser.getPhone()).isEqualTo("+1234567890");
            assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
            assertThat(savedUser.getFullName()).isEqualTo("John Doe");
            assertThat(savedUser.getWallet()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
            assertThat(savedUser.getRoomNumber()).isNull();
            assertThat(savedUser.getBlocked()).isFalse();
        }
    }

    @Nested
    @DisplayName("Получение информации о пользователе")
    class GetInfoTests {

        @Test
        @DisplayName("Должен вернуть UserDTO когда пользователь существует")
        void getInfo_WhenUserExists_ShouldReturnUserDTO() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

            UserDTO result = userService.getInfo(testUserId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testUserId);
            assertThat(result.getLogin()).isEqualTo("john_doe");
            verify(userRepository).findById(testUserId);
            verify(userMapper).toDto(testUser);
        }

        @Test
        @DisplayName("Должен выбросить UserNotFoundException когда пользователь не найден")
        void getInfo_WhenUserNotFound_ShouldThrowUserNotFoundException() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getInfo(testUserId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(testUserId.toString());
        }
    }

    @Nested
    @DisplayName("Получение всех пользователей")
    class GetAllTests {

        @Test
        @DisplayName("Должен вернуть список всех пользователей")
        void getAll_ShouldReturnListOfUserDTOs() {
            User user2 = User.builder()
                    .id(UUID.randomUUID())
                    .login("jane_doe")
                    .email("jane@example.com")
                    .role(UserRole.USER)
                    .wallet(BigDecimal.ZERO)
                    .blocked(false)
                    .build();

            UserDTO userDTO2 = UserDTO.builder()
                    .id(user2.getId())
                    .login("jane_doe")
                    .email("jane@example.com")
                    .role(UserRole.USER)
                    .wallet(BigDecimal.ZERO)
                    .blocked(false)
                    .build();

            when(userRepository.findAll()).thenReturn(List.of(testUser, user2));
            when(userMapper.toDto(testUser)).thenReturn(testUserDTO);
            when(userMapper.toDto(user2)).thenReturn(userDTO2);

            List<UserDTO> result = userService.getAll();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserDTO::getLogin)
                    .containsExactlyInAnyOrder("john_doe", "jane_doe");
        }
    }

    @Nested
    @DisplayName("Изменение роли пользователя")
    class ChangeRoleTests {

        @Test
        @DisplayName("Должен изменить роль пользователя на ADMIN")
        void changeRole_ShouldUpdateUserRoleToAdmin() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

            UserDTO result = userService.changeRole(testUserId, UserRole.ADMIN);

            verify(userRepository).save(argThat(user -> user.getRole() == UserRole.ADMIN));
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Блокировка/разблокировка пользователя")
    class AccessTests {

        @Test
        @DisplayName("Должен заблокировать пользователя при первом вызове")
        void handleAccess_FirstCall_ShouldBlockUser() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

            UserDTO result = userService.handleAccess(testUserId);

            verify(userRepository).save(argThat(user -> user.getBlocked() == true));
        }

        @Test
        @DisplayName("Должен разблокировать пользователя при втором вызове")
        void handleAccess_SecondCall_ShouldUnblockUser() {
            testUser.setBlocked(true);
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

            UserDTO result = userService.handleAccess(testUserId);

            verify(userRepository).save(argThat(user -> user.getBlocked() == false));
        }
    }

    @Nested
    @DisplayName("Удаление пользователя")
    class DeleteUserTests {

        @Test
        @DisplayName("Должен удалить существующего пользователя")
        void deleteUser_WhenUserExists_ShouldDelete() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            userService.deleteUser(testUserId);

            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("Должен выбросить исключение при удалении несуществующего пользователя")
        void deleteUser_WhenUserNotFound_ShouldThrowException() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(testUserId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Обновление профиля")
    class UpdateProfileTests {

        @Test
        @DisplayName("Должен обновить email, phone и номер комнаты")
        void updateProfile_ShouldUpdateSpecifiedFields() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

            UpdateUserProfileDTO updateDTO = UpdateUserProfileDTO.builder()
                    .email("newemail@example.com")
                    .phone("+9876543210")
                    .roomNumber(101)
                    .build();

            UserDTO result = userService.updateProfile(testUserId, updateDTO);

            verify(userRepository).save(argThat(user ->
                    user.getEmail().equals("newemail@example.com") &&
                            user.getPhone().equals("+9876543210") &&
                            user.getRoomNumber() == 101
            ));
        }
    }

    @Nested
    @DisplayName("Номер комнаты")
    class RoomNumberTests {

        @Test
        @DisplayName("Должен установить номер комнаты")
        void updateUserRoom_ShouldSetRoomNumber() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.updateUserRoom(testUserId, 42);

            verify(userRepository).save(argThat(user -> user.getRoomNumber() == 42));
        }

        @Test
        @DisplayName("Должен вернуть 0 если номер комнаты не установлен")
        void getMyRoomNumber_WhenRoomIsNull_ShouldReturnZero() {
            testUser.setRoomNumber(null);
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            Integer result = userService.getMyRoomNumber(testUserId);

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен вернуть номер комнаты если он установлен")
        void getMyRoomNumber_WhenRoomExists_ShouldReturnRoomNumber() {
            testUser.setRoomNumber(42);
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            Integer result = userService.getMyRoomNumber(testUserId);

            assertThat(result).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Пополнение кошелька")
    class TopUpTests {

        @Test
        @DisplayName("Должен увеличить баланс и отправить событие успеха")
        void topUp_ShouldIncreaseWalletAndSendSuccessEvent() {
            testUser.setWallet(new BigDecimal("100.00"));
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            Long paymentId = 12345L;
            CreateWalletEvent event = CreateWalletEvent.builder()
                    .userId(testUserId)
                    .totalAmount(new BigDecimal("50.00"))
                    .paymentId(paymentId)
                    .build();

            userService.topUp(event);

            verify(userRepository).save(argThat(user ->
                    user.getWallet().compareTo(new BigDecimal("150.00")) == 0
            ));

            ArgumentCaptor<CreateReplenishmentEvent> eventCaptor =
                    ArgumentCaptor.forClass(CreateReplenishmentEvent.class);
            verify(kafkaTemplate).send(eq("REPLENISHMENT_STATUS"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getPaymentId()).isEqualTo(paymentId);
            assertThat(eventCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }
    }

    @Nested
    @DisplayName("Списание денег")
    class WithdrawTests {

        @Test
        @DisplayName("Должен списать деньги и отправить PAID при достаточном балансе")
        void withdrawMoney_WithSufficientFunds_ShouldDecreaseWalletAndSendPaidEvent() {
            testUser.setWallet(new BigDecimal("200.00"));
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            Long paymentId = 12345L;
            CreateWalletEvent event = CreateWalletEvent.builder()
                    .userId(testUserId)
                    .totalAmount(new BigDecimal("75.50"))
                    .paymentId(paymentId)
                    .build();

            userService.withdrawMoney(event);

            verify(userRepository).save(argThat(user ->
                    user.getWallet().compareTo(new BigDecimal("124.50")) == 0
            ));

            ArgumentCaptor<WithdrawEvent> eventCaptor = ArgumentCaptor.forClass(WithdrawEvent.class);
            verify(kafkaTemplate).send(eq("WITHDRAW_MONEY"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getPaymentId()).isEqualTo(paymentId);
            assertThat(eventCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        }

        @Test
        @DisplayName("Должен выбросить исключение и отправить NOT_ENOUGH_MONEY при недостатке средств")
        void withdrawMoney_WithInsufficientFunds_ShouldThrowExceptionAndSendNotEnoughMoneyEvent() {
            testUser.setWallet(new BigDecimal("50.00"));
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            Long paymentId = 12345L;
            CreateWalletEvent event = CreateWalletEvent.builder()
                    .userId(testUserId)
                    .totalAmount(new BigDecimal("100.00"))
                    .paymentId(paymentId)
                    .build();

            assertThatThrownBy(() -> userService.withdrawMoney(event))
                    .isInstanceOf(NotEnoughMoneyException.class);

            verify(userRepository, never()).save(any());

            ArgumentCaptor<WithdrawEvent> eventCaptor = ArgumentCaptor.forClass(WithdrawEvent.class);
            verify(kafkaTemplate).send(eq("WITHDRAW_MONEY"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getPaymentId()).isEqualTo(paymentId);
            assertThat(eventCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.NOT_ENOUGH_MONEY);
        }
    }

    @Nested
    @DisplayName("Возврат средств")
    class RefundTests {

        @Test
        @DisplayName("Должен уменьшить баланс и отправить SUCCESS при успешном возврате")
        void refundMoneys_WhenUserExists_ShouldDecreaseWalletAndSendSuccessEvent() {
            testUser.setWallet(new BigDecimal("500.00"));
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            Long paymentId = 12345L;
            RefundMoneyEvent event = RefundMoneyEvent.builder()
                    .userId(testUserId)
                    .amount(new BigDecimal("30.00"))
                    .paymentId(paymentId)
                    .build();

            userService.refundMoneys(event);

            verify(userRepository).save(argThat(user ->
                    user.getWallet().compareTo(new BigDecimal("470.00")) == 0
            ));

            ArgumentCaptor<RefundEvent> eventCaptor = ArgumentCaptor.forClass(RefundEvent.class);
            verify(kafkaTemplate).send(eq("REFUND_STATUS_EVENT"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getPaymentId()).isEqualTo(paymentId);
            assertThat(eventCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        @DisplayName("Должен отправить ERROR если пользователь не найден")
        void refundMoneys_WhenUserNotFound_ShouldSendErrorEventAndThrowException() {
            Long paymentId = 12345L;
            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            RefundMoneyEvent event = RefundMoneyEvent.builder()
                    .userId(testUserId)
                    .amount(new BigDecimal("30.00"))
                    .paymentId(paymentId)
                    .build();

            assertThatThrownBy(() -> userService.refundMoneys(event))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(testUserId.toString());

            verify(userRepository, never()).save(any());

            ArgumentCaptor<RefundEvent> eventCaptor = ArgumentCaptor.forClass(RefundEvent.class);
            verify(kafkaTemplate).send(eq("REFUND_STATUS_EVENT"), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getPaymentId()).isEqualTo(paymentId);
            assertThat(eventCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.ERROR);
        }
    }
}