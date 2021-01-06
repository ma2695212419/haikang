package com.mzp.haikang;

import com.mzp.haikang.face.FaceAlarmBusiness;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HaikangsdkjApplication {

    public static void main(String[] args) {
        SpringApplication.run(HaikangsdkjApplication.class, args);
//        FaceAlarmBusiness.load();
        FaceAlarmBusiness.startProgress();

    }

}
