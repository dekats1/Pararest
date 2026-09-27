package com.actisys.userservice.service.impl;

import com.actisys.dto.CreateUserRequest;
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
import com.actisys.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void register(CreateUserRequest createUserRequest) {
        User user = User.builder()
                .id(createUserRequest.getId())
                .login(createUserRequest.getUser().getLogin())
                .email(createUserRequest.getUser().getEmail())
                .birthday(createUserRequest.getUser().getBirthday())
                .phone(createUserRequest.getUser().getPhone())
                .fullName(createUserRequest.getUser().getFullName())
                .role(UserRole.USER)
                .wallet(new BigDecimal(0))
                .roomNumber(null)
                .blocked(false)
                .build();
        userRepository.save(user);
    }

    @Override
    public UserDTO getInfo(UUID uuid) {
        User user = userRepository.findById(uuid).orElseThrow(()->new UserNotFoundException(uuid));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDTO> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public UserDTO changeRole(UUID id, UserRole role) {
        User user = userRepository.findById(id).orElseThrow(()->new UserNotFoundException(id));
        user.setRole(role);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDTO handleAccess(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(userId));
        user.setBlocked(!user.getBlocked());
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(userId));

        userRepository.delete(user);
    }

    @Override
    public void updateUserRoom(UUID uuid, int room) {
        User user = userRepository.findById(uuid).orElseThrow(()->new UserNotFoundException(uuid));
        user.setRoomNumber(room);
        userRepository.save(user);
    }

    @Override
    public UserDTO updateProfile(UUID uuid, UpdateUserProfileDTO updateUserProfileDTO) {
        User user = userRepository.findById(uuid).orElseThrow(() -> new UserNotFoundException(uuid));
        user.setEmail(updateUserProfileDTO.getEmail());
        user.setPhone(updateUserProfileDTO.getPhone());
        user.setRoomNumber(updateUserProfileDTO.getRoomNumber());
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void topUp(CreateWalletEvent event) {
        User user = userRepository.findById(event.getUserId()).orElseThrow(
                ()->new UserNotFoundException(event.getUserId()));
        user.setWallet(user.getWallet().add(event.getTotalAmount()));
        userRepository.save(user);
        CreateReplenishmentEvent replenishmentEvent = CreateReplenishmentEvent.builder()
                .paymentId(event.getPaymentId())
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
        kafkaTemplate.send("REPLENISHMENT_STATUS", replenishmentEvent);
    }

    @Override
    public void withdrawMoney(CreateWalletEvent event) {
        log.info("Processing withdrawMoney event: userId={}, paymentId={}, amount={}", 
                event.getUserId(), event.getPaymentId(), event.getTotalAmount());
        
        User user = userRepository.findById(event.getUserId()).orElseThrow(
                ()->new UserNotFoundException(event.getUserId()));
        
        log.info("User current wallet balance: {}", user.getWallet());
        
        WithdrawEvent withdrawEvent = new WithdrawEvent();
        withdrawEvent.setPaymentId(event.getPaymentId());
        
        if(user.getWallet().compareTo(event.getTotalAmount()) < 0){
            log.warn("Not enough money: user balance={}, required amount={}", 
                    user.getWallet(), event.getTotalAmount());
            withdrawEvent.setPaymentStatus(PaymentStatus.NOT_ENOUGH_MONEY);
            kafkaTemplate.send("WITHDRAW_MONEY", withdrawEvent);
            throw new NotEnoughMoneyException();
        }
        
        BigDecimal newBalance = user.getWallet().subtract(event.getTotalAmount());
        user.setWallet(newBalance);
        userRepository.save(user);
        
        log.info("Money withdrawn successfully. New balance: {}", newBalance);
        
        withdrawEvent.setPaymentStatus(PaymentStatus.PAID);
        kafkaTemplate.send("WITHDRAW_MONEY", withdrawEvent);
    }

    @Override
    public void refundMoneys(RefundMoneyEvent event) {
        User user = userRepository.findById(event.getUserId()).orElseThrow(
                ()->{
                    RefundEvent statEvent = RefundEvent.builder()
                            .paymentId(event.getPaymentId())
                            .paymentStatus(PaymentStatus.ERROR)
                            .build();
                    kafkaTemplate.send("REFUND_STATUS_EVENT", statEvent);
                    return new UserNotFoundException(event.getUserId());
                });
        user.setWallet(user.getWallet().add(event.getAmount()));
        userRepository.save(user);
        RefundEvent statEvent = RefundEvent.builder()
                .paymentId(event.getPaymentId())
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
        kafkaTemplate.send("REFUND_STATUS_EVENT", statEvent);
    }

    @Override
    public Integer getMyRoomNumber(UUID uuid) {
        User user = userRepository.findById(uuid).orElseThrow(()->new UserNotFoundException(uuid));
        if(user.getRoomNumber() == null){
            return 0;
        }
        return user.getRoomNumber();
    }
}
