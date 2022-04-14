package org.colm.code.lock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;
import org.colm.code.lock.po.LockWithDatabase;

import java.util.Date;

public interface LockMapper extends BaseMapper<LockWithDatabase> {

    @Update({"UPDATE tbl_lock SET dup_cont = dup_count + 1, update_time = #{param2} WHERE lock_key = #{param1}"})
    void updateCountByKey(String key, Date current);
}
