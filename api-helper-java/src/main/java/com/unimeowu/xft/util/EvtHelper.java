package com.unimeowu.xft.util;

import org.bouncycastle.asn1.ASN1EncodableVector;  
import org.bouncycastle.asn1.ASN1Integer;  
import org.bouncycastle.asn1.DERSequence;  
import org.bouncycastle.crypto.params.ECDomainParameters;  
import org.bouncycastle.crypto.params.ECPublicKeyParameters;  
import org.bouncycastle.crypto.params.ParametersWithID;  
import org.bouncycastle.crypto.signers.SM2Signer;  
import org.bouncycastle.jce.ECNamedCurveTable;  
import org.bouncycastle.jce.spec.ECParameterSpec;  
import org.bouncycastle.math.ec.ECPoint;  
import org.bouncycastle.util.encoders.Hex;  
import org.springframework.util.Assert;  
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cmb.xft.open.api.Sm4Util;

import java.io.IOException;  
import java.math.BigInteger;  
import java.util.HashMap;  
import java.util.Map;  
import java.util.TreeMap;

/**  
 * @Author:yyf  
 * @Descriptions: verifyAndProcess：验证签名并返回与原有相同的参数  
 * verify：该方法只提供参数签名校验，不解析原有参数  
 */  
public class EvtHelper {

    private static final String SIGNATURE = "signature";

    private static String pubKey = null;
    private static String decodeKey = null; 
    
    public static void init(String pubKey) {
    	EvtHelper.pubKey = pubKey;
    	EvtHelper.decodeKey =  pubKey.substring(0, 32);
    }
    /**  
     * 国密网关body签名验签  
     */  
    public static boolean verify(String publicKeyStr, String msgStr, String signatureStr) throws Exception {


        byte[] pubkey = Hex.decode(publicKeyStr);  
        byte[] msg = msgStr.getBytes();  
        byte[] signature = Hex.decode(signatureStr);  
        if (signature.length != 64) {  
            throw new Exception("OGSM201"+ signatureStr + "签名格式不对");  
        }  
        return verify(pubkey, msg, signature);  
    }

    /**  
     * 国密签名验签，并且解析参数  
     * @param publicKeyStr 开放平台中生成的公钥  
     * @param msgStr     回调接口接收到的参数  
     * @return 如果解签失败会抛出失败验签失败异常；验签成功，返回去除签名之后的字符串  
     */  
    public static String verifyAndProcess(String publicKeyStr, String msgStr) {  
        Assert.isTrue(!StringUtils.isEmpty(publicKeyStr), "解签公钥不能为空");  
        Assert.isTrue(!StringUtils.isEmpty(msgStr), "密文不能为空");

        Map<String, Object> msgData = JSONObject.parseObject(msgStr, HashMap.class);  
        Assert.notNull(msgData.get(SIGNATURE), "消息签名为空，请确认参数是否经过加签！");  
        Assert.isTrue(verify(publicKeyStr, msgStr), "签名验证失败！");  
        msgData.remove(SIGNATURE);  
        return JSONObject.toJSONString(msgData);  
    }

    /**  
     * 国密签名验签  
     * @param publicKeyStr 开放平台中生成的公钥  
     * @param msgStr     回调接口接收到的参数  
     * @return 验签是否成功  
     */  
    public static boolean verify(String publicKeyStr, String msgStr) {  
        try {  
            Assert.isTrue(!StringUtils.isEmpty(publicKeyStr), "解签公钥不能为空");  
            Assert.isTrue(!StringUtils.isEmpty(msgStr), "接口参数不能为空");  
            Map<String, Object> msgData = JSONObject.parseObject(msgStr, HashMap.class);  
            Assert.notNull(msgData.get(SIGNATURE), "消息签名为空，请确认参数是否经过加签！");  
            String signatureStr = String.valueOf(msgData.remove(SIGNATURE));  
            String param = JSONObject.toJSONString(new TreeMap<>(msgData));  
            if (publicKeyStr.length() != 130) {  
                return false;  
            }  
            byte[] pubkey = Hex.decode(publicKeyStr);  
            byte[] msg = param.getBytes();  
            byte[] signature = Hex.decode(signatureStr);  
            return verify(pubkey, msg, signature);  
        } catch (Exception e) {  
            return false;  
        }  
    }


    private static boolean verify(byte[] pubkey, byte[] msg, byte[] signature) throws IOException {  
        if (signature.length != 64) {  
            return false;  
        }  
        ECPublicKeyParameters publicKey = encodePublicKey(pubkey);  
        SM2Signer signer = new SM2Signer();  
        ParametersWithID parameters = new ParametersWithID(publicKey, "1234567812345678".getBytes());  
        signer.init(false, parameters);  
        signer.update(msg, 0, msg.length);  
        return signer.verifySignature(encodeDERSignature(signature));  
    }

    private static byte[] encodeDERSignature(byte[] signature) throws IOException {  
        byte[] r = new byte[32];  
        byte[] s = new byte[32];  
        System.arraycopy(signature, 0, r, 0, 32);  
        System.arraycopy(signature, 32, s, 0, 32);  
        ASN1EncodableVector vector = new ASN1EncodableVector();  
        vector.add(new ASN1Integer(new BigInteger(1, r)));  
        vector.add(new ASN1Integer(new BigInteger(1, s)));

        return (new DERSequence(vector)).getEncoded();  
    }

    private static ECPublicKeyParameters encodePublicKey(byte[] value) {  
        byte[] x = new byte[32];  
        byte[] y = new byte[32];  
        System.arraycopy(value, 1, x, 0, 32);  
        System.arraycopy(value, 33, y, 0, 32);  
        BigInteger xValue = new BigInteger(1, x);  
        BigInteger yValue = new BigInteger(1, y);  
        ECPoint qValue = ECNamedCurveTable.getParameterSpec("sm2p256v1").getCurve().createPoint(xValue, yValue);  
        return new ECPublicKeyParameters(qValue, getECDomainParameters());  
    }

    private static ECDomainParameters getECDomainParameters() {  
        ECParameterSpec spec = ECNamedCurveTable.getParameterSpec("sm2p256v1");  
        return new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN(), spec.getH(), spec.getSeed());  
    }
    
    public static boolean verify(String eventId, String prjCod, String eventTime, long eventCd, String signature) {
    	final String FIELD_EVENT_ID = "eventId";
        final String FIELD_PRJ_COD = "prjCod";
        final String FIELD_EVENT_TIME = "eventTime";
        final String FIELD_EVENT_CD = "eventCd";
    	try {
    		JSONObject dataSignJson = new JSONObject();
			dataSignJson.put(FIELD_EVENT_ID, eventId);
			dataSignJson.put(FIELD_PRJ_COD, prjCod);
			dataSignJson.put(FIELD_EVENT_TIME, eventTime);
			dataSignJson.put(FIELD_EVENT_CD, eventCd);;

            return verify(pubKey, dataSignJson.toJSONString(), signature);
    	}
    	catch(Exception e) {
    		return false;
    	}
    }
    public static String decode(String bodyStr) throws Exception {
    	return Sm4Util.decryptEcb(decodeKey, bodyStr);
    }
    
}  
