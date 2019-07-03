package cn.joytur.common.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

/**
 * 分类显示辅助工具对象
 * @author xuhang
 */
public class JoyPageUtil {

    private String paramHref;

    public boolean pageInit(Page page, HttpServletRequest request){
    	System.out.println("request=" + request);
        if (page.getTotalRow() > page.getPageSize() && page.getPageNumber() != 0){

            // 获取分页参数地址
            String servletPath = request.getServletPath();
            StringBuffer param = new StringBuffer(servletPath + "?");
            Enumeration<String> em = request.getParameterNames();
            while (em.hasMoreElements()) {
                String name = (String) em.nextElement();
                if(!name.equals("page")){
                    String value = request.getParameter(name);
                    param.append(name + "=" + value + "&");
                }
            }
            this.paramHref = param.toString();
            return true;
        }
        return false;
    }

    public List<String> pageCode(Page page){
        int number = page.getPageNumber()+1;
        int totalPages = page.getTotalPage();
        int start = 0;
        int length = 7;
        int half = length % 2 == 0 ? length / 2 : length / 2 + 1;
        List<String> codeList = new ArrayList<>();

        if(totalPages > length && number > half){
            start = number - half;
        }
        if(totalPages > length && number > totalPages - half){
            start = totalPages - length;
        }
        for (int i=1; i <= (totalPages > length ? length : totalPages); i++){
            codeList.add(String.valueOf( i + start));
        }
        if(totalPages > length && number > half){
            codeList.set(0, "1");
            codeList.set(1, "…");
        }
        if(totalPages > length && number < totalPages - (half-1)){
            codeList.set(length-2, "…");
            codeList.set(length-1, String.valueOf(totalPages));
        }
        return codeList;
    }

    public String pageActive(Page page, String pageCode, String className){
        int number = page.getPageNumber();
        if(!pageCode.equals("…")){
            if(number == Integer.valueOf(pageCode)){
                return " "+className;
            }
        }
        return "";
    }

    public boolean isPrevious(Page page){
        return page.getPageNumber() != 1;
    }

    public boolean isNext(Page page){
        return page.getPageNumber() < page.getTotalPage();
    }

    public boolean isCode(String pageCode){
        return pageCode.equals("…");
    }

    public String pageHref(String pageCode){
        return paramHref + "page=" + pageCode;
    }
    
    public String pageParams(String pageUrl, String pageCode){
    	if(StrKit.notBlank(pageUrl)){
    		return pageUrl.contains("?") ? pageUrl.concat(pageUrl + "&page=" + pageCode) : pageUrl.concat(pageUrl + "?page=" + pageCode);
    	}
        return "";
    }
}
