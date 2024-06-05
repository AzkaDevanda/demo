package com.techmarket.demo.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techmarket.demo.config.MessageString;
import com.techmarket.demo.dto.ResponseDto;
import com.techmarket.demo.dto.user.SignInDto;
import com.techmarket.demo.dto.user.SignInResponseDto;
import com.techmarket.demo.dto.user.SignUpDto;
import com.techmarket.demo.entity.AuthenticationToken;
import com.techmarket.demo.entity.User;
import com.techmarket.demo.enums.ResponseStatus;
import com.techmarket.demo.enums.Role;
import com.techmarket.demo.exception.AuthenticationFailException;
import com.techmarket.demo.exception.CustomeException;
import com.techmarket.demo.repository.UserRepository;
import com.techmarket.demo.utils.Helper;

@Service
public class UserService {
    
    public static final String USER_CREATED = "user created successfully";

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationService authenticationService;

    Logger logger = LoggerFactory.getLogger(UserService.class);

    public ResponseDto signUp(SignUpDto signUpDto) throws CustomeException{

        // check to see if the current email address has already been registered.
        if(Helper.notNull(userRepository.findByEmail(signUpDto.getEmail()))){

            // if the email address has been registered then throw an exception.
            throw new CustomeException("User already exist");
        }

        // first encrypt the password
        String encryptedPassword = signUpDto.getPassword();


        User user = new User(signUpDto.getFirstName(), signUpDto.getLastName(), signUpDto.getEmail(), Role.user, encryptedPassword);

        User createdUser;
        try{
            // save the user
            createdUser = userRepository.save(user);
            // generate token for user
            final AuthenticationToken authenticationToken = new AuthenticationToken(createdUser);
            // save token in database
            authenticationService.saveConfirmationToken(authenticationToken);
            // succes in creaating
            return new ResponseDto(ResponseStatus.succes.toString(), USER_CREATED);
        }catch(Exception e){
            // handle signUp error
            throw new CustomeException(e.getMessage());
        }
    }

    public SignInResponseDto signIn(SignInDto signInDto) throws CustomeException{

        // first find user by email
        User user = userRepository.findByEmail(signInDto.getEmail());
        if(Helper.notNull(user)){
            throw  new AuthenticationFailException("user not present");
        }

        // check if the password is right
        if(!user.getPassword().equals(signInDto.getPassword())){
            throw new AuthenticationFailException(MessageString.WRONG_PASSWORD);
        }

        AuthenticationToken token = authenticationService.getToken(user);

        if(Helper.notNull(token)){
            // token not present
            throw new CustomeException("token not present");
        }

        return new SignInResponseDto("Succes", token.getToken());
    }
}
