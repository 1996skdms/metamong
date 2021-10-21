package com.metamong.server.service;

import com.metamong.server.dto.UserDto;
import com.metamong.server.dto.encode.Encoder;
import com.metamong.server.entity.Characters;
import com.metamong.server.entity.User;
import com.metamong.server.repository.CharactersRepository;
import com.metamong.server.repository.UserRepository;
import org.omg.CORBA.portable.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CharactersRepository charactersRepository;

    private final String ENCODE_ID = "bcrypt";
    private static final Map<String, PasswordEncoder> encoders = Encoder.getEncoder();      // 인코더 : 여러 타입의 암호화 방식을 저장

    @Override
    public int register(UserDto.RegisterRequest registerInfo) {
        PasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(ENCODE_ID, encoders);
        String encPassword = passwordEncoder.encode(registerInfo.getPassword());
        // 암호화 확인 작업
        //if(!passwordEncoder.matches(registerInfo.getPassword(), encPassword)) throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "비밀번호 암호화 중 불일치 오류");
        User user = new User(registerInfo.getEmail(), encPassword, registerInfo.getName(), registerInfo.getNickname());

        return userRepository.save(user).getId();
    }


    @Override
    public boolean isExistEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();
    }

    @Override
    public boolean isExistNickname(String nickname) {
        Optional<User> user = userRepository.findByNickname(nickname);
        return user.isPresent();
    }

    @Override
    public UserDto.Response login(UserDto.LoginRequest loginReq) {

        return null;
    }

    @Override
    public UserDto.Response login(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        UserDto.Response res = new UserDto.Response();

        if(user!=null) {
            res.setId(user.getId());
            res.setEmail(user.getEmail());
            res.setNickname(user.getNickname());
        }

        return res;
    }

    @Override
    public void kakaoRegister(String email, String name){
        User user = new User();

        user.setEmail(email);
        user.setName(name);
        user.setPassword("0");  // 비밀번호 사용X
        userRepository.save(user);

        // Auto increment Id 값 가져와서 닉네임 초기설정해줌
        user.setNickname("회원"+userRepository.findByEmail(email).get().getId());
        userRepository.save(user);
    }

    @Override
    public String TokenGeneration(int userId, String receiverEmail, String url) {

        return null;
    }

    @Override
    public UserDto.userInfoResponse getUserInfo(String nickname) {
        User user = userRepository.findByNickname(nickname).orElse(null);
        UserDto.userInfoResponse res = new UserDto.userInfoResponse();

        if(user!=null) {
            res.setName(user.getName());
            res.setEmail(user.getEmail());
            res.setNickname(user.getNickname());
        }

        return res;
    }

    @Override
    public void setCharacter(int userId, int character) {
        // token에 저장된 id
        User user = userRepository.findById(userId).orElse(null);
        Characters characters = charactersRepository.findById(character).orElse(null);

        user.setCharacter(characters);
        userRepository.save(user);
    }

    @Override
    public UserDto.characterResponse getCharacter(int userId) {
        // token에 저장된 id
        User user = userRepository.findById(userId).orElse(null);
        Characters characters = charactersRepository.findById(user.getCharacter().getId()).orElse(null);

        UserDto.characterResponse res = new UserDto.characterResponse();

        if(characters!=null) {
            res.setFileUrl(characters.getFileUrl());
            res.setName(characters.getName());
        }

        return res;
    }

    @Override
    public UserDto.allCharactersResponse getAllCharacter() {
        UserDto.allCharactersResponse res = new UserDto.allCharactersResponse();
        res.setList(charactersRepository.findAll());

        return res;
    }
}
