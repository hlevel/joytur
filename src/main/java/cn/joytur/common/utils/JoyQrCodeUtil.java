package cn.joytur.common.utils;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import cn.joytur.common.mvc.constant.CommonAttribute;

/**
 * @author xuhang
 */
public class JoyQrCodeUtil {

	private static final Integer WIDTH = 300;
	private static final Integer HEIGHT = 300;
	
	/*
	 * overlapImage
	 * @description：合成二维码和图片为文件
	 * @params [backPicPath, code]
	 * @return void
	 */
	public static final void enQrCodeBackgroundPicToFile(String backPicPath, BufferedImage code/*String fillPicPath*/) {
	    try {
	        BufferedImage big = ImageIO.read(new File(backPicPath));
	        BufferedImage small = code;
	        /*//合成两个文件时使用
	        BufferedImage small = ImageIO.read(new File(fillPicPath));*/
	        Graphics2D g = big.createGraphics();

	        //二维码或小图在大图的左上角坐标
	        //int x = (big.getWidth() - small.getWidth()) / 2;
	        // int y = (big.getHeight() - small.getHeight()) / 2;
	        //int y = (big.getHeight() - small.getHeight() - 100);
	        
	        int x = 128;
	        int y = 370;
	        g.drawImage(small, x, y, small.getWidth(), small.getHeight(), null);
	        g.dispose();
	        //为了保证大图背景不变色，formatName必须为"png"
	        ImageIO.write(big, "png", new File("C:/Users/xuhang/Desktop/share_user1.png"));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	/* 
	 * combineCodeAndPicToInputstream
	 * @description：合成二维码和图片为输出流，可用于下载或直接展示
	 * @params [backPicPath, code]
	 * @return java.io.InputStream
	 */
	public static final void enQrCodeBackgroundPicToInputstream(String backPicPath, BufferedImage code, HttpServletResponse resp) {
	    try {
	        BufferedImage big = ImageIO.read(new File(backPicPath));
	        // BufferedImage small = ImageIO.read(new File(fillPicPath));
	        BufferedImage small = code;
	        Graphics2D g = big.createGraphics();

	        //二维码或小图在大图的左上角坐标
	        int x = (big.getWidth() - small.getWidth()) / 2;
	        int y = (big.getHeight() - small.getHeight() - 100);   //二维码距大图下边距100
	        g.drawImage(small, x, y, small.getWidth(), small.getHeight(), null);
	        g.dispose();
	        resp.addHeader("Content-Disposition", "attachment;filename="+ URLEncoder.encode("lia阿里.png","UTF-8") );//去掉这行设置header的代码，前端访问可以直接展示
	        //为了保证大图背景不变色，formatName必须为"png"
	        ImageIO.write(big, "png", resp.getOutputStream());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	/* 
	 * combineCodeAndPicToBase64 
	 * @description：合成二维码和图片为Base64，同样可用于直接展示
	 * @params [backPicPath, code]
	 * @return java.lang.String
	 */
	public static final String enQrCodeBackgroundPicToBase64(String qrCodeContent, String backPicPath, int x, int y) {
		try {
			BufferedImage code = JoyQrCodeUtil.enQrCodeToImage(qrCodeContent, null, false);
			String imgBase64 = enQrCodeBackgroundPicToBase64(backPicPath, code, x, y);
			return imgBase64;
		} catch (IOException | WriterException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/* 
	 * combineCodeAndPicToBase64 
	 * @description：合成二维码和图片为Base64，同样可用于直接展示
	 * @params [backPicPath, code]
	 * @return java.lang.String
	 */
	public static final String enQrCodeBackgroundPicToBase64(String backPicPath, BufferedImage code, int x, int y) {
	    ImageOutputStream imOut = null;
	    try {
	        BufferedImage big = ImageIO.read(new File(backPicPath));
	        // BufferedImage small = ImageIO.read(new File(fillPicPath));
	        BufferedImage small = code;
	        Graphics2D g = big.createGraphics();

	        //二维码或小图在大图的左上角坐标
	        //int x = (big.getWidth() - small.getWidth()) / 2;
	        //int y = (big.getHeight() - small.getHeight() - 100);
	        g.drawImage(small, x, y, small.getWidth(), small.getHeight(), null);
	        g.dispose();
	        //为了保证大图背景不变色，formatName必须为"png"

	        ByteArrayOutputStream bs = new ByteArrayOutputStream();
	        imOut = ImageIO.createImageOutputStream(bs);
	        ImageIO.write(big, "png", imOut);
	        InputStream in = new ByteArrayInputStream(bs.toByteArray());

	        byte[] bytes = new byte[in.available()];
	        in.read(bytes);
	        String base64 = Base64.getEncoder().encodeToString(bytes);

	        return "data:image/jpeg;base64," + base64;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	/* 
	 * enQrCodeToImage 
	 * @description：生成二维码
	 * @params [content 二维码内容, logoImgPath 中间logo, needCompress 是否压缩]
	 * @return java.awt.image.BufferedImage
	 */
	public static BufferedImage enQrCodeToImage(String content, String logoImgPath, boolean needCompress) throws IOException, WriterException {
	    Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
	    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
	    hints.put(EncodeHintType.CHARACTER_SET, CommonAttribute.UTF_8);
	    hints.put(EncodeHintType.MARGIN, 1);
	    //200是定义的二维码或小图片的大小
	    BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 200, 200, hints);
	    int width = bitMatrix.getWidth();
	    int height = bitMatrix.getHeight();
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    //循环遍历每一个像素点
	    for (int x = 0; x < width; x++) {
	        for (int y = 0; y < height; y++) {
	            image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
	        }
	    }
	    // 没有logo
	    if (logoImgPath == null || "".equals(logoImgPath)) {
	        return image;
	    }

	    // 插入logo
	    enQrCodeLogoImage(image, logoImgPath, needCompress);
	    return image;
	}

	/* 
	 * insertImage 
	 * @description：二维码插入logo
	 * @params [source, logoImgPath, needCompress]
	 * @return void
	 */
	private static void enQrCodeLogoImage(BufferedImage source, String logoImgPath, boolean needCompress) throws IOException {
	    File file = new File(logoImgPath);
	    if (!file.exists()) {
	        return;
	    }

	    Image src = ImageIO.read(new File(logoImgPath));
	    int width = src.getWidth(null);
	    int height = src.getHeight(null);
	    //处理logo
	    if (needCompress) {
	        if (width > WIDTH) {
	            width = WIDTH;
	        }

	        if (height > HEIGHT) {
	            height = HEIGHT;
	        }

	        Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	        Graphics gMaker = tag.getGraphics();
	        gMaker.drawImage(image, 0, 0, null); // 绘制缩小后的图
	        gMaker.dispose();
	        src = image;
	    }

	    // 在中心位置插入logo
	    Graphics2D graph = source.createGraphics();
	    int x = (200 - width) / 2;
	    int y = (200 - height) / 2;
	    graph.drawImage(src, x, y, width, height, null);
	    Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
	    graph.setStroke(new BasicStroke(3f));
	    graph.draw(shape);
	    graph.dispose();
	}
	
	/**
	 * 输出二维码到指定文件夹
	 * @param content
	 * @param tmpFolderPath
	 * @return
	 */
	public static File enQrCodeToImageFilePath(String content, String tmpFolderPath){
		try {
        	BufferedImage image = JoyQrCodeUtil.enQrCodeToImage(content, null, true);
        	//为了保证大图背景不变色，formatName必须为"png"
        	File newFile = new File(tmpFolderPath + File.separator + "en_qrcode_" + System.currentTimeMillis() + ".png");
	        ImageIO.write(image, "png", new File(tmpFolderPath + File.separator + "en_qrcode_" + System.currentTimeMillis() + ".png"));
	        
	        return newFile;
		} catch (IOException | WriterException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 解析二维码内容
	 * @param filePath
	 * @return
	 */
	public static String decQrcode(String filePath){
		try {
            MultiFormatReader formatReader = new MultiFormatReader();

            //读取文件识别成一个图片
            File file = new File(filePath);
            BufferedImage image = ImageIO.read(file);

            /*
            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
            HybridBinarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            */
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));


            //定义二维码的参数
            HashMap map = new HashMap();
            map.put(EncodeHintType.CHARACTER_SET, "utf-8");//字符集

            Result result = formatReader.decode(binaryBitmap, map);

            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
	/**
	 * 解析二维码内容
	 * @param filePath
	 * @return
	 */
	public static String decQrcodeURL(String url){
		try {
            MultiFormatReader formatReader = new MultiFormatReader();

            //读取文件识别成一个图片
            URL tmpUrl = new URL(url);
            BufferedImage image = ImageIO.read(tmpUrl);

            /*
            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
            HybridBinarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            */
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));


            //定义二维码的参数
            HashMap map = new HashMap();
            map.put(EncodeHintType.CHARACTER_SET, "utf-8");//字符集

            Result result = formatReader.decode(binaryBitmap, map);

            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
	/**
	 * 加密二维码为base64
	 * @param content
	 * @return
	 */
	public static String enQrCodeToBase64(String content){
		try {
			BufferedImage code = JoyQrCodeUtil.enQrCodeToImage(content, null, false);
			ImageOutputStream imOut = null;

	        ByteArrayOutputStream bs = new ByteArrayOutputStream();
	        imOut = ImageIO.createImageOutputStream(bs);
	        ImageIO.write(code, "png", imOut);
	        InputStream in = new ByteArrayInputStream(bs.toByteArray());

	        byte[] bytes = new byte[in.available()];
	        in.read(bytes);
	        String base64 = Base64.getEncoder().encodeToString(bytes);

	        return "data:image/jpeg;base64," + base64;
		} catch (IOException | WriterException e) {
			e.printStackTrace();
		}
		return null;

	}
	
		 

	public static final void main(String[] args) throws IOException, WriterException {
	    //BufferedImage code = createImage("https://my.oschina.net/kevin2kelly", null, false);
	    //combineCodeAndPicToFile("C:/Users/xuhang/Desktop/share_qcode.png", code);
	    //combineCodeAndPicToBase64("C:/Users/xuhang/Desktop/IMG_1440.JPG", code);
	    
	    System.out.println(decQrcode("C:\\Users\\xuhang\\Desktop\\QQ图片20190305224303.jpg"));
	    System.out.println(enQrCodeToBase64("C:\\Users\\xuhang\\Desktop\\QQ图片20190305224303.jpg"));
	    enQrCodeToImageFilePath("test", "C:\\Users\\xuhang\\Desktop");
	}

}
