# work_code



**一些工作中用到，自己封装的东西**



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
* BlankReplaceUtil
* ListCopyUtil
* RecoredUtil
    * 方法执行时间记录
* StringUtil
    * 字符集判断
* ThreadPoolUtil
* LockUtil
* Redis
* Mybatis Interceptor
    * 结果集数量限制
    * 执行时间记录
* DateTimeUtil
    * jdk 8 时间 api 与 date 转换



git 指定日期查看自己的增删行数

```bash
git log --since="2022-4-13" --before="2022-4-15" --author="$(git config --get user.name)" --pretty=tformat: --numstat | gawk '{ add += $1 ; subs += $2 ; loc += $1 - $2 } END { printf "added lines: %s rem
oved lines : %s total lines: %s\n",add,subs,loc }'
```

查询指定用户的增删行数

```bash
git log --author="YOUR_USERNAME" --pretty=tformat: --numstat | awk '{ add += $1; subs += $2; loc += $1 - $2 } END { printf "added lines: %s, removed lines: %s, total lines: %s\n", add, subs, loc }'
```

单独统计每个人的增删行数
```bash
git log --format='%aN' | sort -u | while read name; do echo -en "$name\t"; git log --author="$name" --pretty=tformat: --numstat | awk '{ add += $1; subs += $2; loc += $1 - $2 } END { printf "added lines: %s, removed lines: %s, total lines: %s\n", add, subs, loc }' -; done
```