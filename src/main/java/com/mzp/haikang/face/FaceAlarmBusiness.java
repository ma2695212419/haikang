package com.mzp.haikang.face;


import com.mzp.haikang.commons.AlarmBusiness;
import com.mzp.haikang.commons.Constants;
import com.mzp.haikang.commons.HikBean;
import com.mzp.haikang.commons.HikCache;
import com.mzp.haikang.dao.ConfigMapper;
import com.mzp.haikang.model.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class FaceAlarmBusiness extends AlarmBusiness {

    @Autowired
    ConfigMapper configMapper;
    private static ConfigMapper myUtil;

    private static final Logger logger = LogManager.getLogger(FaceAlarmBusiness.class);
    HikBusiness hikBusiness = new HikBusiness();

    @PostConstruct
    public void init() {
        myUtil = configMapper;
    }

    public boolean open(String ip, HikBean hikBean) {
        if (!hikBusiness.init(ip, hikBean)) {
            logger.error("初始化失败");
            return false;
        }
        logger.info("初始化成功");
        if (hikBusiness.login() == -1) {
            logger.error("登录失败");
            return false;
        }
        logger.info("登录成功");
        if (!hikBusiness.setDVRMessageCallBack()) {
            logger.error("设置回调失败");
            return false;
        }
        logger.info("设置回调成功");
        if (hikBusiness.setupAlarmChan() == -1) {
            logger.error("设置布防失败");
            return false;
        }
        logger.info("设置布防成功");
        return true;
    }

    public boolean close(String ip) {
        if (!HikCache.hikBusinessMap.containsKey(ip)) {
            logger.error("设备未开启, ip:" + ip);
            return false;
        }
        hikBusiness = (HikBusiness) HikCache.hikBusinessMap.get(ip);
        if (hikBusiness.lAlarmHandle.intValue() > -1 && !hikBusiness.closeAlarmChan()) {
            logger.error("撤防失敗");
            return false;
        }
        logger.info("撤防成功");
        if (!hikBusiness.logout()) {
            logger.error("登出失敗");
            return false;
        }
        logger.info("登出成功");
        if (!hikBusiness.cleanup()) {
            logger.error("释放失敗");
            return false;
        }
        logger.info("释放成功");
        return true;
    }

    public static void startProgress() {

        List<Config> configs = myUtil.selectAll();
        for (Config config : configs) {
            System.out.println(config.getIp());
            HikBean hikBean = new HikBean();
            hikBean.setUsername(config.getHikusername());
            hikBean.setPassword(config.getHikpassword());
            hikBean.setPort(Integer.parseInt(config.getHikport()));
            hikBean.setAuthorization("token 123");
            new FaceDetectionThread(config.getIp(), hikBean, Constants.ACTION_TYPE_OPEN).start();

        }
    }

    public static void endProgress() {
        for (int i = 0; i < HikCache.ipList.length; i++) {
            new FaceDetectionThread(HikCache.ipList[i], HikCache.mHikBean, Constants.ACTION_TYPE_CLOSE).start();
        }
    }
}
