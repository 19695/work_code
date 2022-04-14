package org.colm.code.sequence;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Function;

public class SequenceUtil {

    private static SequencePOMapper sequencePOMapper;

    @Autowired
    public void setSequenceMapper(SequencePOMapper sequencePOMapper) {
        SequenceUtil.sequencePOMapper = sequencePOMapper;
    }

    /**
     * 简单方式获取序列号，默认步进 1
     * @param key
     * @param fmt
     * @return
     * @throws Exception
     */
    public static String getSeqNoOneStep(String key, String fmt) throws SequenceException {
        return getSeqNoSimple(key, fmt, 1);
    }

    /**
     * 简单方式获取序列号
     * @param key
     * @param fmt
     * @param step
     * @return
     * @throws Exception
     */
    public static String getSeqNoSimple(String key, String fmt, int step) throws SequenceException {
        return getSeqNo(key, prepareFunction(fmt, step));
    }

    /**
     * 提供个性化定制生成规则
     * @param key
     * @param function
     * @return
     * @throws Exception
     */
    public static String getSeqNo(String key, Function<String, String> function) throws SequenceException {
        // 序列号
        String sequence = "";
        // 是否 key 对应表中的 sequence 已存在
        SequencePO sequencePO = sequencePOMapper.queryByKey(key);
        // 已存在的情况下执行更新操作
        if (sequencePO != null) {
            switch (sequencePO.getUsedStatus()) {
                // 0 表示该序号未被使用
                case "0":
                    sequence = sequencePO.getSequence();;
                    break;
                // 1 表示该序号已经被使用，需生成新的序号
                case "1":
                    String currentSeq = sequencePO.getSequence();
                    sequence = function.apply(currentSeq);
                    sequencePO.setSequence(sequence);
                    Integer effResult = sequencePOMapper.updateByKeyAndVersion(sequencePO);
                    assertOperationResult(effResult, "获取失败，请再试一次");
                    break;
                default:
                    throwException("数据状态异常");
            }
        // 如果不在表中则插入，默认从 1 开始
        } else {
            sequence = function.apply("0");
            sequencePO = new SequencePO();
            sequencePO.setKey(key);
            sequencePO.setSequence(sequence);
            Integer effResult = sequencePOMapper.insertFirst(sequencePO);
            assertOperationResult(effResult, "获取失败，请再试一次");
        }
        return sequence;
    }

    /**
     * 简单模式下更新 usedStatus 字段为 1
     * @param key
     * @param sequence
     * @param fmt
     * @return
     */
    public static String updateStatusElseGetSimpleOneStep(String key, String sequence, String fmt) throws SequenceException {
        return updateStatusElseGetSimple(key, sequence, fmt, 1);
    }

    /**
     * 更新 usedStatus 字段为 1
     * @param key
     * @param sequence
     * @param fmt
     * @param step
     * @return
     */
    public static String updateStatusElseGetSimple(String key, String sequence, String fmt, int step) throws SequenceException {
        return updateStatusElseGet(key, sequence, prepareFunction(fmt, step));
    }

    /**
     * 更新 usedStatus 字段为 1
     * 如果更新失败会再次回去一个最新的 sequence 并将 usedStatus 标记为 1
     * 提供个性化生成规则的能力
     * @param key
     * @param sequence
     * @param function
     * @return
     */
    public static String updateStatusElseGet(String key, String sequence, Function<String, String> function) throws SequenceException {
        if (updateStatusSimple(key, sequence)) {
            return sequence;
        } else {
            return updateStatusElseGet(key, getSeqNo(key, function), function);
        }
    }

    /**
     * 将更新结果给到调用者，由调用者决定后续操作
     * @param key
     * @param sequence
     * @return
     */
    private static boolean updateStatusSimple(String key, String sequence) {
        SequencePO sequencePO = new SequencePO();
        sequencePO.setKey(key);
        sequencePO.setSequence(sequence);
        Integer updateResult = sequencePOMapper.updateUsedStatusFlag(sequencePO);
        return updateResult > 0;
    }

    private static Function<String, String> prepareFunction(String fmt, int step) {
        // String.format("%06d", Integer.valueOf(temp));  ==> 000001
        return e -> String.format(fmt, (Integer.parseInt(e) + step));
    }

    private static void assertOperationResult(int result, String msg) throws SequenceException {
        if (result <= 0) {
            throwException(msg);
        }
    }

    private static void throwException(String msg) throws SequenceException {
        throw new SequenceException(msg);
    }

}
