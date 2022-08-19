package com.cumulus.modules.system.repository;

import com.cumulus.modules.system.entity.DetailedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 明细清单数据接口
 *
 * @author : shenjc
 */
public interface DetailedFileRepository extends JpaRepository<DetailedFile, Long>, JpaSpecificationExecutor<DetailedFile> {
}
