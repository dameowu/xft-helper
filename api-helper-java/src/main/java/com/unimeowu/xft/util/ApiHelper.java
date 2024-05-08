package com.unimeowu.xft.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.cmb.xft.open.api.BaseReqInf;
import com.cmb.xft.open.api.SM3Utils;
import com.cmb.xft.open.api.SignInf;

public class ApiHelper {

	private static Method apiGetSignInf;
	private static Method apiSm3withsm2Signature;
	private static BaseReqInf apiReqInf;
	public static boolean init(String companyId, String appId, String authSecret) {
		final String classSignInfName = "com.cmb.xft.open.api.XftOpenApiReqClient";
		final String classSm2SignatureUtilsName = "com.cmb.xft.open.api.Sm2SignatureUtils";
		try {
	        apiGetSignInf = Class.forName(classSignInfName).getDeclaredMethod("getSignInf", BaseReqInf.class, String.class, Map.class);
	        apiGetSignInf.setAccessible(true);
	        apiSm3withsm2Signature = Class.forName(classSm2SignatureUtilsName).getDeclaredMethod("sm3withsm2Signature", String.class, String.class);
	        apiSm3withsm2Signature.setAccessible(true);
	        
	        apiReqInf = new BaseReqInf(companyId, appId, authSecret);
	        return true;
		}
		catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static SignInf getSignInf(String url, Map<String, Object> queryParam)
	{
		try {
			return (SignInf) apiGetSignInf.invoke(null, apiReqInf, url, queryParam);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String sm3withsm2Signature(String privateKeyStr, String dataStr)
	{
		try {
			return (String) apiSm3withsm2Signature.invoke(null, privateKeyStr, dataStr);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static HttpInfo postInfo(String url, String queryParamJson, String requestBody) throws Exception
	{
		
		HttpInfo httpInfo = new HttpInfo();
		Map<String, Object> queryParam = null;
		if (null != queryParamJson) {
			queryParam = JSON.parseObject(queryParamJson, Map.class);
		}
		SignInf signInf = getSignInf(url, queryParam);
	    String digest = SM3Utils.sm3Signature2(requestBody);
	    String signStr = "POST " + signInf.getPath() + "\nx-alb-digest: " + requestBody + "\nx-alb-timestamp: " + signInf.getTimestamp();

	    String apisign = sm3withsm2Signature(apiReqInf.getAuthoritySecret(), signStr);
	    Map<String, String> headerMap = new HashMap<>();
	    headerMap.put("Content-Type", "application/json; charset=utf-8");
	    headerMap.put("appid", apiReqInf.getAppId());
	    headerMap.put("x-alb-timestamp", signInf.getTimestamp());
	    headerMap.put("x-alb-digest", digest);
	    headerMap.put("apisign", apisign);
	    headerMap.put("x-alb-verify", "sm3withsm2");
	    headerMap.put("Content-Type", "application/json; charset=UTF-8");
	    
	    httpInfo.setHeaders(headerMap);
	    httpInfo.setUrl(signInf.getUrl());
	    httpInfo.setBody(requestBody.getBytes("UTF-8"));
		
		return httpInfo;
	}
	
	
	
}
