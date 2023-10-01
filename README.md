## Simple TODO API

>北邮人技术考核 Task 3

尝试使用 Socket 实现的简易 HTTP 服务器。

思路大概是对的，实现了 GET 和 DELETE，但是 POST 方法没能从 Request 里筛出来 Body（JSON），估计是 Kotlin 处理字符串有点生疏了。

先这么提交了（饶命）

### 使用方法

```
git clone <repo.git>
```
因为没用 Gradle，所以只能在 IntelliJ 里执行 Main.kt。

### 题外话

最开始尝试使用 Python 来实现，为此曾兴致勃勃想实现一个类似于 Flask 的框架。

相关项目在此：[Ririya 莉莉娅](https://github.com/Kyocius/Ririya)