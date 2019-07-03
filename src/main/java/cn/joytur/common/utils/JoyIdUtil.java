package cn.joytur.common.utils;

import java.util.Random;

import com.jfinal.kit.StrKit;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.Enums.OrderNoType;

/**
 * id生成获取
 * @author xuhang
 *
 */
public class JoyIdUtil extends cn.hutool.core.util.IdUtil {
	
	/** 自定义进制(0,1没有加入,容易与o,l混淆) */
	private static final char[] r = new char[] { 'q', 'w', 'e', '8', 'a', 's', '2', 'd', 'z', 'x', '9', 'c', '7', 'p',
			'5', 'i', 'k', '3', 'm', 'j', 'u', 'f', 'r', '4', 'v', 'y', 'l', 't', 'n', '6', 'b', 'g', 'h' };
	
	/** (不能与自定义进制有重复) */
	private static final char b = 'o';

	/** 进制长度 */
	private static final int binLen = r.length;

	/** 序列最小长度 */
	private static final int s = 4;
	
	/**
	 * 获取订单orderno
	 * @param orderNoType
	 * @return
	 */
	public static String getOrderNo(OrderNoType orderNoType) {
		String no = null;
		switch (orderNoType) {
		case RECHARGE_ORDER:
			no = "E";
			break;
		case RECOMMEND_ORDER:
			no = "D";
			break;
		case GOODS_ORDER:
			no = "G";
			break;
		default:
			break;
		}
		
		String orderNo = no + DateUtil.format(new java.util.Date(), "yyMMddHHmmssSSS");
		
		return orderNo;
	}
	
	/**
	 * 检查是否为充值订单
	 * @param orderNo
	 * @return
	 */
	public static boolean isRechargeOrder(String orderNo) {
		return StrKit.notBlank(orderNo) && orderNo.startsWith("E");
	}
	
	/**
	 * 检查是否为代理升级订单
	 * @param orderNo
	 * @return
	 */
	public static boolean isUpgradeOrder(String orderNo) {
		return StrKit.notBlank(orderNo) && orderNo.startsWith("D");
	}
	
	/**
	 * 获取邀请码
	 * @return
	 * 邀请码生成，算法原理：<br/>
	 * 1) 获取id: 1127738 <br/>
	 * 2) 使用自定义进制转为：gpm6 <br/>
	 * 3) 转为字符串，并在后面加'o'字符：gpm6o <br/>
	 * 4）在后面随机产生若干个随机数字字符：gpm6o7 <br/>
	 * 转为自定义进制后就不会出现o这个字符，然后在后面加个'o'，这样就能确定唯一性。最后在后面产生一些随机字符进行补全。<br/>
	 */
	public static String toSerialCode() {
		return toSerialCode(RandomUtil.randomLong(11));
	}
	
	/**
	 * 根据ID生成六位随机码
	 * 
	 * @param id ID
	 * @return 随机码
	 */
	public static String toSerialCode(long id) {
		char[] buf = new char[32];
		int charPos = 32;

		while ((id / binLen) > 0) {
			int ind = (int) (id % binLen);
			buf[--charPos] = r[ind];
			id /= binLen;
		}
		buf[--charPos] = r[(int) (id % binLen)];
		String str = new String(buf, charPos, (32 - charPos));
		// 不够长度的自动随机补全
		if (str.length() < s) {
			StringBuilder sb = new StringBuilder();
			sb.append(b);
			Random rnd = new Random();
			for (int i = 1; i < s - str.length(); i++) {
				sb.append(r[rnd.nextInt(binLen)]);
			}
			str += sb.toString();
		}
		return str;
	}

	public static long codeToId(String code) {
		char chs[] = code.toCharArray();
		long res = 0L;
		for (int i = 0; i < chs.length; i++) {
			int ind = 0;
			for (int j = 0; j < binLen; j++) {
				if (chs[i] == r[j]) {
					ind = j;
					break;
				}
			}
			if (chs[i] == b) {
				break;
			}
			if (i > 0) {
				res = res * binLen + ind;
			} else {
				res = ind;
			}
			// System.out.println(ind + "-->" + res);
		}
		return res;
	}
	
	/**
	 * 签名校验
	 * @return
	 */
	public static boolean signatureVerification(String params, String sign){
		if(params == null || params.isEmpty()) {
			return false;
		}
		
		return sign.equalsIgnoreCase(signatureEncrypt(params));
	}
	
	/**
	 * 签名加密
	 * @return
	 */
	public static String signatureEncrypt(String params){
		if(params == null || params.isEmpty()) {
			return null;
		}
		
		String key = JoyConfigUtil.getConfigValue(Enums.SysConfigType.MONITOR_APP_SIGN.name(), "");
		String encData = SecureUtil.md5(params+key);
		
		return encData;
	}
	
	public static String get15RandomText() {
		Random RANDOM = new Random();
        StringBuilder sb = new StringBuilder("e");
        sb.append(RANDOM.nextInt(9) + 1);
        for (int i = 0; i < 14; i++)
            sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }
	
}
