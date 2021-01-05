package com.mzp.haikang.face;

import cn.hutool.core.codec.Base64;

import com.mzp.haikang.commons.HCNetSDK;
import com.mzp.haikang.commons.HikCommonBusiness;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HikBusiness extends HikCommonBusiness {
    private static final Logger logger = LogManager.getLogger(HikBusiness.class);

    public long login() {// GEN-FIRST:event_jButtonLoginActionPerformed
        // 注册
        lUserID = hCNetSDK.NET_DVR_Login_V30(mDeviceIP, (short) mPort, mUsername, mPassword, m_strDeviceInfo);
        return lUserID.longValue();
    }

    public boolean logout() {
        // 注销
        return hCNetSDK.NET_DVR_Logout(lUserID);
    }

    public boolean cleanup() {// GEN-FIRST:event_exitMenuItemMouseClicked
        return hCNetSDK.NET_DVR_Cleanup();
    }

    public boolean setDVRMessageCallBack() {
        Pointer pUser = null;
        fMSFCallBack_V31 = new FMSGCallBack_V31();
        return hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser);
    }

    public int setupAlarmChan() {
        HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
        m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
        m_strAlarmInfo.byFaceAlarmDetection = 0;// 人脸侦测报警，设备支持人脸侦测功能的前提下，上传COMM_ALARM_FACE_DETECTION类型报警信息
        m_strAlarmInfo.write();
        lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
        return lAlarmHandle.intValue();
    }

    public boolean closeAlarmChan() {
        return hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle);
    }

    public void alarmDataHandle(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        String sAlarmType = new String();
        String[] newRow = new String[3];
        // 报警时间
        String[] sIP = new String[2];

        newRow[0] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // 报警类型
        newRow[1] = sAlarmType;
        // 报警设备IP地址
        sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
        newRow[2] = sIP[0];

        sAlarmType = new String("lCommand=") + lCommand.intValue();
        // lCommand是传的报警类型
        switch (lCommand.intValue()) {
            case HCNetSDK.COMM_VCA_ALARM: // 智能检测通用报警
                // TODO
                while (pAlarmInfo.getByte(0) != -1) ;


                break;

            case HCNetSDK.COMM_ALARM_FACE_DETECTION: // 人脸侦测报警信息
                logger.info("人脸侦测报警信息【" + newRow[0] + "】" + sIP[0] + "|" + sAlarmType);
                break;
            case HCNetSDK.COMM_UPLOAD_FACESNAP_RESULT: // 人脸抓拍报警信息
                HCNetSDK.NET_VCA_FACESNAP_RESULT struNetVCAFaceSnapResult = new HCNetSDK.NET_VCA_FACESNAP_RESULT();
                struNetVCAFaceSnapResult.write();
                Pointer pPlateInfo1 = struNetVCAFaceSnapResult.getPointer();
                pPlateInfo1.write(0, pAlarmInfo.getByteArray(0, struNetVCAFaceSnapResult.size()), 0,
                        struNetVCAFaceSnapResult.size());
                struNetVCAFaceSnapResult.read();
                logger.info("人脸抓拍报警信息【" + newRow[0] + "】" + newRow[2] + "|" + sAlarmType);

                // 保存抓拍场景图片
                if (struNetVCAFaceSnapResult.dwFacePicLen > 0 && struNetVCAFaceSnapResult.pBuffer2 != null) {
                    FileOutputStream fout = null;
                    try {

                        // 将字节写入文件
                        long offset = 0;
                        ByteBuffer buffers = struNetVCAFaceSnapResult.pBuffer1.getByteBuffer(offset,
                                struNetVCAFaceSnapResult.dwFacePicLen);
                        byte[] bytes = new byte[struNetVCAFaceSnapResult.dwFacePicLen];
                        buffers.rewind();
                        buffers.get(bytes);
                        String encode = Base64.encode(bytes);
                        System.out.println("base64-1===========" + encode);


                        offset = 0;
                        buffers = struNetVCAFaceSnapResult.pBuffer2.getByteBuffer(offset,
                                struNetVCAFaceSnapResult.dwBackgroundPicLen);
                        bytes = new byte[struNetVCAFaceSnapResult.dwBackgroundPicLen];
                        buffers.rewind();
                        buffers.get(bytes);
                        encode  = Base64.encode(bytes);

                        System.out.println("base64-2===========" + encode);

                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
                break;
        }
    }

    public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31 {
        // 报警信息回调函数
        public boolean invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen,
                              Pointer pUser) {
            alarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
            return true;
        }
    }

    public class FMSGCallBack implements HCNetSDK.FMSGCallBack {
        // 报警信息回调函数
        public void invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen,
                           Pointer pUser) {
            alarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        }
    }
}
