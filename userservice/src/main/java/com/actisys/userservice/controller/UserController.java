package com.actisys.userservice.controller;

import com.actisys.dto.CreateUserRequest;
import com.actisys.userservice.dto.UpdateUserProfileDTO;
import com.actisys.userservice.dto.UserDTO;
import com.actisys.userservice.model.UserRole;
import com.actisys.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/createNewUser")
    public ResponseEntity<Void> register(@RequestBody CreateUserRequest createUserRequest){
        userService.register(createUserRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getInfo")
    public ResponseEntity<UserDTO> getInfo(@RequestHeader("X-User-Id") String userId){
        return ResponseEntity.ok().body(userService.getInfo(UUID.fromString(userId)));
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAll(){
        return ResponseEntity.ok().body(userService.getAll());
    }

    @PutMapping("/changeRole/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> changeRole(@PathVariable String userId, @RequestBody UserRole role){
        return ResponseEntity.ok().body(userService.changeRole(UUID.fromString(userId),role));
    }

    @PutMapping("/handleAccess/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> handleAccess(@PathVariable String userId){
        return ResponseEntity.ok().body(userService.handleAccess(UUID.fromString(userId)));
    }

    @PutMapping("/updateUserRoom/{room}")
    public ResponseEntity<Void> updateRoomNumber(@RequestHeader("X-User-Id") String userId,
                                                 @PathVariable int room){
        userService.updateUserRoom(UUID.fromString(userId), room);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getMyRoomNumber")
    public ResponseEntity<Integer> getMyRoomNumber(@RequestHeader("X-User-Id") String userId){
        return ResponseEntity.ok().body(userService.getMyRoomNumber(UUID.fromString(userId)));
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<UserDTO> updateProfile(@RequestHeader("X-User-Id") String userId,
                                                 @RequestBody UpdateUserProfileDTO updateUserProfileDTO) {
        return ResponseEntity.ok().body(userService.updateProfile(UUID.fromString(userId), updateUserProfileDTO));
    }


}
