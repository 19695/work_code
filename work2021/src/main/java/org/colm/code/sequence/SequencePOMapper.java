package org.colm.code.sequence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface SequencePOMapper extends BaseMapper<SequencePO> {

    /**
     * 根据 key 查询表记录
     * @param key
     * @return
     */
    SequencePO queryByKey(String key);

    /**
     * 通过 key 和 version 字段来更新 sequence
     * @param sequencePO
     * @return
     */
    Integer updateByKeyAndVersion(SequencePO sequencePO);

    /**
     * 插入第一条记录， key，sequence， ‘0’
     * @param sequencePO
     * @return
     */
    Integer insertFirst(SequencePO sequencePO);

    /**
     * 根据 key 和 sequence 更新表字段 usedStatus
     * @param sequencePO
     * @return
     */
    Integer updateUsedStatusFlag(SequencePO sequencePO);

}
