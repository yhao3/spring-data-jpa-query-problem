# Spring Data JPA $N + 1$ Problem

### Tags
<p align="left">
    <img alt="Spring Data JPA" src="https://img.shields.io/badge/Spring%20Data%20JPA-blue">
    <img alt="N + 1 Problem" src="https://img.shields.io/badge/N+1%20Problem-blue">
</p>

<h1 align="center">Section 02</h1>

## $N + 1$ Problem 與 `LAZY`、`EAGER` loading 之間的關係？

您可能會好奇 Demo 中為何會發生 $N + 1$ Problem。

會不會是因為我們將 `@OneToMany` 該 annotation 的 `fetch` 屬性被宣告為 `FetchType.EAGER` 了呢？

> `EAGER` loading 簡單來說就是「ORM 會提前幫我們撈出所有指定的關聯資料」。相反的，`LAZY` loading 則是代表「ORM 會延遲撈出所有指定的關聯資料，直到真的需要用到時才會去撈資料」！所以 `LAZY` loading 的出發點是好的！它可以減少多餘的資料傳輸！
> 

那還不簡單，我們就把 `fetch` 屬性改為 `FetchType.LAZY` 不就大功告成了？

我們重新啟動 application，並重複之前的測試，可以發現印出以下 Log: 

```sql
Hibernate: select producttyp0_.product_type_id as product_1_1_, producttyp0_.product_type_name as product_2_1_ from product_type producttyp0_
2022-10-10 03:29:55.116  INFO 34970 --- [nio-8080-exec-1] i.StatisticalLoggingSessionEventListener : Session Metrics {
    98875 nanoseconds spent acquiring 1 JDBC connections;
    0 nanoseconds spent releasing 0 JDBC connections;
    395834 nanoseconds spent preparing 1 JDBC statements;
    144375 nanoseconds spent executing 1 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    0 nanoseconds spent executing 0 flushes (flushing a total of 0 entities and 0 collections);
    8541 nanoseconds spent executing 1 partial-flushes (flushing a total of 0 entities and 0 collections)
}
```

Perfect! 我們成功解決 $N + 1$ Problem 了！喜歡本文記得幫我按個 `⭐ Star`...

很遺憾，事情並沒有想像中那麼簡單！

讓我們再進行第二個測試，訪問 `[http://localhost:8080/test2](http://localhost:8080/test2)`，這次我們還另外呼叫了 `getProducts()` 印出每個商品分類底下的第一個商品。觀察 Log 可以發現 $N + 1$ Problem 又出現了... 也就是說 `FetchType.LAZY` 其實只是「延遲了 $N + 1$ Problem 的發生」罷了！

## $N + 1$ Problem 合理嗎？

說到底，導致 $N + 1$ Problem 發生的原因，其實就只是因為 JPA 在關聯表之間檢索時生成 SQL 的機制是這樣設計的而已。但重點是 $N + 1$ Problem 合理嗎？

其實對於 `LAZY` loading 的場景，會發生 $N + 1$ Problem 也是非常合理的！因為 `LAZY` loading 的精神就是「需要時才撈資料」！所以根本沒必要一開始就用 $1$ 條 JOIN Query 把所有關聯資料全部都撈出來。

至於針對 `EAGER` loading 的場景，還發生 $N + 1$ Problem 就是不被允許的了！因為在「需要撈關聯資料」的場景下，明明可以用 $1$ 條 JOIN Query 就達成，那為何還要將其拆成 $N + 1$ 條 Query 呢？

所以在解決問題之前，我們必須重新釐清、限縮問題：我們需要解決的不是「所有 $N + 1$ Problem」，而是只有「 `EAGER` loading 場景下發生的 $N + 1$ Problem」！

那這樣是不是代表 JPA 在 `FetchType.EAGER` 的實作上設計不良呢？一開始實作就將所有關聯 TABLE JOIN 起來不就大功告成了嗎？

是的！所以我將 `FetchType.EAGER` 對 `EAGER` mode 的實作稱為「愚蠢的 `EAGER` mode」！我相信 Java 團隊自己也意識到了這個問題，因此在 JPA 2.1 版本，就提供了「聰明的 `EAGER` mode」的作法！因此下一小節，我們將會介紹 JPA 2.1 版本提供的「聰明的 `EAGER` mode」的作法！

### 切換到 `section-03` branch 接續下一小節的內容
```shell
git checkout -b section-03
```