package com.open.lcp.framework.core.api.service.dao;

import java.util.List;

import com.open.jade.jade.annotation.DAO;
import com.open.jade.jade.annotation.SQL;
import com.open.lcp.framework.core.api.service.dao.entity.LcpIgnoreMethodLogEntity;

@DAO(catalog = "lcp")
public interface LcpIgnoreMethodLogDAO {

	@SQL("SELECT method_name, ctime FROM lcp_sys_config_ignore_log_method ORDER BY method_name ASC")
	public List<LcpIgnoreMethodLogEntity> listAll();

}