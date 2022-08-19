package com.cumulus.repository;

import com.cumulus.entity.LogFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 日志归档实体对象 数据库操作接口
 *
 * @author : shenjc
 */
@Repository
public interface LogFileRepository extends JpaRepository<LogFile, Long>, JpaSpecificationExecutor<LogFile> {
}
