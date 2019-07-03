package cn.joytur.common.extensions.wechat.weblistener;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.kit.StrKit;

import cn.hutool.core.util.ImageUtil;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyUtil;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * 可以写个网页接口，判断Online文件是否存在即可知道微信是否在线
     * 经过测试直接关闭控制台后ShutdownHook不会运行，所以该情况下Online文件不会自动删除
     */
    private static final File ONLINE_FILE = new File("Online");

    private static final Thread SHUTDOWN_HANDLER = new Thread(() -> {
        if (ONLINE_FILE.exists())
            //noinspection ResultOfMethodCallIgnored
            ONLINE_FILE.delete();
    });

    public static void main(String[] args) throws IOException {
        LOGGER.info("微信支付监听 V1.1");
        Runtime.getRuntime().addShutdownHook(SHUTDOWN_HANDLER);
        if (ONLINE_FILE.exists())
            //noinspection ResultOfMethodCallIgnored
            ONLINE_FILE.delete();

        login();

        /*Scanner scanner = new Scanner(System.in);
        while (true) {
            if (scanner.nextLine().equalsIgnoreCase("exit")) {
                System.exit(0);
                break;
            }
        }*/
    }

    private static void login() {
        final WechatWebPayListener listener = new WechatWebPayListener() {
            ImageViewer viewerFrame;

            @Override
            public void onLoadingQRCode() {
                LOGGER.info("正在获取登录二维码..");
            }

            @Override
            public void onReceivedQRCode(byte[] jpgData) {
            	LOGGER.info("获取成功，请用手机微信扫码");
                viewerFrame = new ImageViewer(jpgData);
            }

            @Override
            public void onQRCodeScanned(byte[] jpgData) {
                LOGGER.info("扫码成功，请在手机微信中点击登录");
                if (viewerFrame != null) {
                    viewerFrame.setImage(jpgData);
                }
            }

            @Override
            public void onLoginResult(boolean loginSucceed) {
                if (viewerFrame != null) {
                    viewerFrame.dispose();
                    viewerFrame = null;
                }
                if (loginSucceed) {
                    LOGGER.info("登录成功");
                    try {
                        if (!ONLINE_FILE.createNewFile()) {
                            LOGGER.error("创建Online文件失败");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    LOGGER.info("登录失败");
                    //noinspection ResultOfMethodCallIgnored
                    ONLINE_FILE.delete();
                }
            }

            @Override
            public void onReceivedMoney(String money) throws IOException {
                LOGGER.info("二维码收款：{}元", money);
                //MtUtil.openVip(mark, money, id);
            }

            @Override
            public void onDropped(long onlineTime) {
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
                if (viewerFrame != null) {
                    viewerFrame.dispose();
                    viewerFrame = null;
                }
            }
            
        };
        //new WechatWebProcessor(listener).login();
    }
}
