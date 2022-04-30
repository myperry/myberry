# MyBerry Project

![license](https://img.shields.io/badge/license-MIT-blue)
![license](https://img.shields.io/badge/release-v2.2.0-green)

MyBerry is a distributed ID construction engine that can construct meaningful and unique IDs for
enterprises in the production process.

## Official Site

[myberry.org](https://myberry.org)

## Features

* Platformization
* Easy to operate
* Free and flexible

## Building

myberry version > 2.1.0 only supports Java 17,
myberry version <= 2.1.0 only supports Java 8.

```bash
# mvn -Prelease-all -DskipTests clean install -U
```

### Maven dependency

```xml

<dependency>
  <groupId>org.myberry</groupId>
  <artifactId>myberry-client</artifactId>
  <version>2.2.0</version>
</dependency>
```

## License

MyBerry is under the MIT License. See the [LICENSE](https://myberry.org/license) file for details.