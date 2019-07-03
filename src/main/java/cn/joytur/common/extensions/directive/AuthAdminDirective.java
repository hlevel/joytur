package cn.joytur.common.extensions.directive;

import java.io.IOException;

import com.jfinal.kit.StrKit;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

import cn.hutool.core.collection.CollUtil;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.dto.AdminDTO;

/**
 * 权限校验
 * @author xuhang
 * @time 2019年1月15日 上午9:17:36
 */
public class AuthAdminDirective extends Directive {

	private Expr dto;
	private Expr auth;
	
	@Override
    public void setExprList(ExprList exprList) {
		//参数长度
        Integer paraNum = exprList.length();
        if (paraNum < 2) {
            throw new ParseException("#authAdmin 参数错误 示例#authAdmin(yaAdmin,'sys.auth.view')", location);
        }
        dto = exprList.getExpr(0);
        auth = exprList.getExpr(1);
	}
	
	@Override
	public void exec(Env env, Scope scope, Writer writer) {
		try{
			Object dtoValue = dto.eval(scope);
			Object authValue = auth.eval(scope);
			
			if (!(dtoValue instanceof AdminDTO)) {
	            throw new ParseException("#authAdmin 参数错误 示例#authAdmin(yaAdmin,'sys.auth.view')", location);
	        }
			
			if (!(authValue instanceof String)) {
	            throw new ParseException("#authAdmin 参数错误 示例#authAdmin(yaAdmin,'sys.auth.view')", location);
	        }
			
			boolean authAccept = false; //鉴权结果，默认为失败
			
			AdminDTO dto = (AdminDTO) dtoValue;
			String authName = (String) authValue;
			
			if(StrKit.equals(dto.getUsername(), CommonAttribute.SUPER_ADMIN_NAME)){
				authAccept = true;
	    	}
			authAccept = CollUtil.contains(dto.getPermissionList(), authName);
			
			writer.write(authAccept);
		} catch (IOException e) {
	        e.printStackTrace();
	    }
	}

}
