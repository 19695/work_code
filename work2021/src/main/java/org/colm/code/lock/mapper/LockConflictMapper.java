package org.colm.code.lock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.colm.code.lock.po.LockConflictWithDatabase;

public interface LockConflictMapper extends BaseMapper<LockConflictWithDatabase> {

    @Insert({"INSERT INTO tbl_lock_conflict " +
            "SELECT * FROM tbl_lock " +
            "WHERE tbl_lock.dup_count > 0 " +
            "AND tbl_lock.lock_key = #{key}"})
    void copyFromLock(String key);

}
