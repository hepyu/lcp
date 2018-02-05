package com.open.lcp.core.feature.user.service.dao;

import com.open.lcp.core.env.LcpResource;
import com.open.lcp.core.feature.user.service.dao.entity.UserDetailInfoEntity;
import com.open.lcp.orm.jade.annotation.DAO;
import com.open.lcp.orm.jade.annotation.ReturnGeneratedKeys;
import com.open.lcp.orm.jade.annotation.SQL;

@DAO(catalog = LcpResource.dbAnnotationName_lcp_mysql_core_feature_user_master)
public interface UserDetailInfoDAO {

	String SQL_SELECT_PART = "user_id, user_name, nick_name, avatar, gender, update_ip, regist_ip, update_time, regist_time, last_login_time, description, user_category";

	String SQL_INSERT_PART = "user_id, user_name, nick_name, avatar, gender, update_ip, regist_ip, update_time, regist_time, last_login_time, description, user_category";

	//@ReturnGeneratedKeys
	@SQL("INSERT INTO passport_user_account(" + SQL_INSERT_PART
			+ ") values(:1.userId, :1.userName, :1.nickName, :1.avatar, :1.gender, :1.updateIp, :1.registIp, :1.updateTime, :1.registTime, :1.lastLoginTime, :1.description, :1.userCategory) on duplicate key update user_name=:1.userName, nick_name=:1.nickName, avatar=:1.avatar, gender=:1.gender, update_time=:1.updateTime, update_ip=:1.updateIp, description=:1.description")
	public Long insertOrUpdate(UserDetailInfoEntity passportUserAccountEntity);

	@SQL("INSERT IGNORE INTO passport_user_account(" + SQL_SELECT_PART
			+ ") values(:1.userId, :1.userName, :1.nickName, :1.avatar, :1.gender, :1.updateIp, :1.registIp, :1.updateTime, :1.registTime, :1.description)")
	public int insert(UserDetailInfoEntity userDetailInfoEntity);

	@SQL("UPDATE passport_user_account SET last_login_time=:1.lastLoginTime WHERE user_id=:1.userId")
	public int resetLastLoginTime(UserDetailInfoEntity userDetailInfoEntity);

	@SQL("SELECT " + SQL_SELECT_PART + " from passport_user_account WHERE user_id=:1 LIMIT 1")
	public UserDetailInfoEntity getUserInfo(Long userId);

	@SQL("UPDATE passport_user_account SET gender=:2 WHERE user_id=:1")
	public int updateGender(Long userId, int gender);

	@SQL("UPDATE passport_user_account SET nick_name=:2 WHERE user_id=:1")
	public int updateNickName(Long userId, String nickName);

	@SQL("UPDATE passport_user_account SET avatar=:2 WHERE user_id=:1")
	public int updateAvatar(Long userId, String avatar);

	@SQL("UPDATE passport_user_account SET description=:2 WHERE user_id=:1")
	public int updateDescription(Long userId, String description);

	@SQL("DELETE FROM passport_user_account WHERE user_id=:1")
	public int delUserDetailInfo(Long userId);

}
