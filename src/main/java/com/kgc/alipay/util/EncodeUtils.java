package com.kgc.alipay.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 类功能说明：编码通用类EncodeUtils
 * 
 * @author zhukang
 * @version V1.0
 */
public class EncodeUtils {

	/**
	 * 函数功能: MD5签名
	 * 
	 * @param myinfo
	 *            String
	 * @return String
	 */
	public static String testDigest(String myinfo) {
		byte[] digesta;
		try {
			MessageDigest alga = MessageDigest.getInstance("MD5");
			alga.update(myinfo.getBytes("gbk"));
			digesta = alga.digest();
		} catch (UnsupportedEncodingException e) {
			System.out.println("MD5加密异常:" + e.getMessage());
			return null;
		} catch (NoSuchAlgorithmException ex) {
			System.out.println("MD5加密异常:" + ex.getMessage());
			return null;
		}
		return byte2hex(digesta);
	}

	/**
	 * 函数功能: MD5签名
	 * 
	 * @param myinfo
	 *            String
	 * @param encode
	 *            String
	 * @return String
	 */
	public static String testDigest(String myinfo, String encode) throws UnsupportedEncodingException {
		byte[] digesta;
		try {
			MessageDigest alga = MessageDigest.getInstance("MD5");
			alga.update(myinfo.getBytes(encode));
			digesta = alga.digest();

		} catch (NoSuchAlgorithmException ex) {
			System.out.println("MD5加密异常:" + ex.getMessage());
			return null;
		}
		return byte2hex(digesta);
	}

	/**
	 * 函数功能: 二进制转为十六进制串
	 * 
	 * @param b
	 *            byte[]
	 * @return String
	 */
	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp;
		for (byte bb : b) {
			stmp = (Integer.toHexString(bb & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs;
	}
	
	public static void main(String[] args) throws Exception {
		// MD5加密，特点：不可逆，只能加密，不能解密，通常使用在用户密码上（加密没法解密，别人拿到加密后的密码没用）
		// 用在密码字段后，下次登录校验，把接收的密码也MD5加密，跟数据库存的密码对比，一致就是正确的
		// 密码规则：强度比较高：包含小写，大写，特殊字符和数字
		// xyxxyX@123 -- 自己好记忆的规则，且强度要高
		String str = "zhukangzK_198811";
		System.out.println(EncodeUtils.testDigest(str));
	}

}
