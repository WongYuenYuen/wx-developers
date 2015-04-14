package com.yy.ent.wx.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxMenu;
import me.chanjar.weixin.common.bean.WxMenu.WxMenuButton;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
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
import com.yy.ent.wx.dao.NewsDao;
import com.yy.ent.wx.dao.RouterDao;
import com.yy.ent.wx.dao.TextDao;
import com.yy.ent.wx.dao.VideoDao;
import com.yy.ent.wx.dao.VoiceDao;
import com.yy.ent.wx.dao.Wx1931Dao;
import com.yy.ent.wx.service.base.BaseService;

public class RouterService extends BaseService {

	private Logger logger = Logger.getLogger(RouterService.class);

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
	 * @return 返回修改记录数,新增时返回新增的id
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
		yszx2.setName("自拍萌照");
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

		System.out.println("---------红队或白队---------------" + type);
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

		for (News news : list) {
			WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
			item.setDescription(news.getDescription());
			item.setPicUrl(news.getPicUrl());
			item.setTitle(news.getTitle());
			item.setUrl(news.getUrl());
			nb = nb.addArticle(item);
		}
		return nb;
	}

	/**
	 * 微信公众号事件处理、彩蛋区
	 * 
	 * @param outMessage
	 * @param inMessage
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public WxMpXmlOutMessage dispose(WxMpXmlOutMessage outMessage,
			WxMpXmlMessage inMessage, HttpServletResponse response) {

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

				outMessage = WxMpXmlOutMessage.TEXT().content("回复‘吃货’试试")
						.fromUser(inMessage.getToUserName())
						.toUser(inMessage.getFromUserName()).build();
			} else if (eventKey.equals("我们的歌")) {

				// 发送图文消息
				NewsBuilder nb = WxMpXmlOutMessage.NEWS();
				outMessage = newNews(nb, 3).fromUser(inMessage.getToUserName())
						.toUser(inMessage.getFromUserName()).build();
			}
		}
		// 对语音事件的处理
		if (msgType.equals("voice")) {

			boolean save = false;
			if (save) {
				Voice voice = new Voice();
				voice.setDesc("来自粉丝");
				voice.setMedia_id(inMessage.getMediaId());
				try {
					voiceDao.insert(voice);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
			}
			outMessage = WxMpXmlOutMessage
					.VOICE()
					.mediaId(
							"5T7uBvbnl8-AAc5DrVPUu3qO2AnMMcxjXDfCBBUKX5A0eG_JhJL-4fUX4ponSbHc")
					.fromUser(inMessage.getToUserName())
					.toUser(inMessage.getFromUserName()).build();
		}
		// 对图片事件的处理
		if (msgType.equals("image")) {

		}
		// 对短视频事件的处理
		if (msgType.equals("shortvideo")) {

		}
		return outMessage;
	}

	/**
	 * 获取图文信息列表
	 * 
	 * @param type
	 *            为1 表示自拍萌照
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
				System.out.println("上传文件:" + savePath + "\\"
						+ md5Name + ".jpg");
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
				FileItem itemFile = list.get(list.size()-1);
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
}
