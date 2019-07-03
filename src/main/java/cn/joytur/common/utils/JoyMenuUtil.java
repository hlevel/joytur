package cn.joytur.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.ehcache.CacheKit;

import cn.hutool.core.util.StrUtil;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.modules.system.entities.SysMenu;
/**
 * 权限认证管理
 * @author xuhang
 */
public class JoyMenuUtil {
	
	public static final String CACHE_MENU_KEY = "joy_menu_key";
	
	/**
	 * 获取菜单名称路径（如：系统设置-机构用户-用户管理-编辑）
	 */
	public static String getMenuNamePath(String requestUri, String[] perms){
		String href = StrUtil.subAfter(requestUri, PropKit.get(CommonAttribute.SYSTEM_ADMIN_PATH), false);
		@SuppressWarnings("unchecked")
		Map<String, String> menuMap = (Map<String, String>) CacheKit.get(CommonAttribute.CACHE_SYSTEM, CACHE_MENU_KEY);
		if (menuMap == null){
			menuMap = new HashMap<String, String>();
			List<SysMenu> menuList = SysMenu.dao.findAll();
			for (SysMenu menu : menuList){
				// 获取菜单名称
				String namePath = "";
				if (!StrKit.equals(menu.getPid(), "0")){
					List<String> namePathList = new ArrayList<>();
					for (String id : StrUtil.split(menu.getPath(), ",")){
						for (SysMenu m : menuList){
							if (m.getId().equals(id)){
								namePathList.add(m.getTitle());
								break;
							}
						}
					}
					namePathList.add(menu.getTitle());
					namePath = StrUtil.join("-", namePathList);
				}
				// 设置菜单名称路径
				if (StrUtil.isNotBlank(menu.getUrl())){
					menuMap.put(menu.getUrl(), namePath);
				}else if (StrUtil.isNotBlank(menu.getPermission())){
					/*for (String p : StringUtils.split(menu.getPermission())){
						menuMap.put(p, namePath);
					}*/
					menuMap.put(menu.getPermission(), namePath);
				}
				
			}
			CacheKit.put(CommonAttribute.CACHE_SYSTEM, CACHE_MENU_KEY, menuMap);
		}
		String menuNamePath = menuMap.get(href);
		if (menuNamePath == null){
			
			if(perms != null){
				for(String perm : perms) {
					menuNamePath = menuMap.get(perm);
				}
			}
			/*
			for (String p : StringUtils.split(permission)){
				menuNamePath = menuMap.get(p);
				if (StringUtils.isNotBlank(menuNamePath)){
					break;
				}
			}
			*/
			if (menuNamePath == null){
				return "";
			}
		}
		return menuNamePath;
	}
	
	/**
	 * 递归排序菜单
	 * @param list
	 * @param sourcelist
	 * @param pid
	 */
    public static void sortMenuList(List<SysMenu> list, List<SysMenu> sourcelist, String pid){
    	for (int i=0; sourcelist != null && i < sourcelist.size(); i++){
    		SysMenu e = sourcelist.get(i);
    		if (e.getPid()!=null && e.getPid().equals(pid)){
    			sortChildMenuList(sourcelist, e);
    			list.add(e);
    		}
    	}
    }
    
    
    /**
     * 递归排序菜单
     * @param sourcelist
     * @param parent
     */
    private static void sortChildMenuList(List<SysMenu> sourcelist, SysMenu parent){
    	for (int i=0; sourcelist != null && i < sourcelist.size(); i++){
    		SysMenu e = sourcelist.get(i);
    		if (e.getPid()!=null && e.getPid().equals(parent.getId())){
    			
    			if(parent.childSysMenuList == null){
    				parent.childSysMenuList = new ArrayList<SysMenu>();
				}
    			parent.childSysMenuList.add(e);
				
				// 判断是否还有子节点, 有则继续获取子节点
				for (int j=0; j<sourcelist.size(); j++){
					SysMenu child = sourcelist.get(j);
					if (child.getPid()!=null && child.getPid().equals(e.getId())){
						sortChildMenuList(sourcelist, e);
						break;
					}
				}
    		}
    	}
    }

	/**
	 * 清除缓存
	 */
	public static void clearCache() {
		CacheKit.remove(CommonAttribute.CACHE_SYSTEM, CACHE_MENU_KEY);
	}
	
}
