package com.hsenid.surveyapp.service;

import com.hsenid.surveyapp.dto.ResetPasswordRequestDto;
import com.hsenid.surveyapp.dto.UserDto;
import com.hsenid.surveyapp.dto.UserResponseDto;
import com.hsenid.surveyapp.exceptions.NotFoundException;
import com.hsenid.surveyapp.model.User;
import com.hsenid.surveyapp.repositoy.UserRepository;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public UserDto updateUser(final String userId, final UserDto userDto) {
        Optional<User> userObj = userRepository.findById(userId);
        if (userObj.isPresent()) {
            User user = userObj.get();
            logger.info("updating user: " + user.getId());
            logger.info("dto user: " + userDto.getId());
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            if (StringUtils.isNotBlank(userDto.getPassword())) {
                user.setPassword(encoder.encode(userDto.getPassword()));
            }
            userRepository.save(user);
            logger.info("updated user: " + user.getId());
            return userDto;
        } else {
            logger.info("user not found");
            throw new NotFoundException("user not found");
        }
    }

    @Override
    public UserDto getUserById(final String userId) {
        Optional<User> userObj = userRepository.findById(userId);
        if (userObj.isPresent()) {
            User user = userObj.get();
            logger.info("retrieving user: " + user.getId());
            List<String> roles = new ArrayList<>();
            user.getRoles().stream().forEach(role -> roles.add(role.getName().name()));
            return UserDto.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail()).role(roles).build();
        } else {
            logger.info("user not found");
            throw new NotFoundException("user not found");
        }
    }

    @Override
    public UserDto getUserByName(final String userName) {
        Optional<User> userobj = userRepository.findByUsername(userName);
        if (userobj.isPresent()) {
            User user = userobj.get();
            logger.info("retrieving user: " + user.getId());
            List<String> roles = new ArrayList<>();
            user.getRoles().stream().forEach(role -> roles.add(role.getName().name()));
            return UserDto.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail()).role(roles).build();
        } else {
            logger.info("user not found");
            throw new NotFoundException("user not found");
        }
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        logger.info("getting list of users");
        List<UserResponseDto> userResponseDtoList = new ArrayList<>();
        List<User> users = userRepository.findAll();

        users.forEach(user -> {
            //String role = user.getRoles().contains();
            user.getRoles().stream().filter(role -> {
                if (role.getName().name().equals("ROLE_USER")) {
                    UserResponseDto userResponseDto = new UserResponseDto();
                    userResponseDto.setUsername(user.getUsername());
                    userResponseDto.setEmail(user.getEmail());
                    userResponseDto.setId(user.getId());
                    userResponseDtoList.add(userResponseDto);
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
        });
//            if (user.getRoles().contains("ROLE_USER")) {
//                UserResponseDto userResponseDto = new UserResponseDto();
//                userResponseDto.setUsername(user.getUsername());
//                userResponseDto.setEmail(user.getEmail());
//                userResponseDto.setId(user.getId());
//                userResponseDtoList.add(userResponseDto);
//                return true;
//            } else {
//                return false;
//            }
//        }).collect(Collectors.toList());

//        users.stream().forEach(user -> {
//            UserResponseDto userResponseDto = new UserResponseDto();
//            userResponseDto.setUsername(user.getUsername());
//            userResponseDto.setEmail(user.getEmail());
//            userResponseDto.setId(user.getId());
//            userResponseDtoList.add(userResponseDto);
//        });
        return userResponseDtoList;
    }

    @Override
    public String deleteUser(final String userId) {
        Optional<User> userObj = userRepository.findById(userId);
        if (userObj.isPresent()) {
            User user = userObj.get();
            logger.info("retrieving user: " + user.getId());
            userRepository.deleteById(userId);
            logger.info("deleted user: " + user.getId());
            return userId;
        } else {
            logger.info("deleting user: " + userId + " failed");
            throw new NotFoundException("user not found");
        }
    }

    @Override
    public String resetPassword(final ResetPasswordRequestDto resetPasswordRequestDto) {
        if (userRepository.existsByEmail(resetPasswordRequestDto.getEmail())) {
            logger.info("resetting password");
            Optional<User> userObj = userRepository.findByUsername(resetPasswordRequestDto.getUsername());
            if (userObj.isPresent()) {
                User user = userObj.get();
                logger.info("resetting password of user: " + user.getId());
                user.setPassword(encoder.encode(resetPasswordRequestDto.getPassword()));
                userRepository.save(user);
                return resetPasswordRequestDto.getPassword();
            } else {
                logger.info("user not found");
                throw new NotFoundException("user not found");
            }
        } else {
            logger.info("resetting user password failed");
            throw new NotFoundException("user not found");
        }
    }
}
