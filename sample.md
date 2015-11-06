# 用户会话 - 登录

用户输入密码登录

## Http访问

BASE: http://localhost.com/m

PATH: /a/b/c

METHOD: GET

HEADER: application/x-protobuf

VERSION: 1.0

## 参数列表

### 参数 - a
这是参数a

* 必须: YES
* 来源: Path / Header / Cookie / QueryString
* 类型: Integer
* 默认: 无 / 0

### 参数 - b



## 返回结构

[Person](/proto/Person "Person")


```
message PBActivityBody {
	// 主键. 和activity保持一致
    optional int32 id = 1;
	// 活动描述主体
    optional string body = 2;
	// 创建日期
    optional int32 addDate = 3;
	// 更新日期
    optional int32 updateDate = 4;
}
```
