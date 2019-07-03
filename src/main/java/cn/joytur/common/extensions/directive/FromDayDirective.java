package cn.joytur.common.extensions.directive;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;
 
public class FromDayDirective extends Directive {
 
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter _time = DateTimeFormatter.ofPattern("HH:mm");
 
    // valueExpr 能为 string，or date
    private Expr valueExpr;
    private int paraNum;
 
    @Override
    public void setExprList(ExprList exprList) {
        //参数长度
        this.paraNum = exprList.length();
        if (paraNum != 1) {
            throw new ParseException("#fromday 参数错误", location);
        }
        valueExpr = exprList.getExpr(0);
    }
 
    @Override
    public void exec(Env env, Scope scope, Writer writer) {
        try {
            showDate(env, scope, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    private void showDate(Env env, Scope scope, Writer writer) throws IOException {
        Object value = valueExpr.eval(scope);
        if (!(value instanceof Date) && !(value instanceof String)) {
            throw new ParseException("#fromday 参数错误", location);
        }
        //结束时间
        LocalDateTime now = LocalDateTime.now();
        if (value instanceof Date) {
            LocalDateTime thisValue = LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault());
            write(writer, now, thisValue);
        } else {
            TemporalAccessor parse = formatter.parse((String) value);
            LocalDateTime thisValue = LocalDateTime.from(parse);
            write(writer, now, thisValue);
        }
    }
 
    private void write(Writer writer, LocalDateTime now, LocalDateTime thisValue) throws IOException {
        Duration between = Duration.between(thisValue, now);
        //小于1分钟
        long l = between.toMinutes();
        if (l < 1) {
            writer.write("刚刚");
            return;
        }
        //小于1小时
        long l1 = between.toHours();
        if (l1 < 1) {
            writer.write(between.toMinutes() + "分钟前");
            return;
        }
 
        LocalDate time2 = thisValue.toLocalDate();
        LocalDate now2 = now.toLocalDate();
 
        if (time2.equals(now2)) {
            writer.write(_time.format(thisValue));
            return;
        }
        writer.write(formatter.format(thisValue));
    }
}
