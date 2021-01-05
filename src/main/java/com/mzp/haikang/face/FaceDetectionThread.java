package com.mzp.haikang.face;


import com.mzp.haikang.commons.Constants;
import com.mzp.haikang.commons.HikBean;
import com.mzp.haikang.commons.HikCache;

public class FaceDetectionThread extends Thread {
    private String ip;
    private HikBean mHikBean;
    private int actionType;

    public FaceDetectionThread(String ip, HikBean hikBean, int actionType) {
        this.ip = ip;
        this.mHikBean = hikBean;
        this.actionType = actionType;
    }

    public void run() {
        FaceAlarmBusiness faceAlarmBusiness = new FaceAlarmBusiness();
        switch (actionType) {
            case Constants.ACTION_TYPE_OPEN:
                while (!HikCache.hikBusinessMap.containsKey(ip)) {
                    if (faceAlarmBusiness.open(ip, mHikBean)) {
                        HikCache.hikBusinessMap.put(ip, faceAlarmBusiness.hikBusiness);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Constants.ACTION_TYPE_CLOSE:
                faceAlarmBusiness.close(ip);
                break;
            default:
                break;
        }
    }
}