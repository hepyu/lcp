package com.open.lcp.core.feature.user.service.dao;

import java.util.List;

import com.open.lcp.core.env.LcpResource;
import com.open.lcp.core.feature.user.service.dao.entity.UserOAuthInfoEntity;
import com.open.lcp.orm.jade.annotation.DAO;
import com.open.lcp.orm.jade.annotation.SQL;

@DAO(catalog = LcpResource.dbAnnotationName_lcp_mysql_core_feature_user_master)
public interface UserOAuthInfoDAO {

	public static final String SELECT_PART_SQL = " user_id, open_id, type, user_name, nick_name, avatar, gender, update_ip, bind_ip, update_time, bind_time, last_login_time ";

	public static final int MOBILE_ACCOUNT_TYPE = 2;// UserAccountType.mobile.value()

	@SQL("INSERT INTO passport_oauth_account(" + SELECT_PART_SQL
			+ ") values(:1.userId, :1.openId, :1.type, :1.userName, :1.nickName, :1.avatar, :1.gender, :1.updateIp, :1.bindIp, :1.updateTime, :1.bindTime, :1.lastLoginTime) on duplicate key update user_name=:1.nickName, nick_name=:1.nickName, avatar=:1.avatar, gender=:1.gender, update_time=:1.updateTime, update_ip=:1.updateIp")
	public int insertOrUpdate(UserOAuthInfoEntity userOAuthInfoEntity);

	@SQL("UPDATE passport_oauth_account SET last_login_time=:1.lastLoginTime WHERE user_id=:1.userId AND type=:1.type")
	public int resetLastLoginTime(UserOAuthInfoEntity userOAuthInfoEntity);

	@SQL("SELECT user_id FROM passport_oauth_account WHERE open_id=:1 AND type=:2")
	public Long getUserId(String openId, int type);

	@SQL("SELECT * FROM passport_oauth_account WHERE user_id=:1 AND type=:2")
	public List<UserOAuthInfoEntity> getOAuthAccountInfo(Long userId, int type);

	@SQL("SELECT type FROM passport_oauth_account WHERE user_id=:1")
	public List<Integer> getOAuthAccountTypeList(Long userId);

	@SQL("SELECT count(1) FROM passport_oauth_account WHERE user_id=:1 AND type NOT IN(" + MOBILE_ACCOUNT_TYPE + ")")
	public int countAccountExceptMobile(Long userId);

	@SQL("SELECT count(1) FROM passport_oauth_account WHERE user_id=:1 AND type='weichat'")
	public int existWeichatAccount(Long userId);

	@SQL("SELECT count(1) FROM passport_oauth_account WHERE user_id=:1")
	public int existAccount(Long userId);

	// @Deprecated
	// @SQL("DELETE FROM passport_oauth_account WHERE user_id=:1")
	// public int delPassportOAuthAccountByUserId(Long userId);

	@SQL("SELECT " + SELECT_PART_SQL + " FROM passport_oauth_account WHERE user_id=:1")
	public List<UserOAuthInfoEntity> getOAuthAccountList(Long userId);

	@SQL("DELETE FROM passport_oauth_account WHERE user_id=:1 AND type=:2 AND (SELECT t.total FROM (SELECT count(1) as total FROM passport_oauth_account where user_id=:1) AS t) > 1;")
	public int unbindOAuthAccount(Long userId, int userAccountType);

	// 下列方法仅仅用于洗数据，其他场合不准使用
	@SQL("UPDATE passport_oauth_account SET user_name=:2, nick_name=:3, update_time=:4 WHERE user_id=:1 AND type=7")
	public int updateTextField(Long userId, String userName, String nickName, long ts);

	@SQL("DELETE FROM passport_oauth_account WHERE user_id=:1")
	public int delUserOAuthInfo(Long userId);

}
