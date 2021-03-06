package com.metamong.server.service;

import com.google.firebase.messaging.*;
import com.metamong.server.dto.FcmMessage;
import com.metamong.server.dto.UserDto;
import com.metamong.server.dto.UserDto.Response;
import com.metamong.server.entity.FirebaseToken;
import com.metamong.server.entity.User;
import com.metamong.server.repository.FirebaseTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

//import okhttp3;

@Component
@RequiredArgsConstructor
public class FirebaseCloudMessageServiceImpl implements FirebaseCloudMessageService{
    
    @Autowired
	private FirebaseTokenRepository firebaseTokenRepository;
    
    @Override
    public void sends(List<FirebaseToken> tokens, String nickname, String title, String body) throws InterruptedException, IOException, FirebaseMessagingException {
        if(tokens.size() == 0) return;
        
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(null)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build())
                        .data(FcmMessage.Data.builder()
                            .nickname(nickname)
                            .build())
                        .build()
                )
                .validate_only(false)
                .build();
        
        MulticastMessage multicastMessage = makeMulticastMessage(tokens,fcmMessage);

        BatchResponse response = FirebaseMessaging.getInstance()
                .sendMulticast(multicastMessage);


        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            List<String> failedTokens = new ArrayList<>();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    System.out.println(responses.get(i).getException());
                    failedTokens.add(tokens.get(i).getToken());
                }
            }
        }
    }
    
    private MulticastMessage makeMulticastMessage(List<FirebaseToken> tokens, FcmMessage fcm){
        List<String> tokenList = new ArrayList<>();
        for(FirebaseToken token : tokens) tokenList.add(token.getToken());
        
        return MulticastMessage.builder()
                .setNotification(new Notification(fcm.getMessage().getNotification().getTitle(), fcm.getMessage().getNotification().getBody()))
                .putData("nickname", String.valueOf(fcm.getMessage().getData().getNickname() ))
                .addAllTokens(tokenList)
                .build();
    }

    /**
     * ????????? ??? DB??? Firebase ?????? ?????? ??????
     * @param myRes : ?????? ??????
     * @param token : Firebase ?????? ??????
     */
    @Override
    @Transactional
    public void save(UserDto.LoginRes loginRes, String token) {
        Optional<List<FirebaseToken>> firebaseTokens = firebaseTokenRepository.findByUserIdAndToken(loginRes.getId(), token);
        if(firebaseTokens.isPresent() && firebaseTokens.get().size() >= 1) return;

        User user = new User();
        user.setId(loginRes.getId());
        FirebaseToken firebaseToken = FirebaseToken.builder()
                .user(user)
                .token(token)
                .createAt(Date.from(LocalDateTime.now().atZone(ZoneId.of("+9")).toInstant()))
                .build();

        firebaseTokenRepository.save(firebaseToken);
    }
    
    
    @Override
    @Transactional
	public void save(Response loginRes, String token) {
    	Optional<List<FirebaseToken>> firebaseTokens = firebaseTokenRepository.findByUserIdAndToken(loginRes.getId(), token);
        if(firebaseTokens.isPresent() && firebaseTokens.get().size() >= 1) return;

        User user = new User();
        user.setId(loginRes.getId());
        FirebaseToken firebaseToken = FirebaseToken.builder()
                .user(user)
                .token(token)
                .createAt(Date.from(LocalDateTime.now().atZone(ZoneId.of("+9")).toInstant()))
                .build();

        firebaseTokenRepository.save(firebaseToken);
		
	}

    
    /**
     * ?????? ????????? Firebase Auth Token ??? ???????????? -> ???????????? ?????? ??????????????? ???????????? ??? ?????? ???????????? ??????
     * @param userId : ?????? ID
     * @return : Firebase ?????? ??????
     */
    @Override
    public List<FirebaseToken> getUserToken(int userId) {
        Optional<List<FirebaseToken>> firebaseTokens = firebaseTokenRepository.findByUserId(userId);

        return firebaseTokens.orElse(null);
    }

    

    /**
     * ???????????? ??? ?????? ???????????? ?????? Firebase ?????? DB?????? ??????
     * @param token : ??????
     */
    @Override
    @Transactional
    public void del(String token) {
        Optional<List<FirebaseToken>> firebaseTokens = firebaseTokenRepository.findByToken(token);

        firebaseTokens.ifPresent(select -> {
            firebaseTokenRepository.deleteAll(select);
        });
    }

    /**
     * ?????? ???????????? Trash Firebase Token ?????? ???????????? (?????? ??????)
     */
    @Override
    @Transactional
    public void deleteLastDay() {
        Optional<List<FirebaseToken>> firebaseTokens = firebaseTokenRepository.findByCreateAtBefore(LocalDateTime.now(ZoneId.of("+9")).minusDays(1));

        firebaseTokens.ifPresent(select ->{
            firebaseTokenRepository.deleteAll(firebaseTokens.get());
        });
    }
    
}