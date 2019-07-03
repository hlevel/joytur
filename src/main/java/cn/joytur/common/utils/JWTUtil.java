package cn.joytur.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;

import cn.joytur.common.exception.NoAuthorizationException;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.dto.AdminDTO;
import cn.joytur.common.mvc.dto.WapMemberDTO;

/**
 * 基于jwt扩展授权
 * @author xuhang
 */
public class JWTUtil {

	/** token秘钥，请勿泄露，请勿随便修改 backups:JKKLJOoasdlfa */
	public static final String SECRET = "JKKLJOoasdlfa";
	/** token 过期时间: 10天 */
	public static final int calendarField = Calendar.DATE;
	public static final int calendarInterval = 10;
	
	public static final String ADMIN = "_admin";
	public static final String MEMBER = "_member";
	public static final int MAX_AGE_IN_SECONDS = 60 * 60 * 24 * 7;	//七天

	/**
	 * JWT生成Token.<br/>
	 * 
	 * JWT构成: header, payload, signature
	 * 
	 * @param user_id 登录成功后用户user_id, 参数user_id不可传空
	 */
	public static String createJWT(String key, String subject) {
		
		Date iatDate = new Date();
		// expire time
		Calendar nowTime = Calendar.getInstance();
		nowTime.add(calendarField, calendarInterval);
		Date expiresDate = nowTime.getTime();

		// header Map
		Map<String, Object> map = new HashMap<>();
		map.put("alg", "HS256");
		map.put("typ", "JWT");
		// build token
		// param backups {iss:Service, aud:APP}
		String token = null;
		try {
			token = JWT.create().withHeader(map) // header
					//.withClaim("iss", "Service") // payload
					//.withClaim("aud", "APP")
					.withClaim(key, subject)
					.withIssuedAt(iatDate) // sign time
					.withExpiresAt(expiresDate) // expire time
					.sign(Algorithm.HMAC256(SECRET));
		} catch (IllegalArgumentException | JWTCreationException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return token;
	}

	/**
	 * 解密Token
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Claim> parseJWT(String compactJws) {
		DecodedJWT jwt = null;
		try {
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
			jwt = verifier.verify(compactJws);
			return jwt.getClaims();
		} catch (Exception e) {
			// e.printStackTrace();
			// token 校验失败, 抛出Token验证非法异常
		}
		return null;
	}

	/**
	 * 根据Token获取user_id
	 * 
	 * @param token
	 * @return user_id
	 */
	public static AdminDTO getAdminDTO(Controller controller) {
		String authorization = controller.getHeader(ADMIN); //header里取token

		//paramter里面取
		if(StrKit.isBlank(authorization)) {
			authorization = controller.getPara(ADMIN);
			
			if(StrKit.notBlank(authorization)){
				controller.setCookie(JWTUtil.ADMIN, authorization, MAX_AGE_IN_SECONDS);
			}
		}
		
		//cookie里面取
		if(StrKit.isBlank(authorization)) {
			authorization = controller.getCookie(ADMIN);
		}
		
		//attr里面
		if(StrKit.isBlank(authorization)) {
			authorization = controller.getAttrForStr(ADMIN);
		}
		
		if(StrKit.isBlank(authorization)){
			return null;
		}
		
		Map<String, Claim> claims = parseJWT(authorization);
		if(claims == null){
			// token 校验失败, 抛出Token验证非法异常
			//throw new NotLoggedInException();
			return null;
		}
		
		Claim jwt_claim = claims.get(ADMIN);
		if (null == jwt_claim || StrKit.isBlank(jwt_claim.asString())) {
			// token 校验失败, 抛出Token验证非法异常
			//throw new NoAuthorizationException(RenderResultCode.NOTAUTH);
			return null;
		}
		
		AdminDTO adminDTO = JsonKit.parse(jwt_claim.asString(), AdminDTO.class);
		
		return adminDTO;
	}
	
	/**
	 * 根据Token获取user_id
	 * 
	 * @param token
	 * @return user_id
	 */
	public static WapMemberDTO getWapMemberDTO(Controller controller) {
		String authorization = controller.getHeader(MEMBER); //header里取token

		//paramter里面取
		if(StrKit.isBlank(authorization)) {
			authorization = controller.getPara(MEMBER);
			
			if(StrKit.notBlank(authorization)){
				controller.setCookie(JWTUtil.MEMBER, authorization, MAX_AGE_IN_SECONDS);
			}
		}
		
		//cookie里面取
		if(StrKit.isBlank(authorization)) {
			authorization = controller.getCookie(MEMBER);
		}
		
		//attr里面
		if(StrKit.isBlank(authorization)) {
			authorization = controller.getAttrForStr(MEMBER);
		}
		
		if(StrKit.isBlank(authorization)){
			return null;
		}
		
		Map<String, Claim> claims = parseJWT(authorization);
		Claim jwt_claim = claims.get(MEMBER);
		if (null == jwt_claim || StrKit.isBlank(jwt_claim.asString())) {
			// token 校验失败, 抛出Token验证非法异常
			throw new NoAuthorizationException(RenderResultCode.NOTAUTH);
		}
		
		WapMemberDTO wapMemberDTO = JsonKit.parse(jwt_claim.asString(), WapMemberDTO.class);
		
		return wapMemberDTO;
	}
	
	/**
	 * 注销token
	 * 
	 * @param token
	 * @return user_id
	 */
	public static void removeUID(Controller controller) {
		controller.removeCookie(ADMIN);
	}
	
}
