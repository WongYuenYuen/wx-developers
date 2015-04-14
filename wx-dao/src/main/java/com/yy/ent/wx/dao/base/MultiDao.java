package com.yy.ent.wx.dao.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.yy.ent.cherroot.entity.callback.PreparedStatementSetterImpl;
import com.yy.ent.cherroot.ext.YYGenericDaoImpl;
import com.yy.ent.cherroot.ext.callback.PropertyRsExtractorAndMapperImpl;
import com.yy.ent.commons.base.dto.Property;
import com.yy.ent.commons.base.inject.Inject;

/**
 * 多表操作dao
 * 
 * @author suzhihua
 * @date 2015年3月20日 上午11:54:22
 */
public class MultiDao {
    @Inject(instance = YYGenericDaoImpl.class)
    protected YYGenericDaoImpl yyGenericDaoImpl;
    protected final Logger logger = Logger.getLogger(getClass());

    private Properties configuration;

    /**
     * 初始化sql配置文件
     * 
     * @param conf
     * @author suzhihua
     * @date 2015年3月20日 上午11:43:10
     */
    public MultiDao(String conf) {
        configuration = new Properties();
        FileInputStream is = null;
        try {
            File file = new File(conf);
            File[] listFiles = file.getParentFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });
            for (File file2 : listFiles) {
                is = new FileInputStream(file2);
                configuration.loadFromXML(is);
                IOUtils.closeQuietly(is);
            }
        } catch (Exception e) {
            logger.error("加载文件出错", e);
        }
    }

    /**
     * 取sql
     * 
     * @param key
     * @return
     * @author suzhihua
     * @date 2015年3月20日 上午11:43:13
     */
    public String getSql(String key) {
        String property = configuration.getProperty(key);
        if (property == null) {
            throw new RuntimeException("找不到key=" + key + "的sql语句.");
        }
        return property;
    }

    /**
     * 查询返回列表,无数据/异常时返回size=0的列表
     * 
     * @param key:配置文件sql key
     * @param holder:参数列表
     * @return
     * @author suzhihua
     * @date 2015年3月20日 上午9:48:11
     */
    public List<Property> queryCollection(String key, Object... holder) {
        List<Property> queryCollection = null;
        try {
            queryCollection = yyGenericDaoImpl.queryCollection("", getSql(key), new PreparedStatementSetterImpl(holder), new PropertyRsExtractorAndMapperImpl());
        } catch (Exception e) {
            logger.error("查询数据库出错", e);
            queryCollection = Collections.emptyList();
        }
        return queryCollection;
    }

    /**
     * 查询返回数据行,无数据/异常时返回size=0的Property
     * 
     * @param key:配置文件sql key
     * @param holder:参数列表
     * @return
     * @author suzhihua
     * @date 2015年3月20日 上午9:48:44
     */
    public Property query(String key, Object... holder) {
        Property query = null;
        try {
            query = yyGenericDaoImpl.query("", getSql(key), new PreparedStatementSetterImpl(holder), new PropertyRsExtractorAndMapperImpl());
        } catch (Exception e) {
            logger.error("查询数据库出错", e);
            query = new Property();
        }
        return query;
    }
}
