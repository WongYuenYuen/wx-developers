/**
 * sign_up 表 dao
 * @author xieyong
 *
 */
package com.yy.ent.wx.dao;

import java.util.List;

import com.yy.ent.cherroot.condition.DBCondition;
import com.yy.ent.cherroot.core.datasource.DBEnv;
import com.yy.ent.cherroot.core.datasource.base.DBEnvImpl;
import com.yy.ent.cherroot.entity.EntityBeanDao;
import com.yy.ent.commons.base.inject.Inject;
import com.yy.ent.wx.common.model.Router;

public class RouterDao extends EntityBeanDao<Router> {

    @Inject(instance = DBEnvImpl.class)
    private DBEnv dbEnv;

    public DBEnv getDataSource() {
        return dbEnv;
    }
}