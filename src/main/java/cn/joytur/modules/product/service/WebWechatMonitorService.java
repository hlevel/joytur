package cn.joytur.modules.product.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.joytur.common.extensions.wechat.weblistener.WechatWebPayListener;
import cn.joytur.common.mvc.constant.Enums.WebWechatStatus;
import cn.joytur.common.utils.JoyIdUtil;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebWechatMonitorService {
	private final Logger LOGGER = LoggerFactory.getLogger(WebWechatMonitorService.class);

    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    private static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36";

    private String uuid;
    private String loginUrl;
    private String domainName;
    private String pushDomainName;
    private String skey;
    private String wxsid;
    private String wxuin;
    private String pass_ticket;
    private String syncKey;
    private JSONObject syncKeyJson;

    private WechatWebPayListenerImpl listener = new WechatWebPayListenerImpl();

    private CookieJar cookieJar = new CookieJar() {
        HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = new ArrayList<>();
            String host = url.host();
            // 没有对path进行匹配
            for (Map.Entry<String, List<Cookie>> entry : cookieStore.entrySet()) {
                if (host.endsWith(entry.getKey())) {
                    cookies.addAll(entry.getValue());
                }
            }
            return cookies;
        }

    };
    private OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .cookieJar(cookieJar).build();
    // 禁止重定向
    private OkHttpClient client2 = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .cookieJar(cookieJar).followRedirects(false).build();

    private byte[] getLoginQRCode() throws IOException {
        String url = "https://wx2.qq.com/?&lang=zh_CN";
        Request request = new Request.Builder().url(url)
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", UA)
                .build();
        Response response = client.newCall(request).execute();
        response.close();

        url = "https://login.wx2.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx2.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_=" + System.currentTimeMillis();
        request = new Request.Builder().url(url)
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", UA)
                .build();

        response = client.newCall(request).execute();
        checkStatusCode(response);
        //noinspection ConstantConditions
        String str = response.body().string();
        response.close();
        uuid = StrUtil.subBetween(str, "window.QRLogin.uuid = \"", "\";");

        url = "https://login.weixin.qq.com/qrcode/" + uuid;
        request = new Request.Builder().url(url)
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", UA)
                .build();
        response = client.newCall(request).execute();
        checkStatusCode(response);
        //noinspection ConstantConditions
        byte[] data = response.body().bytes();
        response.close();
        return data;
    }

    private long lastCheckQRCodeTime;

    private boolean checkQRCode() throws IOException {
        if (lastCheckQRCodeTime == -1)
            lastCheckQRCodeTime = System.currentTimeMillis();
        else
            lastCheckQRCodeTime++;
        int r = (int) (System.currentTimeMillis() & 0xFFFFFFF);
        String url = "https://login.wx2.qq.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid=" + uuid + "&tip=0&r=-" + r + "&_=" + lastCheckQRCodeTime;
        Request request = new Request.Builder().url(url)
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", UA)
                .addHeader("host", "login.wx2.qq.com")
                .addHeader("referer", "https://wx2.qq.com/?&lang=zh_CN")
                .build();
        Response response = client.newCall(request).execute();
        checkStatusCode(response);
        //noinspection ConstantConditions
        String str = response.body().string();
//        System.out.println(str);
//        response.close();
        String code = StrUtil.subBetween(str, "code=", ";");
        switch (code) {
            case "408":
                break;
            case "201":
                String base64 = StrUtil.subBetween(str, "base64,", "'");
                //listener.onQRCodeScanned(new BASE64Decoder().decodeBuffer(base64));
                listener.onQRCodeScanned(Base64.decode(base64));
                break;
            case "200":
                loginUrl = StrUtil.subBetween(str, "redirect_uri=\"", "\"");
                domainName = StrUtil.subBetween(loginUrl, "//", "/");
                pushDomainName = "webpush." + domainName;
                return true;
            default:
                LOGGER.debug("checkQRCode: unknown code - " + str);
                return false;
        }
        return false;
    }

    private boolean checkIsLogged() throws IOException {
        Request request = new Request.Builder().url(loginUrl)
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", UA)
                .build();
//        System.out.println("loginUrl " + loginUrl);
        Response response = client2.newCall(request).execute();
        //noinspection ConstantConditions
        String str = response.body().string();
        response.close();
//        System.out.println(str);
        if (StrUtil.subBetween(str, "<ret>", "</ret>").equals("0")) {
            skey = StrUtil.subBetween(str, "<skey>", "</skey>");
            wxsid = StrUtil.subBetween(str, "<wxsid>", "</wxsid>");
            wxuin = StrUtil.subBetween(str, "<wxuin>", "</wxuin>");
            pass_ticket = StrUtil.subBetween(str, "<pass_ticket>", "</pass_ticket>");
//            System.out.println(skey);
//            System.out.println(wxsid);
//            System.out.println(wxuin);
//            System.out.println(pass_ticket);
//            System.out.println(webwx_data_ticket);
            loadSyncKey();
            return true;
        }
        return false;
    }

    public void login() {
        lastCheckQRCodeTime = -1;
        listener.onLoadingQRCode();
        new Thread(() -> {
            try {
                byte[] data = getLoginQRCode();
                listener.onReceivedQRCode(data);
                while (true) {
                    if (checkQRCode())
                        break;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                boolean logged = checkIsLogged();
                listener.onLoginResult(logged);
                if (logged) {
                    long time = System.currentTimeMillis();
                    w:
                    while (true) {
                        for (int i = 0; i < 5; i++) {
                            try {
                                if (syncCheck() < 1000)
                                    continue w;
                            } catch (Throwable e) {
                                LOGGER.debug("SyncCheck", e);
                                continue w;
                            }
                        }
                        // 如果10次都得到错误的返回码，break
                        break;
                    }
                    listener.onDropped(System.currentTimeMillis() - time);
                }
            } catch (IOException e) {
                listener.onException(e);
            }
        }).start();
    }

    private void loadSyncKey() throws IOException {
        String postData = "{\"BaseRequest\":{\"Uin\":\"" + wxuin
                + "\",\"Sid\":\"" + wxsid + "\",\"Skey\":\"" + skey
                + "\",\"DeviceID\":\"" + JoyIdUtil.get15RandomText() + "\"}}";
        int r = (int) (System.currentTimeMillis() & 0xFFFFFFF);
        String url = "https://" + domainName + "/cgi-bin/mmwebwx-bin/webwxinit?r=-" + r + "&lang=zh_CN&pass_ticket=" + pass_ticket;
        Request request = new Request.Builder().url(url)
                .method("POST", RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), postData))
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", UA)
                .addHeader("host", "wx.qq.com")
                .addHeader("content-type", "application/json;charset=UTF-8")
                .addHeader("referer", "https://" + domainName + "/")
                .build();

        Response response = client.newCall(request).execute();

        //noinspection ConstantConditions
        JSONObject jsonObject = JSONObject.parseObject(response.body().string());
        response.close();
        jsonObject = syncKeyJson = jsonObject.getJSONObject("SyncKey");
        int count = jsonObject.getIntValue("Count");
        JSONArray jsonArray = jsonObject.getJSONArray("List");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            jsonObject = jsonArray.getJSONObject(i);
            sb.append("|").append(jsonObject.getIntValue("Key")).append("_").append(jsonObject.getIntValue("Val"));
        }
        sb.deleteCharAt(0);
        syncKey = sb.toString();
//        System.out.println(syncKey);
//        System.out.println(syncKeyJson.toString());
    }

    private int syncCheck() throws IOException {
        String url = "https://" + pushDomainName + "/cgi-bin/mmwebwx-bin/synccheck?r=" + System.currentTimeMillis()
                + "&skey=" + skey.replace("@", "%40") + "&sid=" + wxsid + "&uin=" + wxuin + "&deviceid=" + JoyIdUtil.get15RandomText()
                + "&synckey=" + syncKey.replace("|", "%7C") + "&_=" + System.currentTimeMillis();

//        System.out.println("正在等待消息..");

        Request request = new Request.Builder().url(url)
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent", UA)
                .addHeader("referer", "https://" + domainName + "/")
                .addHeader("host", pushDomainName)
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (Exception e) {
            return 1100;
        }
        if (!response.isSuccessful()) {
            response.close();
            return 1100;
        }

        //noinspection ConstantConditions
        String str = response.body().string();
        response.close();
        String retCode = StrUtil.subBetween(str, "retcode:\"", "\"");
        String selector = StrUtil.subBetween(str, "selector:\"", "\"");

        // 1101 1100 1102掉线
        if (retCode.length() == 4 && retCode.startsWith("1")) {
            LOGGER.warn(str);
            return Integer.parseInt(retCode);
        }
        if (selector.equals("0"))
            return 0;
        // 有消息
        try {
            getMessage();
        } catch (Throwable e) {
            return 1000;
        }
        return 1;
    }

    private void getMessage() throws IOException {
        JSONObject json = new JSONObject();

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin", wxuin);
        baseRequest.put("Sid", wxsid);
        baseRequest.put("Skey", skey);
        baseRequest.put("DeviceID", JoyIdUtil.get15RandomText());
        json.put("BaseRequest", baseRequest);
        json.put("SyncKey", syncKeyJson);
        StringBuilder sb = new StringBuilder("-1728");
        for (int i = 0; i < 6; i++) {
            sb.append(RandomUtil.randomInt(10));
        }
        json.put("rr", Integer.valueOf(sb.toString()));
        String content = json.toString();
//        System.out.println(content);

        String url = "https://" + domainName + "/cgi-bin/mmwebwx-bin/webwxsync?sid=" + wxsid + "&skey=" + skey.replace("@", "%40") + "&pass_ticket=" + pass_ticket;
        Request request = new Request.Builder().url(url)
                .method("POST", RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), content))
                .addHeader("connection", "keep-alive")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", UA)
                .addHeader("content-type", "application/json;charset=UTF-8")
                .build();


        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (Exception e) {
            return;
        }
        if (!response.isSuccessful()) {
            response.close();
            return;
        }

        //noinspection ConstantConditions
        content = response.body().string();
        response.close();
//        System.out.println(content);

        JSONObject jsonObject = JSONObject.parseObject(content);
        JSONObject syncKeyJson = this.syncKeyJson = jsonObject.getJSONObject("SyncKey");
        int count = syncKeyJson.getIntValue("Count");
        JSONArray jsonArray = syncKeyJson.getJSONArray("List");
        sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            syncKeyJson = jsonArray.getJSONObject(i);
            sb.append("|").append(syncKeyJson.getIntValue("Key")).append("_").append(syncKeyJson.getIntValue("Val"));
        }
        sb.deleteCharAt(0);
        syncKey = sb.toString();

        jsonArray = jsonObject.getJSONArray("AddMsgList");
        int size = jsonArray.size();
        for (int i = 0; i < size; i++) {
            jsonObject = jsonArray.getJSONObject(i);
//            String fromUserName = jsonObject.getString("FromUserName");
//            String toUserName = jsonObject.getString("ToUserName");
            int msgType = jsonObject.getIntValue("MsgType");
            String con = jsonObject.getString("Content");
            if (msgType == 49) {
                checkPay(con);
            }
        }
    }

    private void checkPay(String con) throws IOException {
        if (!con.contains("CDATA[微信支付]") || !con.contains("CDATA[收款到账通知") || !con.contains("收款成功") || !con.contains("CDATA[微信转账]")){
        	return;
        }
        String text = HtmlUtil.unescape(con);
        LOGGER.info(text);
        //获取金额
        String des = ReUtil.get("<des>(.*?)</des>", text, 1);
        String money = ReUtil.get("[0-9]+(.[0-9]{2})?", des, 0);
        if (money.isEmpty())
            return;
        try {
            //noinspection ResultOfMethodCallIgnored
            Float.parseFloat(money);
        } catch (NumberFormatException e) {
        	LOGGER.error(e.getMessage(), e);
            return;
        }
        listener.onReceivedMoney(money);
    }

    private static void checkStatusCode(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Http status code = " + response.code());
        }
    }
    
    public WebWechatStatus getWebWechatStatus(){
    	return listener.getWebWechatStatus();
    }
    
    public String getQrcodeImage(){
    	return listener.getQrcodeImage();
    }
}
