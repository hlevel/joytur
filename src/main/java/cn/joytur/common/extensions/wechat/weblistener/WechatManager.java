package cn.joytur.common.extensions.wechat.weblistener;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.aop.Duang;
import com.jfinal.kit.StrKit;

import cn.hutool.core.util.ImageUtil;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.Enums.WebWechatStatus;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyUtil;
import cn.joytur.modules.order.service.RechargeOrderService;
import cn.joytur.modules.product.service.WechatWebPayListenerImpl;

/**
 * 微信网页管理
 * @author xuhang
 */
public class WechatManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WechatManager.class);

    /**
     * 可以写个网页接口，判断Online文件是否存在即可知道微信是否在线
     * 经过测试直接关闭控制台后ShutdownHook不会运行，所以该情况下Online文件不会自动删除
     */
    public static final File ONLINE_FILE = new File("Online");
    private static WechatWebPayListenerImpl weChatWebProcessor;

    /**
     * 状态code
     */
    public static WebWechatStatus webWechatStatus = WebWechatStatus.WAIT;
    
    /**
     * 登录二维码图片
     */
    public static String loginCodeImage;

    /*
    private static final Thread SHUTDOWN_HANDLER = new Thread(() -> {
        if (ONLINE_FILE.exists())
            //noinspection ResultOfMethodCallIgnored
            ONLINE_FILE.delete();
    });
	*/
    
    public static void login() {
        final WechatWebPayListener listener = new WechatWebPayListener() {

            @Override
            public void onLoadingQRCode() {
            	webWechatStatus = WebWechatStatus.LOADING_QRCODE;
                LOGGER.info("正在获取登录二维码..");
            }

            @Override
            public void onReceivedQRCode(byte[] jpgData) {
            	webWechatStatus = WebWechatStatus.RECEIVED_QRCODE;
            	//loginCodeImage = JoyQrCodeUtil.enQrCodeToBase64(jpgData);
            	//loginCodeImage = jpgData;
            	loginCodeImage = "data:image/jpeg;base64," + ImageUtil.toBase64(ImageUtil.toImage(jpgData), "png");
                LOGGER.info("获取成功，请用手机微信扫码");
            }

            @Override
            public void onQRCodeScanned(byte[] jpgData) {
            	webWechatStatus = WebWechatStatus.SCANNED_QRCODE;
                LOGGER.info("扫码成功，请在手机微信中点击登录");
                loginCodeImage = "data:image/jpeg;base64," + ImageUtil.toBase64(ImageUtil.toImage(jpgData), "png");
                
            }

            @Override
            public void onLoginResult(boolean loginSucceed) {
                if (loginSucceed) {
                	webWechatStatus = WebWechatStatus.LOGIN_SUCCES;
                    LOGGER.info("登录成功");
                    try {
                        if (!ONLINE_FILE.createNewFile()) {
                            LOGGER.error("创建Online文件失败");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                	webWechatStatus = WebWechatStatus.LOGIN_FAIL;
                    LOGGER.info("登录失败");
                    //noinspection ResultOfMethodCallIgnored
                    ONLINE_FILE.delete();
                }
            }

            @Override
            public void onReceivedMoney(String money) throws IOException {
                LOGGER.info("二维码收款：{}元", money);
                //MtUtil.openVip(mark, money, id);
                Long rechargeType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_UNPAY, DictAttribute.RECHARGE_ORDER_STATUS, ""));
        		Duang.duang(RechargeOrderService.class).payCompletedRechargeOrder(Double.valueOf(money), rechargeType, null);
            }

            @Override
            public void onDropped(long onlineTime) {
            	webWechatStatus = WebWechatStatus.LOGIN_DROPPED;
                //noinspection ResultOfMethodCallIgnored
                ONLINE_FILE.delete();
                if (onlineTime > 5000) {
                	String receiptMail = JoyConfigUtil.getConfigValue(Enums.SysConfigType.MONITOR_RECEIPT_MAIL.name());
                    if (StrKit.notBlank(receiptMail) && JoyUtil.sendEmail(receiptMail, "微信离线通知", "服务器的微信已经离线啦，快去登录！"))
                        LOGGER.info("微信已离线，发送通知邮件成功");
                    else
                        LOGGER.info("微信已离线，发送通知邮件失败");
                } else {
                    LOGGER.info("请尝试重新登录");
                    login();
                }
            }

            @Override
            public void onException(IOException e) {
                e.printStackTrace();
            }
            
        };
        //weChatWebProcessor = new WechatWebProcessor(listener);
        //weChatWebProcessor.login();
    }
}
