package com.actisys.userservice.service.impl;

import com.actisys.dto.CreateUserRequest;
import com.actisys.dto.UserAddDTO;
import com.actisys.kafkacommunication.wallet.CreateWalletEvent;
import com.actisys.kafkacommunication.wallet.RefundMoneyEvent;
import com.actisys.userservice.dto.UpdateUserProfileDTO;
import com.actisys.userservice.dto.UserDTO;
import com.actisys.userservice.model.User;
import com.actisys.userservice.model.UserRole;
import com.actisys.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
@DisplayName("Интеграционное функциональное тестирование User Service")
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("userdb")
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
    private UserRepository userRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private UUID testUserId;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUserId = UUID.randomUUID();

        UserAddDTO userDto = UserAddDTO.builder()
                .login("integration_user")
                .email("integration@example.com")
                .phone("+123456789")
                .birthday(LocalDate.of(1995, 5, 15))
                .fullName("Integration Test User")
                .build();

        createUserRequest = CreateUserRequest.builder()
                .id(testUserId)
                .user(userDto)
                .build();
    }


    @Test
    @DisplayName("ФТ-1: POST /createNewUser - регистрация нового пользователя")
    void ft1_registerUser_ShouldCreateUser() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/v1/users/createNewUser",
                createUserRequest,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Optional<User> savedUser = userRepository.findById(testUserId);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getLogin()).isEqualTo("integration_user");
        assertThat(savedUser.get().getEmail()).isEqualTo("integration@example.com");
        assertThat(savedUser.get().getWallet()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedUser.get().getBlocked()).isFalse();
    }

    @Test
    @DisplayName("ФТ-3: GET /getInfo - получение информации о пользователе")
    void ft3_getUserInfo_ShouldReturnUserDTO() {
        userRepository.save(createTestUser());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", testUserId.toString());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<UserDTO> response = restTemplate.exchange("/api/v1/users/getInfo",
                HttpMethod.GET, request, UserDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLogin()).isEqualTo("integration_user");
        assertThat(response.getBody().getEmail()).isEqualTo("integration@example.com");
        assertThat(response.getBody().getWallet()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("ФТ-4: GET /getInfo - пользователь не найден")
    void ft4_getUserInfo_NotFound_ShouldReturn404() {
        UUID nonExistentId = UUID.randomUUID();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", nonExistentId.toString());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<UserDTO> response = restTemplate.exchange("/api/v1/users/getInfo",
                HttpMethod.GET, request, UserDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    @DisplayName("ФТ-5: PUT /changeRole/{id} - изменение роли")
    void ft5_changeRole_ShouldUpdateUserRole() {
        userRepository.save(createTestUser());

        ResponseEntity<UserDTO> response = restTemplate.exchange(
                "/api/v1/users/changeRole/" + testUserId,
                HttpMethod.PUT,
                new HttpEntity<>(UserRole.ADMIN),
                UserDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getRole()).isEqualTo(UserRole.ADMIN);

        Optional<User> updatedUser = userRepository.findById(testUserId);
        assertThat(updatedUser.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("ФТ-6 и ФТ-7: PUT /handleAccess/{id} - блокировка/разблокировка")
    void ft6_ft7_handleAccess_ShouldToggleBlockStatus() {
        userRepository.save(createTestUser());

        // When - block
        ResponseEntity<UserDTO> blockResponse = restTemplate.exchange(
                "/api/v1/users/handleAccess/" + testUserId,
                HttpMethod.PUT,
                null,
                UserDTO.class
        );

        // Then - blocked
        assertThat(blockResponse.getBody().getBlocked()).isTrue();

        // When - unblock
        ResponseEntity<UserDTO> unblockResponse = restTemplate.exchange(
                "/api/v1/users/handleAccess/" + testUserId,
                HttpMethod.PUT,
                null,
                UserDTO.class
        );

        // Then - unblocked
        assertThat(unblockResponse.getBody().getBlocked()).isFalse();
    }

    // ==================== ФТ-8 до ФТ-9: Профиль и номер комнаты ====================

    @Test
    @DisplayName("ФТ-8: PUT /updateProfile - обновление профиля")
    void ft9_updateProfile_ShouldUpdateFields() {
        userRepository.save(createTestUser());

        UpdateUserProfileDTO updateDTO = UpdateUserProfileDTO.builder()
                .email("updated@example.com")
                .phone("+999888777")
                .roomNumber(202)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", testUserId.toString());
        HttpEntity<UpdateUserProfileDTO> request = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<UserDTO> response = restTemplate.exchange(
                "/api/v1/users/updateProfile",
                HttpMethod.PUT,
                request,
                UserDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo("updated@example.com");
        assertThat(response.getBody().getPhone()).isEqualTo("+999888777");
        assertThat(response.getBody().getRoomNumber()).isEqualTo(202);
    }

    @Test
    @DisplayName("ФТ-9: PUT /updateUserRoom/{room} - установка номера комнаты")
    void ft10_updateUserRoom_ShouldSetRoomNumber() {
        userRepository.save(createTestUser());
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", testUserId.toString());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/users/updateUserRoom/303",
                HttpMethod.PUT,
                request,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Optional<User> updatedUser = userRepository.findById(testUserId);
        assertThat(updatedUser.get().getRoomNumber()).isEqualTo(303);
    }

    // ==================== ФТ-10 до ФТ-11: Получение номера комнаты ====================

    @Test
    @DisplayName("ФТ-10: GET /getMyRoomNumber - получение номера комнаты когда есть")
    void ft11_getMyRoomNumber_WhenExists_ShouldReturnRoomNumber() {
        User user = createTestUser();
        user.setRoomNumber(404);
        userRepository.save(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", testUserId.toString());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Integer> response = restTemplate.exchange("/api/v1/users/getMyRoomNumber",
                HttpMethod.GET, request, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(404);
    }

    @Test
    @DisplayName("ФТ-11: GET /getMyRoomNumber - получение номера комнаты когда нет")
    void ft12_getMyRoomNumber_WhenNull_ShouldReturnZero() {
        userRepository.save(createTestUser());
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", testUserId.toString());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Integer> response = restTemplate.exchange("/api/v1/users/getMyRoomNumber",
                HttpMethod.GET, request, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0);
    }

    // ==================== ФТ-12: Пополнение кошелька ====================

    @Test
    @DisplayName("ФТ-12: Kafka событие REPLENISHMENT_EVENT - пополнение кошелька")
    void ft13_topUp_ShouldIncreaseWallet() throws Exception {
        User user = createTestUser();
        user.setWallet(new BigDecimal("100.00"));
        userRepository.save(user);

        CreateWalletEvent event = CreateWalletEvent.builder()
                .userId(testUserId)
                .totalAmount(new BigDecimal("50.00"))
                .paymentId(12345L)
                .build();

        kafkaTemplate.send("REPLENISHMENT_EVENT", event).get(5, TimeUnit.SECONDS);

        // Then - wait a bit for async processing
        Thread.sleep(1000);

        Optional<User> updatedUser = userRepository.findById(testUserId);
        assertThat(updatedUser.get().getWallet()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    // ==================== ФТ-13 до ФТ-14: Списание денег ====================

    @Test
    @DisplayName("ФТ-13: Списание денег при достаточном балансе")
    void ft14_withdrawMoney_SufficientFunds_ShouldDecreaseWallet() throws Exception {
        User user = createTestUser();
        user.setWallet(new BigDecimal("200.00"));
        userRepository.save(user);

        CreateWalletEvent event = CreateWalletEvent.builder()
                .userId(testUserId)
                .totalAmount(new BigDecimal("75.00"))
                .paymentId(12346L)
                .build();

        kafkaTemplate.send("PAYMENT_EVENT", event).get(5, TimeUnit.SECONDS);

        Thread.sleep(1000);

        Optional<User> updatedUser = userRepository.findById(testUserId);
        assertThat(updatedUser.get().getWallet()).isEqualByComparingTo(new BigDecimal("125.00"));
    }

    @Test
    @DisplayName("ФТ-14: Списание денег при недостаточном балансе")
    void ft15_withdrawMoney_InsufficientFunds_ShouldNotChangeWallet() throws Exception {
        User user = createTestUser();
        user.setWallet(new BigDecimal("50.00"));
        userRepository.save(user);

        CreateWalletEvent event = CreateWalletEvent.builder()
                .userId(testUserId)
                .totalAmount(new BigDecimal("100.00"))
                .paymentId(12347L)
                .build();

        kafkaTemplate.send("PAYMENT_EVENT", event).get(5, TimeUnit.SECONDS);

        Thread.sleep(1000);

        Optional<User> updatedUser = userRepository.findById(testUserId);
        assertThat(updatedUser.get().getWallet()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    // ==================== ФТ-15: Возврат средств ====================

    @Test
    @DisplayName("ФТ-15: Возврат средств пользователю")
    void ft16_refundMoneys_ShouldDecreaseWallet() throws Exception {
        User user = createTestUser();
        user.setWallet(new BigDecimal("500.00"));
        userRepository.save(user);

        RefundMoneyEvent event = RefundMoneyEvent.builder()
                .userId(testUserId)
                .amount(new BigDecimal("30.00"))
                .paymentId(12348L)
                .build();

        kafkaTemplate.send("REFUND_EVENT", event).get(5, TimeUnit.SECONDS);

        Thread.sleep(1000);

        Optional<User> updatedUser = userRepository.findById(testUserId);
        assertThat(updatedUser.get().getWallet()).isEqualByComparingTo(new BigDecimal("470.00"));
    }

    @Test
    @DisplayName("ФТ-16: GET /getAll - получение всех пользователей")
    void ft18_getAllUsers_ShouldReturnList() {
        userRepository.save(createTestUser());

        User secondUser = User.builder()
                .id(UUID.randomUUID())
                .login("second_user")
                .email("second@example.com")
                .role(UserRole.USER)
                .wallet(BigDecimal.ZERO)
                .blocked(false)
                .build();
        userRepository.save(secondUser);

        ResponseEntity<UserDTO[]> response = restTemplate.getForEntity("/api/v1/users/getAll", UserDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).extracting(UserDTO::getLogin)
                .containsExactlyInAnyOrder("integration_user", "second_user");
    }


    private User createTestUser() {
        return User.builder()
                .id(testUserId)
                .login("integration_user")
                .email("integration@example.com")
                .phone("+123456789")
                .birthday(LocalDate.of(1995, 5, 15))
                .fullName("Integration Test User")
                .role(UserRole.USER)
                .wallet(BigDecimal.ZERO)
                .roomNumber(null)
                .blocked(false)
                .build();
    }
}