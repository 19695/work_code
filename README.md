# work_code



一些工作中用到，自己封装的东西

* MBG
   * 根据实际项目需要进行了处理
      * mapper.xml 手动生成
      * entity 带有属性注释   

* ExcelUtl
    * ObjectDescriptionReflect
        支持多种类型，基于反射，读写都支持
    * ObjectToStringArrayReflect
        所有属性都是字符串，基于反射，读写都支持
    * StringArrayReadUtil
        基于字符串数组，仅适配了读取

* CSVUtl
    * 支持对象所有属性都是字符串
    * 支持字符串数组集合
    * 支持字符串
    * 读写都支持
 * 文件
   * 分片上传（基于 base64）
   * 压缩
      * Compress
      * GzUtil
      * ZipUtil
      * TarUtil
   * FileUtil
* Json 排序
* SequenceUtil
* SftpPool
* AssertUtil
    * 为空判断
* BlankCheckUtil
* ListCopyUtil
* RecoredUtil
    * 方法执行时间记录
    * 方法出入参记录（不一定会写，因为只是基于 resttemplate 发送 map，不通用）
* StringUtil
* ThreadPoolUtil
* LockUtil
