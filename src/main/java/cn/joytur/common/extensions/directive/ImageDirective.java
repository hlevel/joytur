package cn.joytur.common.extensions.directive;

import java.io.IOException;

import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.utils.JoyUploadFileUtil;

/**
 * 图片展示
 * @author xuhang
 * @time 2019年1月15日 上午9:17:36
 */
public class ImageDirective extends Directive {

	// valueExpr 能为 string
    private Expr valueExpr;
    private Expr defaultExpr;	//默认图片
    private static final String localAdminImgPath = CommonAttribute.BASE_STATIC_PATH + PropKit.get(CommonAttribute.SYSTEM_THEME) + "/admin/images/img_notfund.png";
    private String defaultImg = "img_notfund.png";	//默认图片
    
    @Override
    public void setExprList(ExprList exprList) {
    	valueExpr = exprList.getExpr(0);
    	
    	if(exprList.length() > 1){
    		defaultExpr = exprList.getExpr(1);
    	}
    }
    
	@Override
	public void exec(Env env, Scope scope, Writer writer) {
		try {
			Object value = valueExpr.eval(scope);
			
			if(defaultExpr != null && defaultExpr.eval(scope) != null){
				Object defaultValue = defaultExpr.eval(scope);
				defaultImg = defaultValue.toString();
			}else{
				defaultImg = localAdminImgPath;
			}
			
			String finalPath = null;
			if(value == null || StrKit.isBlank(value.toString()) ){
				finalPath = defaultImg; 
			}else{
				if (!(value instanceof String)) {
					throw new ParseException("#image 参数错误", location);
				}
				String img = String.valueOf(value);
				finalPath = JoyUploadFileUtil.getHttpPath(img);
			}
			
			writer.write(finalPath);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
