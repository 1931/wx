package com.yy.ent.wx.action;

import com.yy.ent.wx.base.BaseAction;
import com.yy.ent.cherrice.annotation.Get;
import com.yy.ent.cherrice.annotation.Interceptor;
import com.yy.ent.cherrice.annotation.Read;
import com.yy.ent.cherrice.ret.Forward;
import com.yy.ent.cherrice.ret.Render;
import com.yy.ent.clients.dfs.ha.FastDfsProxyHalbService;
import com.yy.ent.commons.base.http.HttpUtil;
import com.yy.ent.commons.base.inject.Inject;
import com.yy.ent.commons.base.valid.Validation;
import com.yy.ent.wx.common.model.Demo;
import org.apache.log4j.Logger;
import com.yy.ent.wx.service.DemoService;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wangqitao
 */
public class DemoAction extends BaseAction {

    private Logger logger = Logger.getLogger(DemoAction.class);

    @Inject(instance = DemoService.class)
    private DemoService wxService;


    @Get(encode = "UTF-8")
    @Interceptor(id = "AuthInterceptor")
    public Render checkValidString(@Read(key = "t") String test) {
        String ip = HttpUtil.getIpAddr(getRequest());
        logger.info("ip:" + ip);
        return getRender(test);
    }

    public Render checkValidLong(@Read(key = "t") Long test) {
        return getRender(test);
    }

    public Render checkValidList(@Read(key = "t") List<Short> test) {

        return getRender(test);
    }

    public Render checkValidLinkList(@Read(key = "t") LinkedList<Long> test) {
        return getRender(test);
    }

    public Render checkValidArryLong(@Read(key = "t") Long[] test) {
        return getRender(test);
    }

    public Render checkValidArryString(@Read(key = "t") String[] test) {
        return getRender(test);
    }

    public Forward toHome() {
        return getForward("index.jsp");
    }


    public Render querySignUp(@Read(key = "uid") String uid) throws Exception {

        Validation.checkNumeric("uid", uid);
        Demo signUp = wxService.query(uid);
        return getRender(signUp);
    }

}
