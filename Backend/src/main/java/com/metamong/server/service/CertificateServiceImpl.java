package com.metamong.server.service;

import com.metamong.server.dto.EducationDto;
import com.metamong.server.dto.RankDto;
import com.metamong.server.entity.Certificate;
import com.metamong.server.entity.Education;
import com.metamong.server.exception.ApplicationException;
import com.metamong.server.repository.CertificateRepository;
import com.metamong.server.repository.EducationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CertificateServiceImpl implements CertificateService{

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private EducationRepository educationRepository;

    public EducationDto.EduResponse getCertificate(String education, int userId){
        Optional<Education> edu = educationRepository.findByEducation(education);

        Optional<Certificate> certificate = certificateRepository.findByEducationAndUserId(edu.get(), userId);
        if(!certificate.isPresent()) throw new ApplicationException(HttpStatus.valueOf(404), "There is no certification");

        EducationDto.EduResponse eduResponse = new EducationDto.EduResponse();
        eduResponse.setNickname(certificate.get().getUser().getNickname());
        eduResponse.setEducation(certificate.get().getEducation().getEducation());
        eduResponse.setCreateAt(certificate.get().getCreateAt());

        return eduResponse;
    }

    public RankDto.ResponseList getRank(String education){

        Optional<Education> edu = educationRepository.findByEducation(education);

        List<Certificate> certificateList = certificateRepository.findAllByEducation(edu.get());

        List<RankDto> rankDtoList = new ArrayList<>();
        for (Certificate certificate : certificateList) {
            if(certificate.getPassTime() == null) continue;         // 교육만 들은 상태는 배제
            RankDto rankDto = RankDto.builder()
                    .nickname(certificate.getUser().getNickname())
                    .passTime(certificate.getPassTime())
                    .createAt(certificate.getCreateAt())
                    .build();

            rankDtoList.add(rankDto);
        }
        Collections.sort(rankDtoList, (o1, o2) -> {
            return o1.getPassTime() - o2.getPassTime();
        });
        rankDtoList = rankDtoList.subList(0, Math.min(5, rankDtoList.size()));
        RankDto.ResponseList responseList = new RankDto.ResponseList();
        responseList.setData(rankDtoList);

        return responseList;
    }

    @Override
    public void setMissionClearTime(int userId, int unityTime, String education) {
        Optional<Education> edu = educationRepository.findByEducation(education);
        System.out.println(education);
        Optional<Certificate> certificate = certificateRepository.findByEducationAndUserId(edu.get(), userId);

        certificate.ifPresent(select -> {
        	if(select.getPassTime()!=null && select.getPassTime()> unityTime) {
        		select.setPassTime(Math.min(select.getPassTime(), unityTime));
        		select.setCreateAt(new Date());
        		select.setAuthenticated(true);
        		certificateRepository.save(select);
        	}else if(select.getPassTime()==null){
        		select.setPassTime(unityTime);
        		select.setCreateAt(new Date());
        		select.setAuthenticated(true);
        		certificateRepository.save(select);
        	}
            
        });
    }
}
