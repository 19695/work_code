package org.colm.code.sequence;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支持在事务代码中使用
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TransactionSequenceUtil {

    public String getSequenceOneStep(String key, String fmt) throws SequenceException {
        return SequenceUtil.getSeqNoOneStep(key, fmt);
    }

    public String updateStatusElseGetOneStep(String key, String sequence, String fmt) throws SequenceException {
        return SequenceUtil.updateStatusElseGetSimpleOneStep(key, sequence, fmt);
    }

}
