package com.yy.ent.wx.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.util.StringUtils;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.WxMpCustomMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yy.ent.cherrice.Return;
import com.yy.ent.cherrice.annotation.Read;
import com.yy.ent.cherrice.ret.Forward;
import com.yy.ent.cherrice.ret.Render;
import com.yy.ent.commons.base.dto.Property;
import com.yy.ent.commons.base.inject.Inject;
import com.yy.ent.wx.base.BaseAction;
import com.yy.ent.wx.common.model.Image;
import com.yy.ent.wx.common.model.Video;
import com.yy.ent.wx.common.model.Wx1931;
import com.yy.ent.wx.dao.ImageDao;
import com.yy.ent.wx.service.RouterService;

public class RouterAction extends BaseAction {

	private Logger logger = Logger.getLogger(RouterAction.class);

	@Inject(instance = RouterService.class)
	private RouterService routerService;

	@Inject(instance = ImageDao.class)
	private ImageDao imageDao;

	private WxMpInMemoryConfigStorage wxMpConfigStorage;
	private WxMpService wxMpService;
	private WxMpMessageRouter wxMpMessageRouter;
	private boolean setRouter = false; 

	@Inject(instance = Image.class)
	private Image image;

	public RouterAction() throws Exception {
		super();
		wxMpConfigStorage = new WxMpInMemoryConfigStorage();
		wxMpConfigStorage.setAppId("wxbea2b5d9b8ffad02"); // 设置微信公众号的appid
		wxMpConfigStorage.setSecret("c084c7231172adab97ea4ce515516333"); // 设置微信公众号的app
		wxMpConfigStorage.setToken("vzhanqun1234567890"); // 设置微信公众号的token
		wxMpService = new WxMpServiceImpl();
		wxMpService.setWxMpConfigStorage(wxMpConfigStorage);

	}

	/**
	 * 获取媒体文件
	 * 
	 * @param jsonData
	 *            格式 1为文本， 2为图片 参数示例 {"data"："1"}
	 * @return
	 * @throws Exception
	 */
	public Render getMediaList(@Read(key = "data") String jsonData)
			throws Exception {
		JSONObject jo = JSON.parseObject(jsonData);
		String str = (String) jo.get("data");
		int type = Integer.valueOf(str);
		return getRender(routerService.getMediaList(type));
	}

	/**
	 * 获取文本文件列表接口
	 * 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public Forward getTextList(@Read(key = "data") String jsonData)
			throws Exception {
		getRequest().setAttribute("textList", routerService.getMediaList(1));
		return getForward("uploadText.jsp");
	}

	/**
	 * 获取图片文件列表接口
	 * 
	 * @return
	 * @throws Exception
	 */
	public Return getImageList() throws Exception {
		getRequest().setAttribute("imageList", routerService.getMediaList(2));
		return getForward("uploadImage.jsp");
	}


	
	/**
	 * 获取路由文件列表接口
	 * 
	 * @return
	 * @throws Exception
	 */
	public Return getRouterList() throws Exception {

		HttpServletRequest req = getRequest();
		req.setAttribute("routerList", routerService.getRouterList());
		return getForward("router.jsp");
	}

	/**
	 * 保存路由文件
	 * 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public Render saveRouter(@Read(key = "data") String jsonData)
			throws Exception {

		if (jsonData != null && !jsonData.equals("")) {
			int result = routerService.saveRouter(jsonData);
			reSetRouter();
			return getRender(result);
		}
		return getRenderFail("参数错误");
	}

	/**
	 * 更新路由文件
	 * 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public Render updateRouter(@Read(key = "data") String jsonData)
			throws Exception {
		if (jsonData != null && !jsonData.equals("")) {
			int result = routerService.updateRouter(jsonData);
			reSetRouter();
			return getRender(result);
		}
		return getRenderFail("参数错误");
	}

	/**
	 * 删除路由文件
	 * 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public Render deleteRouter(@Read(key = "data") String jsonData)
			throws Exception {

		if (jsonData != null && !jsonData.equals("")) {
			int result = routerService.deleteRouter(jsonData);
			reSetRouter();
			return getRender(result);
		}
		return getRenderFail("参数错误");
	}

	/**
	 * 删除图片
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public Render deleteImage(@Read(key = "data") String jsonData)
			throws Exception {

		if (jsonData != null && !jsonData.equals("")) {
			int result = routerService.deleteImage(jsonData);
			return getRender(result);
		}
		return getRenderFail("参数错误");
	}
	
	/**
	 * 删除文本
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public Render deleteText(@Read(key = "data") String jsonData)
			throws Exception {

		if (jsonData != null && !jsonData.equals("")) {
			int result = routerService.deleteText(jsonData);
			return getRender(result);
		}
		return getRenderFail("参数错误");
	}
	
	/**
	 * 上传图片到本服务器
	 * 
	 * @return
	 * @throws Exception
	 */
	public Render uploadImage() throws Exception {
		
		System.out.println("-------------上传图片------------------------");
		String message = routerService.uploadImage(getRequest());
		if (message.equals("fail")) {
			getRenderFail(message);
		}
		return getRender(message);
	}

	/**
	 * 从本服务器上传图片到微信服务器
	 * 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public Render saveImage(@Read(key = "data") String jsonData)
			throws Exception {

		if (jsonData != null && !jsonData.equals("")) {
			int result = routerService.fileUploadImage(wxMpService, jsonData);
			return getRender(result);
		}
		return getRenderFail("参数错误");
	}

	/**
	 * 保存文本
	 * 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public Render saveText(@Read(key = "data") String jsonData)
			throws Exception {

		if (jsonData != null && !jsonData.equals("")) {
			int result = routerService.saveText(wxMpService, jsonData);
			return getRender(result);
		}
		return getRenderFail("参数错误");
	}

	/**
	 * 微信api请求入口
	 * @param signature  微信参数
	 * @param nonce  微信参数
	 * @param timestamp  微信参数
	 * @return
	 * @throws Exception
	 */
	public Render api(@Read(key = "signature") String signature,
			@Read(key = "nonce") String nonce,
			@Read(key = "timestamp") String timestamp) throws Exception {

		if (!setRouter) {
			wxMpMessageRouter = new WxMpMessageRouter(wxMpService);
			routerService.setRouter(wxMpMessageRouter);
			setRouter = true;
		}

		if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
			return getRender("非法请求");

		}

		getResponse().setContentType("text/html;charset=utf-8");
		getResponse().setStatus(HttpServletResponse.SC_OK);

		String encryptType = StringUtils.isBlank(getRequest().getParameter(
				"encrypt_type")) ? "raw" : getRequest().getParameter(
				"encrypt_type");
		
		if ("raw".equals(encryptType)) {
			System.out.println("明文传输");
			// 明文传输的消息
			WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(getRequest()
					.getInputStream());
			WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
			System.out.println("微信消息:" + inMessage.toString());
			System.out.println("-------------------");

			
			if(outMessage == null){
				outMessage = routerService.dispose(outMessage, inMessage,
						wxMpService);
			}
			if (outMessage != null)
				getResponse().getWriter().write(outMessage.toXml());
			else{
				getResponse().getWriter().close();
			}
		}
		return getRender("success");
	}

	/**
	 * 设置菜单
	 * @return
	 * @throws WxErrorException
	 */
	public Render setMenu() throws WxErrorException {

		routerService.setMenu(wxMpService);
		return getRender("success");

	}

	/**
	 * 查询当前设置的菜单
	 * @return
	 * @throws WxErrorException
	 */
	public Render queryMenu() throws WxErrorException {
		routerService.queryMenu(wxMpService);
		return getRender("success");
	}

	/**
	 * 删除微信菜单
	 * @return
	 * @throws WxErrorException
	 */
	public Render deleteMenu() throws WxErrorException {
		routerService.deleteMenu(wxMpService);
		return getRender("success");
	}

	/**
	 * 获取1931资源
	 * @return
	 * @throws Exception
	 */
	public Render getWx1931() throws Exception {

		//为1表示自拍萌照
		List<Wx1931> list = routerService.getWx1931(1);
		List<Property> proList = new ArrayList<Property>();
		for (Wx1931 wx : list) {
			Property pro = new Property();
			pro.put("url", wx.getUrl());
			proList.add(pro);
		}
		return getRender(proList);
	}

	/**
	 * 图文信息（未使用）
	 * @return
	 * @throws WxErrorException
	 */
	public Render mass() throws WxErrorException {
		routerService.massGroupMessageSend(wxMpService);
		return getRender("success");
	}
	
	/**
	 * 红队介绍页
	 * @return
	 * @throws Exception
	 */
	public Return getRed() throws Exception {

		return getForward("red.jsp");
	}
	
	/**
	 * 白队介绍页
	 * @return
	 * @throws Exception
	 */
	public Return getWhite() throws Exception {

		return getForward("white.jsp");
	}
	
	
	
	/**
	 * 上传视频 (大小只能在10M以内，暂时不考虑使用)
	 * @return
	 * @throws Exception
	 */
	public Render uploadVideo(@Read(key = "data") String jsonData) throws Exception {
		
		System.out.println("-------------上传视频到微信服务器------------------------");
		JSONObject jo = JSON.parseObject(jsonData);
		String filePath = (String) jo.get("filePath");
		String desc = (String) jo.get("desc");
		String title = (String) jo.get("title");
		int result = routerService.fileUploadVideo(wxMpService, filePath, desc, title);
		return getRender(result);
	}
	
	/**
	 * 后台主页
	 * @return
	 * @throws Exception
	 */
	public Return main() throws Exception {

		return getForward("main.jsp");
	}
	
	public void reSetRouter(){
		setRouter = false;
	}

}
