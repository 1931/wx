package com.yy.ent.wx.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxMenu;
import me.chanjar.weixin.common.bean.WxMenu.WxMenuButton;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpCustomMessage;
import me.chanjar.weixin.mp.bean.WxMpCustomMessage.WxArticle;
import me.chanjar.weixin.mp.bean.WxMpMassGroupMessage;
import me.chanjar.weixin.mp.bean.WxMpMassNews;
import me.chanjar.weixin.mp.bean.WxMpMassNews.WxMpMassNewsArticle;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutImageMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutNewsMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutTextMessage;
import me.chanjar.weixin.mp.bean.outxmlbuilder.NewsBuilder;
import me.chanjar.weixin.mp.bean.result.WxMpMassUploadResult;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yy.ent.cherroot.condition.DBCondition;
import com.yy.ent.cherroot.condition.DBCondition.OrderType;
import com.yy.ent.commons.base.dto.Property;
import com.yy.ent.commons.base.inject.Inject;
import com.yy.ent.wx.common.constants.Constants;
import com.yy.ent.wx.common.model.Image;
import com.yy.ent.wx.common.model.LocalMedia;
import com.yy.ent.wx.common.model.News;
import com.yy.ent.wx.common.model.Router;
import com.yy.ent.wx.common.model.Text;
import com.yy.ent.wx.common.model.Video;
import com.yy.ent.wx.common.model.Voice;
import com.yy.ent.wx.common.model.Wx1931;
import com.yy.ent.wx.common.util.MessageType;
import com.yy.ent.wx.dao.ImageDao;
import com.yy.ent.wx.dao.LocalMediaDao;
import com.yy.ent.wx.dao.MultiDao;
import com.yy.ent.wx.dao.NewsDao;
import com.yy.ent.wx.dao.RouterDao;
import com.yy.ent.wx.dao.TextDao;
import com.yy.ent.wx.dao.VideoDao;
import com.yy.ent.wx.dao.VoiceDao;
import com.yy.ent.wx.dao.Wx1931Dao;
import com.yy.ent.wx.service.base.BaseService;

public class RouterService extends BaseService {

	private Logger logger = Logger.getLogger(RouterService.class);

	@Inject(instance = MultiDao.class)
	protected MultiDao multiDao = new MultiDao(null);

	@Inject(instance = RouterDao.class)
	protected RouterDao routerDao;

	@Inject(instance = TextDao.class)
	protected TextDao textDao;

	@Inject(instance = ImageService.class)
	protected ImageService imageService;

	@Inject(instance = ImageDao.class)
	protected ImageDao imageDao;

	@Inject(instance = NewsDao.class)
	protected NewsDao newsDao;

	@Inject(instance = Wx1931Dao.class)
	protected Wx1931Dao wx1931Dao;

	@Inject(instance = VoiceDao.class)
	protected VoiceDao voiceDao;

	@Inject(instance = VideoDao.class)
	protected VideoDao videoDao;

	@Inject(instance = LocalMediaDao.class)
	protected LocalMediaDao localMediaDao;

	/**
	 * 
	 * @param wxMpService
	 * @param jsonData
	 *            图片类Image的json
	 * @return 返回修改记录数
	 */
	public int fileUploadImage(WxMpService wxMpService, String jsonData) {

		int result = 0;
		JSONObject jo = JSON.parseObject(jsonData);
		String url = (String) jo.get("url");
		String desc = (String) jo.get("desc");
		String str = (String) jo.get("id");
		int id = Integer.valueOf(str);
		try {
			// 0为新增
			if (id == 0) {
				String fileUrl = Constants.UPLOAD_FILE_PATH
						+ url.substring(url.lastIndexOf("/"), url.length());
				System.out.println("微信上传文件url:" + fileUrl);
				InputStream inputStream = new FileInputStream(new File(fileUrl));
				WxMediaUploadResult res = wxMpService.mediaUpload(
						WxConsts.MEDIA_IMAGE, WxConsts.FILE_JPG, inputStream);
				Image image = new Image();
				image.setDesc(desc);
				image.setMedia_id(res.getMediaId());
				image.setCreateTime(new Date());
				image.setUrl(url);
				result = imageService.save(image);

			} else {
				String fileUrl = Constants.UPLOAD_FILE_PATH
						+ url.substring(url.lastIndexOf("/"), url.length());
				System.out.println("微信上传文件url:" + fileUrl);
				InputStream inputStream = new FileInputStream(new File(fileUrl));
				WxMediaUploadResult res = wxMpService.mediaUpload(
						WxConsts.MEDIA_IMAGE, WxConsts.FILE_JPG, inputStream);
				Image image = imageDao.query((long) id);
				image.setDesc(desc);
				image.setMedia_id(res.getMediaId());
				image.setCreateTime(new Date());
				image.setUrl(url);
				result = imageDao.update(image);
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 此接口还没被使用
	 * 
	 * @param wxMpService
	 * @param filePath
	 * @param id
	 * @param desc
	 * @return
	 */
	public int fileUploadVoice(WxMpService wxMpService, String filePath,
			String desc) {

		int result = 0;
		try {
			InputStream inputStream = new FileInputStream(new File(filePath));
			WxMediaUploadResult res = wxMpService.mediaUpload(
					WxConsts.MEDIA_VOICE, WxConsts.FILE_ARM, inputStream);

			Voice voice = new Voice();
			voice.setDesc("音频");
			voice.setMedia_id(res.getMediaId());
			result = voiceDao.insert(voice);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 上传视频
	 * 
	 * @param wxMpService
	 * @param filePath
	 * @param id
	 * @param desc
	 * @return
	 */
	public int fileUploadVideo(WxMpService wxMpService, String filePath,
			String desc, String title) {

		int result = 0;
		try {
			InputStream inputStream = new FileInputStream(new File(filePath));
			WxMediaUploadResult res = wxMpService.mediaUpload(
					WxConsts.MEDIA_VIDEO, WxConsts.FILE_MP4, inputStream);

			Video video = new Video();
			video.setDesc(desc);
			video.setMedia_id(res.getMediaId());
			video.setTitle(title);
			result = videoDao.insert(video);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 给微信服务器设置路由(暂时支持文本路由和图片路由)
	 * 
	 * @param wxMpMessageRouter
	 * @return
	 */
	public List<Property> setRouter(WxMpMessageRouter wxMpMessageRouter) {

		try {
			DBCondition db = new DBCondition();
			db.addOrder("sortord", OrderType.ASC);
			List<Router> list = routerDao.query(db);
			for (Router router : list) {
				System.out.println("按顺序查询出来的路由：" + router.getIntercept());
				int type = router.getType();
				int type_id = router.getType_id();
				String intercept = router.getIntercept();

				switch (type) {
				case MessageType.WX_TEXT:
					Text text = textDao.query((long) type_id);
					wxMpMessageRouter = setTextHandler(intercept,
							text.getContent(), wxMpMessageRouter);
					break;
				case MessageType.WX_IMAGE:
					Image image = imageDao.query((long) type_id);
					wxMpMessageRouter = setImageHandler(intercept,
							image.getMedia_id(), wxMpMessageRouter);
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 辅助方法
	 * 
	 * @param intercept
	 * @param media_id
	 * @param wxMpMessageRouter
	 * @return
	 */
	public WxMpMessageRouter setImageHandler(String intercept,
			final String media_id, WxMpMessageRouter wxMpMessageRouter) {

		WxMpMessageHandler handler = new WxMpMessageHandler() {
			@Override
			public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
					Map<String, Object> context, WxMpService wxMpService,
					WxSessionManager arg3) throws WxErrorException {
				WxMpXmlOutImageMessage m = WxMpXmlOutMessage.IMAGE()
						.mediaId(media_id).fromUser(wxMessage.getToUserName())
						.toUser(wxMessage.getFromUserName()).build();
				return m;
			}
		};

		return wxMpMessageRouter.rule().async(false).content(intercept) // 拦截内容为“哈哈”的消息
				.handler(handler).end();
	}

	/**
	 * 辅助方法
	 * 
	 * @param intercept
	 * @param concent
	 * @param wxMpMessageRouter
	 * @return
	 */
	public WxMpMessageRouter setTextHandler(String intercept,
			final String concent, WxMpMessageRouter wxMpMessageRouter) {

		System.out.println("设置文字路由,拦截的文字为：" + intercept);
		WxMpMessageHandler handler = new WxMpMessageHandler() {
			@Override
			public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
					Map<String, Object> context, WxMpService wxMpService,
					WxSessionManager arg3) throws WxErrorException {
				WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT()
						.content(concent).fromUser(wxMessage.getToUserName())
						.toUser(wxMessage.getFromUserName()).build();
				return m;
			}
		};

		return wxMpMessageRouter.rule().async(false).content(intercept) // 拦截内容为“哈哈”的消息
				.handler(handler).end();
//		return wxMpMessageRouter.rule().async(false).rContent(intercept+"*")// 拦截内容为“哈哈”的消息
//				.handler(handler).end();
	}

	/**
	 * 设置微信公众号菜单
	 * 
	 * @param wxMpService
	 * @return
	 * @throws WxErrorException
	 */
	public boolean setMenu(WxMpService wxMpService) {

		// 设置菜单
		WxMenu wxMenu = new WxMenu();
		WxMenuButton caidan = new WxMenuButton();
		caidan.setKey("caidan");
		caidan.setType("click");
		caidan.setName("彩蛋区");

		WxMenuButton yszx = new WxMenuButton();
		yszx.setName("一手资讯");
		List<WxMenuButton> subYszx = new ArrayList<WxMenuButton>(5);
		// WxMenuButton yszx1 = new WxMenuButton();
		// yszx1.setUrl("http://www.baidu.com");
		// yszx1.setName("推送历史");
		// yszx1.setType("view");
		WxMenuButton yszx2 = new WxMenuButton();
		yszx2.setUrl("http://www.1931.com/dream/mobile/newsPhotos.action");
		yszx2.setName("精彩留影");
		yszx2.setType("view");
		WxMenuButton yszx3 = new WxMenuButton();
		yszx3.setUrl("http://www.1931.com/dream/mobile/newsideos.action");
		yszx3.setName("热门视频");
		yszx3.setType("view");
		WxMenuButton yszx4 = new WxMenuButton();
		yszx4.setKey("我们的歌");
		yszx4.setName("我们的歌");
		yszx4.setType("click");
		WxMenuButton yszx5 = new WxMenuButton();
		yszx5.setUrl("http://bbs.1931.com/thread-20512-1-1.html");
		yszx5.setName("直播时间表");
		yszx5.setType("view");
		// subYszx.add(yszx1);
		subYszx.add(yszx2);
		subYszx.add(yszx3);
		subYszx.add(yszx4);
		subYszx.add(yszx5);
		yszx.setSubButtons(subYszx);

		WxMenuButton fans = new WxMenuButton();
		fans.setName("粉丝区");
		List<WxMenuButton> subFans = new ArrayList<WxMenuButton>(3);
		WxMenuButton fans1 = new WxMenuButton();
		fans1.setKey("白队");
		fans1.setName("白队");
		fans1.setType("click");
		WxMenuButton fans2 = new WxMenuButton();
		fans2.setKey("红队");
		fans2.setName("红队");
		fans2.setType("click");
		WxMenuButton fans3 = new WxMenuButton();
		fans3.setUrl("http://bbs.1931.com/");
		fans3.setName("讨论区");
		fans3.setType("view");
		subFans.add(fans1);
		subFans.add(fans2);
		subFans.add(fans3);
		fans.setSubButtons(subFans);

		List<WxMenuButton> lists = new ArrayList<WxMenuButton>(2);
		lists.add(yszx);
		lists.add(fans);
		lists.add(caidan);

		wxMenu.setButtons(lists);
		try {
			wxMpService.menuCreate(wxMenu);
		} catch (WxErrorException e) {
			logger.error(e);
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 查询微信公众号菜单
	 * 
	 * @param wxMpService
	 * @throws WxErrorException
	 */
	public void queryMenu(WxMpService wxMpService) {
		WxMenu wxMenu = null;
		try {
			wxMenu = wxMpService.menuGet();
		} catch (WxErrorException e) {
			logger.error(e);
			e.printStackTrace();
		}
		System.out.println("=========menu============");
		System.out.println(wxMenu.toJson());
	}

	/**
	 * 删除微信公众号菜单
	 * 
	 * @param wxMpService
	 * @throws WxErrorException
	 */
	public void deleteMenu(WxMpService wxMpService) {
		try {
			wxMpService.menuDelete();
		} catch (WxErrorException e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	/**
	 * 构建图文信息列表
	 * 
	 * @param nb
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public NewsBuilder newNews(NewsBuilder nb, int type) {

		DBCondition db = new DBCondition();
		db.addCondition("type", type);
		db.addOrder("sortord", OrderType.ASC);
		List<News> list = null;
		try {
			list = newsDao.query(db);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		int count = 0;
		for (News news : list) {
			WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
			item.setDescription(news.getDescription());
			item.setPicUrl(news.getPicUrl());
			item.setTitle(news.getTitle());
			if (type == 4) {
				item.setUrl("http://mynona.xicp.net/wx/video.jsp?url="
						+ news.getUrl());
			} else {
				item.setUrl(news.getUrl());
			}
			nb = nb.addArticle(item);
			count++;
			if (count > 9)
				break;
		}
		return nb;
	}

	public int getRandomVideosByName(NewsBuilder nb, int type, String name) {

		List<Property> pros = multiDao.queryCollection("newVideo", name, name, name);
		if (pros.size() > 10) {
			int[] index = randomArray(0, pros.size() - 1, 10);
			for (int i = 0; i < index.length; i++) {
				Property pro = pros.get(index[i]);
				WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
				item.setDescription(pro.get("description"));
				item.setPicUrl(pro.get("picUrl"));
				item.setTitle(pro.get("title"));
				item.setUrl(pro.get("url"));
				nb = nb.addArticle(item);
			}
		} else {
			for (Property pro : pros) {
				WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
				item.setDescription(pro.get("description"));
				item.setPicUrl(pro.get("picUrl"));
				item.setTitle(pro.get("title"));
				item.setUrl(pro.get("url"));
				nb = nb.addArticle(item);
			}
		}
		return pros.size();
	}

	/**
	 * 发送符合条件的一张图片
	 * 
	 * @param ib
	 * @param name
	 *            图片描述
	 * @return
	 * @throws WxErrorException
	 * @throws IOException
	 */
	public int getImageByName(String user, String name, WxMpService wxMpService)
			throws WxErrorException {

		List<Property> pros = multiDao.queryCollection("image", name, name);
		int count = 5;
		count = pros.size() > 5 ? 5 : pros.size();
		int[] index = randomArray(0, pros.size() - 1, count);
		System.out.println("------------回复图片size---------" + pros.size());
		System.out.println("------------回复图片count---------" + count);
		for (int i = 0; i < count; i++) {
			System.out.println("------------回复图片下标---------" + index[i]);
			Property pro = pros.get(index[i]);
			System.out.println("------------回复图片---------"
					+ pro.get("description"));
			WxMpCustomMessage message = WxMpCustomMessage.IMAGE().toUser(user)
					.mediaId(pro.get("media_id")).build();
			wxMpService.customMessageSend(message);
		}

		return count;
	}

	public int getVoiceByName(String user, String name, WxMpService wxMpService)
			throws WxErrorException {

		List<Property> pros = multiDao.queryCollection("voice", name);
		int count = 5;
		count = pros.size() > 5 ? 5 : pros.size();
		int[] index = randomArray(0, pros.size() - 1, count);
		for (int i = 0; i < count; i++) {
			Property pro = pros.get(index[i]);
			WxMpCustomMessage message = WxMpCustomMessage.VOICE().toUser(user)
					.mediaId(pro.get("media_id")).build();
			wxMpService.customMessageSend(message);
		}
		return count;
	}

	/**
	 * 微信公众号事件处理、彩蛋区
	 * 
	 * @param outMessage
	 * @param inMessage
	 * @param response
	 * @return
	 * @throws WxErrorException
	 * @throws Exception
	 */
	public WxMpXmlOutMessage dispose(WxMpXmlOutMessage outMessage,
			WxMpXmlMessage inMessage, WxMpService wxMpService)
			throws WxErrorException {

		String msgType = inMessage.getMsgType();

		// 对菜单事件的处理
		if (msgType.equals("event")) {
			System.out.println("-----------event-------------------");
			String eventKey = inMessage.getEventKey();
			if (eventKey.equals("白队")) {
				NewsBuilder nb = WxMpXmlOutMessage.NEWS();
				outMessage = newNews(nb, 1).fromUser(inMessage.getToUserName())
						.toUser(inMessage.getFromUserName()).build();

			} else if (eventKey.equals("红队")) {
				NewsBuilder nb = WxMpXmlOutMessage.NEWS();
				outMessage = newNews(nb, 2).fromUser(inMessage.getToUserName())
						.toUser(inMessage.getFromUserName()).build();

			} else if (eventKey.equals("caidan")) {

				String contentText = "试试回复任意内容，比如:\n"
						+ getEggshellContent(5) ;
				outMessage = WxMpXmlOutMessage.TEXT().content(contentText)
						.fromUser(inMessage.getToUserName())
						.toUser(inMessage.getFromUserName()).build();
			} else if (eventKey.equals("我们的歌")) {

				// 发送图文消息
				NewsBuilder nb = WxMpXmlOutMessage.NEWS();
				outMessage = newNews(nb, 3).fromUser(inMessage.getToUserName())
						.toUser(inMessage.getFromUserName()).build();
			}
			//给新用户的回复
			if(inMessage.getEvent().equals("subscribe")){
				String content = "欢迎关注1931粉丝团，么么哒!";
				WxMpCustomMessage message = WxMpCustomMessage.TEXT().toUser(inMessage.getFromUserName())
						.content(content).build();
				wxMpService.customMessageSend(message);
			}
		}
		// 对语音事件的处理
		if (msgType.equals("voice")) {

			// 保存语音资源
			if (inMessage.getFromUserName().equals(
					"oD6flst5M4hp6jinlGwvpXf982o8")) {
				Voice voice = new Voice();
				voice.setDesc("来自mynona");
				voice.setMedia_id(inMessage.getMediaId());
				try {
					voiceDao.insert(voice);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
				outMessage = WxMpXmlOutMessage.VOICE()
						.mediaId(inMessage.getMediaId())
						.fromUser(inMessage.getToUserName())
						.toUser(inMessage.getFromUserName()).build();
			}
			if (inMessage.getFromUserName().equals(
					"oD6flsi1L6NDiryqefCalDF1k6XE")) {
				Voice voice = new Voice();
				voice.setDesc("来自Yuen");
				voice.setMedia_id(inMessage.getMediaId());
				try {
					voiceDao.insert(voice);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
				outMessage = WxMpXmlOutMessage.VOICE()
						.mediaId(inMessage.getMediaId())
						.fromUser(inMessage.getToUserName())
						.toUser("oD6flst5M4hp6jinlGwvpXf982o8").build();
			}
		}
		// 对图片事件的处理
		if (msgType.equals("image")) {

			String contentText = "试回复:\n" + getEggshellContent(3);
			outMessage = WxMpXmlOutMessage.TEXT().content(contentText)
					.fromUser(inMessage.getToUserName())
					.toUser(inMessage.getFromUserName()).build();
		}
		// 对短视频事件的处理
		if (msgType.equals("shortvideo")) {

			String contentText = "试回复:\n" + getEggshellContent(3);
			outMessage = WxMpXmlOutMessage.TEXT().content(contentText)
					.fromUser(inMessage.getToUserName())
					.toUser(inMessage.getFromUserName()).build();
		}
		// 对文本时间的处理
		if (msgType.equals("text")) {

			outMessage = disposeText(outMessage, inMessage, wxMpService);
		}

		return outMessage;
	}

	public WxMpXmlOutMessage disposeText(WxMpXmlOutMessage outMessage,
			WxMpXmlMessage inMessage, WxMpService wxMpService)
			throws WxErrorException {

		String content = inMessage.getContent();
		int count = 0;
		boolean isNull = true;

		NewsBuilder nb = WxMpXmlOutMessage.NEWS();
		count += getRandomVideosByName(nb, 4, content);
		outMessage = nb.fromUser(inMessage.getToUserName())
				.toUser(inMessage.getFromUserName()).build();
		if (count != 0) {
			isNull = false;
		}
		count += getImageByName(inMessage.getFromUserName(), content,
				wxMpService);

		// 语音
		count += getVoiceByName(inMessage.getFromUserName(), content,
				wxMpService);

		if (count == 0) {
			String contentText = "回复以下试试:\n" + getEggshellContent(3);
			outMessage = WxMpXmlOutMessage.TEXT().content(contentText)
					.fromUser(inMessage.getToUserName())
					.toUser(inMessage.getFromUserName()).build();
			isNull = false;
		}

		if (isNull)
			return null;
		else
			return outMessage;
	}

	/**
	 * 获取图文信息列表
	 * 
	 * @param type
	 *            为1表示自拍萌照
	 * @return
	 * @throws Exception
	 */
	public List<Wx1931> getWx1931(int type) throws Exception {

		DBCondition db = new DBCondition();
		db.addCondition("type", type);
		db.addOrder("sortord", OrderType.ASC);
		List<Wx1931> list = wx1931Dao.query(db);
		return list;
	}

	/**
	 * 群发接口（未使用）
	 * 
	 * @param wxMpService
	 * @throws WxErrorException
	 */
	public void massGroupMessageSend(WxMpService wxMpService)
			throws WxErrorException {
		WxMpMassGroupMessage message = new WxMpMassGroupMessage();
		message.setMsgtype(WxConsts.MASS_MSG_NEWS);
		message.setContent("消息描述");
		message.setMediaId("VSrTjv0ET7b_N6wvbAeT-kQ4eXC4VNL6b0Q7_zJKv1vCs7hylLDWdfbi8E8m8yoG");
		wxMpService.massGroupMessageSend(message);
	}

	/**
	 * 群发图文上传接口 （未使用）
	 * 
	 * @param wxMpService
	 * @return
	 * @throws WxErrorException
	 */
	public WxMpMassUploadResult massNewsUpload(WxMpService wxMpService)
			throws WxErrorException {

		WxMpMassNewsArticle article = new WxMpMassNewsArticle();
		article.setAuthor("yingjie");
		article.setContent("content");
		article.setContentSourceUrl("http://www.baidu.com");
		article.setDigest("digest");
		article.setShowCoverPic(true);
		article.setTitle("title");
		article.setThumbMediaId("7MZopX2YXSxDzLqiSIuBlQYysFStWOTBTN9FfCHq0GdbOmdDB2V6hgVVPyAmUZz-");

		WxMpMassNews massNews = new WxMpMassNews();
		massNews.addArticle(article);

		return wxMpService.massNewsUpload(massNews);
	}

	/**
	 * 保存路由
	 * 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public int saveRouter(String jsonData) throws Exception {

		Router routerJson = JSON.parseObject(jsonData, Router.class);
		Router router = new Router();
		router.setIntercept(routerJson.getIntercept());
		router.setSortord(routerJson.getSortord());
		router.setType(routerJson.getType());
		router.setType_id(routerJson.getType_id());
		return routerDao.insert(router);
	}

	/**
	 * 更新路由
	 * 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public int updateRouter(String jsonData) throws Exception {

		Router routerJson = JSON.parseObject(jsonData, Router.class);
		Router router = routerDao.query((long) routerJson.getId());
		router.setIntercept(routerJson.getIntercept());
		router.setSortord(routerJson.getSortord());
		router.setType(routerJson.getType());
		router.setType_id(routerJson.getType_id());
		return routerDao.update(router);
	}

	/**
	 * 删除路由
	 * 
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public int deleteRouter(String jsonData) throws Exception {

		JSONObject jo = JSON.parseObject(jsonData);
		String str = (String) jo.get("data");
		int id = Integer.valueOf(str);
		Router router = routerDao.query((long) id);
		return routerDao.delete(router);
	}

	public int deleteImage(String jsonData) throws Exception {

		JSONObject jo = JSON.parseObject(jsonData);
		String str = (String) jo.get("data");
		int id = Integer.valueOf(str);
		Image image = imageDao.query((long) id);
		return imageDao.delete(image);
	}

	public int deleteText(String jsonData) throws Exception {

		JSONObject jo = JSON.parseObject(jsonData);
		String str = (String) jo.get("data");
		int id = Integer.valueOf(str);
		Text text = textDao.query((long) id);
		return textDao.delete(text);
	}

	/**
	 * 获取路由列表
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Property> getRouterList() {

		List<Property> proList = new ArrayList<Property>();
		try {
			DBCondition db = new DBCondition();
			List<Router> routerList = routerDao.query(db);
			for (Router router : routerList) {
				Property pro = new Property();
				pro.put("id", router.getId());
				pro.put("intercept", router.getIntercept());
				pro.put("sortord", router.getSortord());
				pro.put("type", router.getType());
				pro.put("type_id", router.getType_id());
				int type = router.getType();
				switch (type) {
				case 1:
					Text text = textDao.query((long) router.getType_id());
					if (text != null)
						pro.put("desc", text.getContent());
					break;
				case 2:
					Image image = imageDao.query((long) router.getType_id());
					if (image != null)
						pro.put("desc", image.getDesc());
					break;
				default:
					break;
				}
				proList.add(pro);
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return proList;
	}

	/**
	 * 获取媒体文件
	 * 
	 * @param type
	 *            类型值 1为文本 ， 2为图片
	 * @return
	 * @throws Exception
	 */
	public List getMediaList(int type) {

		List list = null;
		try {
			switch (type) {
			case 1:
				List<Text> texts = new ArrayList<Text>();
				DBCondition db = new DBCondition();
				texts = textDao.query(db);
				return texts;
			case 2:
				DBCondition db2 = new DBCondition();
				List<Property> prolist = new ArrayList<Property>();
				List<Image> images = new ArrayList<Image>();
				images = imageDao.query(db2);
				for (Image image : images) {
					Property pro = new Property();
					pro.put("id", image.getId());
					pro.put("media_id", image.getMedia_id());
					pro.put("url", image.getUrl());
					pro.put("createTime",
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
									.format(image.getCreateTime()));
					pro.put("desc", image.getDesc());
					prolist.add(pro);
				}
				return prolist;

			default:
				break;
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 上传文件
	 * 
	 * @param savePath
	 * @param item
	 * @param md5Name
	 */
	public void uploadFile(String savePath, FileItem item, String md5Name) {

		System.out.println("进入文件上传------------------");
		try {
			System.out.println("上传文件:" + savePath + "\\" + md5Name + ".jpg");
			InputStream in = item.getInputStream();
			FileOutputStream out = new FileOutputStream(savePath + "\\"
					+ md5Name + ".jpg");
			byte buffer[] = new byte[1024];
			int len = 0;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			in.close();
			out.close();
			item.delete();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	/**
	 * 检查MD5 true为已存在文件，不再上传
	 * 
	 * @param md5
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public boolean checkMd5(String md5, int type) throws Exception {

		DBCondition db = new DBCondition();
		db.addCondition("md5", md5);
		List<LocalMedia> lmLists = localMediaDao.query(db);
		if (lmLists != null && lmLists.size() > 0) {
			System.out.println("MD5校验，此文件已存在");
			return true;
		}
		LocalMedia lo = new LocalMedia();
		lo.setMd5(md5);
		lo.setCreateTime(new Date());
		lo.setType(type);
		localMediaDao.insert(lo);
		return false;
	}

	/**
	 * 
	 * @param savePath
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public String uploadImage(HttpServletRequest request) throws Exception {

		String md5Name = null;
		File file = new File(Constants.UPLOAD_FILE_PATH);
		if (!file.exists() && !file.isDirectory()) {
			System.out.println("新建目录：" + Constants.UPLOAD_FILE_PATH);
			file.mkdir();
		}
		try {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("UTF-8");
			if (!ServletFileUpload.isMultipartContent(request)) {
				return "fail";
			}
			List<FileItem> list = upload.parseRequest(request);
			System.out.println("------------listSize--------------"
					+ list.size());
			if (list.size() >= 2) {
				boolean checkMd5 = false;
				FileItem itemMd5 = list.get(0);
				String name = itemMd5.getFieldName();
				if (name.equals("md5")) {
					md5Name = itemMd5.getString("UTF-8");
					checkMd5 = checkMd5(md5Name, 2);
					System.out.println("md5校验结果：" + checkMd5);
				}
				FileItem itemFile = list.get(list.size() - 1);
				if (!checkMd5) {
					uploadFile(Constants.UPLOAD_FILE_PATH, itemFile, md5Name);
				}
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			return "fail";
		}
		return "http://localhost:8080/wx_upload/" + md5Name + ".jpg";
	}

	/**
	 * 
	 * @param wxMpService
	 * @param jsonData
	 * @return
	 * @throws Exception
	 */
	public int saveText(WxMpService wxMpService, String jsonData) {

		int result = 0;
		try {
			JSONObject jo = JSON.parseObject(jsonData);
			String content = (String) jo.get("content");
			String desc = (String) jo.get("desc");
			String str = (String) jo.get("id");
			int id = Integer.valueOf(str);

			System.out.println("content:" + content);
			System.out.println("title:" + desc);
			if (id == 0) {
				Text text = new Text();
				text.setDesc(desc);
				text.setContent(content);
				result = textDao.insert(text);
			} else {
				Text text = textDao.query((long) id);
				text.setDesc(desc);
				text.setContent(content);
				result = textDao.update(text);
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return result;

	}

	/**
	 * 随机指定范围内N个不重复的数 在初始化的无重复待选数组中随机产生一个数放入结果中，
	 * 将待选数组被随机到的数，用待选数组(len-1)下标对应的数替换 然后从len-2里随机产生下一个随机数，如此类推
	 * 
	 * @param max
	 *            指定范围最大值
	 * @param min
	 *            指定范围最小值
	 * @param n
	 *            随机数个数
	 * @return int[] 随机数结果集
	 */
	public static int[] randomArray(int min, int max, int n) {
		int len = max - min + 1;

		if (max < min || n > len) {
			return null;
		}

		// 初始化给定范围的待选数组
		int[] source = new int[len];
		for (int i = min; i < min + len; i++) {
			source[i - min] = i;
		}

		int[] result = new int[n];
		Random rd = new Random();
		int index = 0;
		for (int i = 0; i < result.length; i++) {
			// 待选数组0到(len-2)随机一个下标
			index = Math.abs(rd.nextInt() % len--);
			// 将随机到的数放入结果集
			result[i] = source[index];
			// 将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换
			source[index] = source[len];
		}
		return result;
	}

	public String getEggshellContent(int count) {

		List<Property> images = multiDao.queryCollection("imageEgg");
		int[] imagesIndex = randomArray(0, images.size() - 1, count);
		String imageContent = "";
		if (imagesIndex != null) {
			for (int i = 0; i < imagesIndex.length; i++) {
				Property pro = images.get(imagesIndex[i]);
				if (pro != null)
					imageContent += pro.get("description") + "\n";
			}
		}

		List<Property> voices = multiDao.queryCollection("voiceEgg");
		int[] voicesIndex = randomArray(0, voices.size() - 1, 1);
		String voiceContent = "";
		if (voicesIndex != null) {
			for (int i = 0; i < voicesIndex.length; i++) {
				Property pro = voices.get(voicesIndex[i]);
				if (pro != null)
					voiceContent += pro.get("description") ;
			}
		}

		List<Property> news = multiDao.queryCollection("newsEgg");
		int[] newsIndex = randomArray(0, news.size() - 1, 2);
		String newContent = "";
		if (newsIndex != null) {
			for (int i = 0; i < newsIndex.length; i++) {
				Property pro = news.get(newsIndex[i]);
				if (pro != null)
					newContent += pro.get("description") + "\n";
			}
		}
		return newContent + imageContent + voiceContent ;
	}

}
