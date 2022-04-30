# MyBerry 项目

![license](https://img.shields.io/badge/license-MIT-blue)
![license](https://img.shields.io/badge/release-v2.2.0-green)

MyBerry 是一个分布式的ID构造引擎，能够为企业在生产过程中构建有含义的唯一ID。

## 官方网站

[myberry.org](https://myberry.org)

## 特性

* 平台化
* 操作简单易上手
* 自由灵活

## 构建

myberry 版本 > 2.1.0 仅支持 Java 17，
myberry 版本 <= 2.1.0 仅支持 Java 8。

```bash
# mvn -Prelease-all -DskipTests clean install -U
```

### Maven 依赖

```xml
<dependency>
  <groupId>org.myberry</groupId>
  <artifactId>myberry-client</artifactId>
  <version>2.2.0</version>
</dependency>
```

## 许可证

Myberry 授予 MIT 许可证。 请参阅 [LICENSE](https://myberry.org/license) 文件了解详情。