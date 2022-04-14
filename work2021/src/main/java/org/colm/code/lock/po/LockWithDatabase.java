package org.colm.code.lock.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@TableName("tbl_lock")
public class LockWithDatabase {

    private String lockKey;

    private Long dupCount;

    private String lockStatus;

    private Date invalidTime;

    private Date createTime;

    private Date updateTime;

}
