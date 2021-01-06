package com.mzp.haikang.commons;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mzp.haikang.dao.ConfigMapper;
import com.mzp.haikang.face.HikBusiness;
import com.mzp.haikang.model.Config;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author mzp
 * @version 1.0
 * @
 * @date 2021/1/6 16:38
 */
public class CreateFaceData {
    private static final Logger logger = LogManager.getLogger(CreateFaceData.class);

    @Autowired
    ConfigMapper configMapper;
    private static ConfigMapper myUtil;

    @PostConstruct
    public void init() {
        myUtil = configMapper;
    }

    public static String getFaceData(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser){

        JSONObject jsonObject = new JSONObject();
        JSONObject FaceListObject = new JSONObject();
        JSONArray FaceObject = new JSONArray();
        JSONObject FaceObjectData = new JSONObject();

        jsonObject.put("FaceListObject",FaceListObject);
        FaceListObject.put("FaceObject",FaceObject);
        FaceObject.add(FaceObjectData);

        String sAlarmType = new String();
        String[] newRow = new String[3];
        // 报警时间
        String[] sIP = new String[2];

        newRow[0] = new SimpleDateFormat("yyyyMMddHH:mm:ss").format(new Date());
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
            case HCNetSDK.COMM_UPLOAD_FACESNAP_RESULT: // 人脸抓拍报警信息
                HCNetSDK.NET_VCA_FACESNAP_RESULT struNetVCAFaceSnapResult = new HCNetSDK.NET_VCA_FACESNAP_RESULT();
                struNetVCAFaceSnapResult.write();

                //人脸ID
                FaceObjectData.put("FaceID",struNetVCAFaceSnapResult.dwFacePicID);
                FaceObjectData.put("FaceID",struNetVCAFaceSnapResult.dwFacePicID);

                /**
                 * 性别
                 * 1-男 2-女
                 */
                byte bySex = struNetVCAFaceSnapResult.struFeature.bySex;
                Integer GenderCode = Integer.valueOf(bySex);
                FaceObjectData.put("GenderCode",GenderCode);


                /**
                 * 抓拍时间
                 */
                FaceObjectData.put("FaceAppearTime",newRow[0]);

                /**
                 * 设备编号
                 */
                Config configs = myUtil.selectByPrimaryKey(newRow[2]);
                FaceObjectData.put("DeviceID",configs.getDeviceid());


                int dwFacePicID = struNetVCAFaceSnapResult.dwFacePicID;
                String s = String.valueOf(dwFacePicID);
                String substring = s.substring(0, 40);
                FaceObjectData.put("SourceID",substring);


                /**
                 * 是否受害者
                 */
                FaceObjectData.put("IsVictim",2);


                /**
                 * 人脸坐标
                 */
                HCNetSDK.NET_VCA_RECT struRect = struNetVCAFaceSnapResult.struRect;

                FaceObjectData.put("LeftTopY",struRect.fX);
                FaceObjectData.put("LeftTopX",struRect.fY);
                FaceObjectData.put("RightBtmY",struRect.fHeight);
                FaceObjectData.put("RightBtmX",struRect.fWidth);

                /**
                 * 是否涉嫌恐怖分子
                 */
                FaceObjectData.put("IsSuspectedTerrorist",2);

                /**
                 * 面部表情出现时间
                 */
                FaceObjectData.put("FaceDisAppearTime",newRow[0]);

                /**
                 * 是否是司机
                 */
                FaceObjectData.put("IsDriver",2);

                /**
                 * 是否是外国人
                 */
                FaceObjectData.put("IsForeigner",2);
                /**
                 * 信息种类
                 */
                FaceObjectData.put("InfoKind",1);

                /**
                 * 涉及犯罪否
                 */
                FaceObjectData.put("IsCriminalInvolved",2);

                /**
                 * 时间
                 */
                FaceObjectData.put("ShotTime",newRow[0]);

                /**
                 * 拘留否
                 */
                FaceObjectData.put("IsDetainees",2);

                /**
                 * 可疑人否
                 */
                FaceObjectData.put("IsDetainees",2);


                FaceObjectData.put("LocationMarkTime",newRow[0]);


                JSONObject SubImageList = new JSONObject();
                FaceObjectData.put("SubImageList",SubImageList);

                JSONArray SubImageInfoObject = new JSONArray();
                SubImageList.put("SubImageInfoObject",SubImageInfoObject);




                Pointer pPlateInfo1 = struNetVCAFaceSnapResult.getPointer();
                pPlateInfo1.write(0, pAlarmInfo.getByteArray(0, struNetVCAFaceSnapResult.size()), 0,
                        struNetVCAFaceSnapResult.size());
                struNetVCAFaceSnapResult.read();
                logger.info("人脸抓拍报警信息【" + jsonObject.toJSONString() + "】" + newRow[2] + "|" + sAlarmType);

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
                        JSONObject jsonObject1 = new JSONObject();
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                        BufferedImage read = ImgUtil.read(byteArrayInputStream);
                        int height = read.getHeight();
                        int width = read.getWidth();
                        jsonObject1.put("Type",14);
                        jsonObject1.put("DeviceID",configs.getDeviceid());
                        jsonObject1.put("ImageID",substring);
                        jsonObject1.put("EventSort",1);
                        jsonObject1.put("ShotTime",newRow[0]);
                        jsonObject1.put("Height",height);
                        jsonObject1.put("Data",encode);
                        jsonObject1.put("FileFormat","Jpeg");
                        jsonObject1.put("Width",width);
                        SubImageInfoObject.add(jsonObject1);



                        offset = 0;
                        buffers = struNetVCAFaceSnapResult.pBuffer2.getByteBuffer(offset,
                                struNetVCAFaceSnapResult.dwBackgroundPicLen);
                        bytes = new byte[struNetVCAFaceSnapResult.dwBackgroundPicLen];
                        buffers.rewind();
                        buffers.get(bytes);
                        encode  = Base64.encode(bytes);

                        System.out.println("base64-2===========" + encode);
                        JSONObject jsonObject2 = new JSONObject();
                        byteArrayInputStream = new ByteArrayInputStream(bytes);
                        read = ImgUtil.read(byteArrayInputStream);
                        height = read.getHeight();
                        width = read.getWidth();
                        jsonObject2.put("Type",11);
                        jsonObject2.put("DeviceID",configs.getDeviceid());
                        jsonObject2.put("ImageID",substring);
                        jsonObject2.put("EventSort",1);
                        jsonObject2.put("ShotTime",newRow[0]);
                        jsonObject2.put("Height",height);
                        jsonObject2.put("Data",encode);
                        jsonObject2.put("FileFormat","Jpeg");
                        jsonObject2.put("Width",width);
                        SubImageInfoObject.add(jsonObject1);

                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
                break;
        }







        return null;
    }

    /**
     * {
     *     "FaceListObject": {
     *         "FaceObject": [{
     *                 "FaceID": "110108010013200013880220201209161120000010600001",
     *                 "GenderCode": "2",
     *                 "FaceAppearTime": "20201209161120",
     *                 "DeviceID": "11010801001320001388",
     *                 "SourceID": "11010801001320001388022020120916112000001",
     *                 "IsVictim": 2,
     *                 "SubImageList": {
     *                     "SubImageInfoObject": [{
     *                             "Type": "14",
     *                             "StoragePath": "",
     *                             "DeviceID": "11010801001320001388",
     *                             "ImageID": "11010801001320001388022020120916112000001",
     *                             "EventSort": 1,
     *                             "ShotTime": "20201209161120",
     *                             "Height": 2160,
     *                             "FileFormat": "Jpeg",
     *                             "Width": 3840
     *                         }, {
     *                             "Type": "11",
     *                             "StoragePath": "",
     *                             "DeviceID": "11010801001320001388",
     *                             "ImageID": "11010801001320001388022020120916112000001",
     *                             "EventSort": 1,
     *                             "ShotTime": "20201209161120",
     *                             "Height": 197,
     *                             "FileFormat": "Jpeg",
     *                             "Width": 197
     *                         }
     *                     ]
     *                 },
     *                 "LeftTopY": 177,
     *                 "LeftTopX": 2068,
     *                 "IsSuspectedTerrorist": 2,
     *                 "FaceDisAppearTime": "20201209161120",
     *                 "IsDriver": 2,
     *                 "IsForeigner": 2,
     *                 "InfoKind": 1,
     *                 "RightBtmY": 374,
     *                 "IsCriminalInvolved": 2,
     *                 "RightBtmX": 2266,
     *                 "ShotTime": "2020120900164532",
     *                 "IsDetainees": 2,
     *                 "IsSuspiciousPerson": 2,
     *                 "LocationMarkTime": "20201209161120"
     *             }
     *         ]
     *     }
     * }
     */
}
