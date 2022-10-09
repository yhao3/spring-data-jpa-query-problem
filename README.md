# Spring Data JPA $N + 1$ Problem

### Tags
<p align="left">
    <img alt="Spring Data JPA" src="https://img.shields.io/badge/Spring%20Data%20JPA-blue">
    <img alt="N + 1 Problem" src="https://img.shields.io/badge/N+1%20Problem-blue">
</p>

<h1 align="center">Section 03</h1>

## Solution

那麽到底該怎麼解決 $N + 1$ Problem 呢？ JPA 2.1 版本就新增了新的 Annotation `@EntityGraph` 來解決這個問題！我們可以使用 `@EntityGraph` 及其 `attributePaths` 屬性創建 ad-hoc entity graph，如此一來 JPA 最終就只會產生 $1$ 條 JOIN Query！

> Note: 也可以使用 `@NamedEntityGraph` 搭配 `@EntityGraph` 達成相同的效果，但只使用 `@EntityGraph` 更加簡潔且直覺！所以本文只介紹 `@EntityGraph` 的用法。有興趣可參考這篇 Baeldung 的文章： [Spring Data JPA and Named Entity Graphs | Baeldung](https://www.baeldung.com/spring-data-jpa-named-entity-graphs)


### `@EntityGraph`

> `org.springframework.data.jpa.repository.EntityGraph`
> 
- 用法: 由 Spring Data JPA 提供，只能標記在「Repository 的方法」上
- 作用: 我將它稱為聰明的 `EAGER` mode。它使被標記的方法使用 `EAGER` mode 幫我們在執行第一次 select 時就一併撈出所有指定的關聯資料（創建 ad-hoc entity graph）。也就是在第一條 select statement 加上 `JOIN` 一併撈出所有指定的關聯資料！
- 屬性:
    1. `value`
        - 型態: String
        - 作用: 要使用的 EntityGraph 之名稱
            - 預設值為 `""`，為 `""` 時會自動命名為 `domainName.methodName`
                
                ```java
                getDomainClass().getSimpleName() + "." + method.getName()
                ```
                
    2. `attributePaths`
        - 型態: String Array
        - 作用: 指定 EntityGraph 要使用的屬性路徑。指定了該屬性後，執行該方法產生之 SQL 就會 JOIN 該屬性對應之 TABLE
    3. `type`
        - 型態: EntityGraphType
        - 作用: 指定 EntityGraph 的模式。推薦使用預設值 `FETCH` 即可，至少可以減少 $N + 1$ Problem 發生的次數。
            - `EntityGraphType.FETCH` (預設值):
                - 使被 `attributePaths` 指定到的屬性被視為 `FetchType.EAGER`
                - 未被指定的屬性則一律視為 `FetchType.LAZY` 處理
            - `EntityGraphType.LOAD`:
                - 使被 `attributePaths` 指定到的屬性被視為 `FetchType.EAGER`
                - 未被指定的屬性則根據其 指定的 FetchType 或 預設的 FetchType 處理。
                    
                    > Note:
                    > - `@OneToMany` 預設的 FetchType 為 `LAZY`
                    > - `@ManyToOne` 預設的 FetchType 為 `EAGER`
                    
        - 可以切換到 branch `demo-entity-graph-type` 查看兩者的範例

### Demo

將 `ProductTypeRepository` 的 `findAll()` 方法標記 `@EntityGraph` annotation，並將 `attributePaths` 屬性值指定為 `products`

```java
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
    @EntityGraph(
        attributePaths = { "products" }
    )
    List<ProductType> findAll();
}
```

**Test**

1. 啟動 application
    
    ```bash
    ./mvnw spring-boot:run
    ```
    
2. 訪問 URL: [`http://localhost:8080/insert`](http://localhost:8080/insert) 
    - 新增 5 筆商品類別，並在每筆商品類別中各新增一筆商品
3. 訪問 URL: [`http://localhost:8080/test2`](http://localhost:8080/test2)
    - 印出之 Log 如下:

    ```sql
    Hibernate: select producttyp0_.product_type_id as product_1_1_0_, products1_.product_id as product_1_0_1_, producttyp0_.product_type_name as product_2_1_0_, products1_.product_name as product_2_0_1_, products1_.product_type_id as product_3_0_1_, products1_.product_type_id as product_3_0_0__, products1_.product_id as product_1_0_0__ from product_type producttyp0_ left outer join product products1_ on producttyp0_.product_type_id=products1_.product_type_id
    2022-10-10 04:15:16.453  INFO 38823 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : productType0
    2022-10-10 04:15:16.453  INFO 38823 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : product0
    2022-10-10 04:15:16.453  INFO 38823 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : productType1
    2022-10-10 04:15:16.453  INFO 38823 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : product1
    2022-10-10 04:15:16.453  INFO 38823 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : productType2
    2022-10-10 04:15:16.453  INFO 38823 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : product2
    2022-10-10 04:15:16.453  INFO 38823 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : productType3
    2022-10-10 04:15:16.453  INFO 38823 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : product3
    2022-10-10 04:15:16.453  INFO 38823 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : productType4
    2022-10-10 04:15:16.453  INFO 38823 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : product4
    2022-10-10 04:15:16.455  INFO 38823 --- [nio-8080-exec-2] i.StatisticalLoggingSessionEventListener : Session Metrics {
        51208 nanoseconds spent acquiring 1 JDBC connections;
        0 nanoseconds spent releasing 0 JDBC connections;
        548000 nanoseconds spent preparing 1 JDBC statements;
        266959 nanoseconds spent executing 1 JDBC statements;
        0 nanoseconds spent executing 0 JDBC batches;
        0 nanoseconds spent performing 0 L2C puts;
        0 nanoseconds spent performing 0 L2C hits;
        0 nanoseconds spent performing 0 L2C misses;
        0 nanoseconds spent executing 0 flushes (flushing a total of 0 entities and 0 collections);
        11500 nanoseconds spent executing 1 partial-flushes (flushing a total of 0 entities and 0 collections)
    }
    ```
    

## Summary

- 在 `LAZY` loading 情境下發生 $N + 1$ Problem 是合理且可被接受的！
- 在 `EAGER` loading 情境下發生 $N + 1$ Problem 是不被接受的！因此我們要解決的是「 `EAGER` loading 場景下發生的 $N + 1$ Problem」！
- 術語：
    - `LAZY` loading 情境下: JPA 要設定為 `LAZY` mode
        - JPA 針對 `LAZY` mode 的實作: 只有 `FetchType.LAZY` （可能會發生 $N + 1$ Problem，但不需要解決）
    - `EAGER` loading 情境下: JPA 要設定為 `EAGER` mode
        - JPA 針對 `EAGER` mode 的實作:
            1. JPA 2.1 之前: 使用 `FetchType.EAGER` （會發生 $N + 1$ Problem）
            2. JPA 2.1 之後: 使用 `@EntityGraph` （不會發生 $N + 1$ Problem）
- `FetchType.EAGER` 對 `EAGER` mode 的實作方式，是造成在 `EAGER` loading 情境下發生 $N + 1$ Problem 的主因。因此我將 `FetchType.EAGER` 稱作**「愚蠢的 `EAGER` mode」**！
- 將 `fetch` 屬性改為 `FetchType.LAZY` 無法解決「 `EAGER` loading 場景下的 $N + 1$ Problem」，它只能延遲 $N + 1$ Problem 的發生（且在 `EAGER` 場景下設定為 `FetchType.LAZY` 本身就不合理）
- 不管是否有 `EAGER` 的需求，`@OneToMany` 的 `FetchType` 一律設定為 `FetchType.LAZY`，因為至少“只要不要用到關聯屬性”，就永遠不會發生 $N + 1$ Problem（僥倖心理）
    - 就算有 `EAGER` 的需求，`@OneToMany` 的 `FetchType` 也一律設定為 `FetchType.LAZY`，`EAGER` 的實作使用 JPA 2.1 之後提供的 `@EntityGraph`，它會覆蓋指定屬性的 `FetchType.LAZY`！
- 解決 $N + 1$ Problem 的方法是使用 `@EntityGraph` 此 annotation。我將這個解決方法稱為**「聰明的 `EAGER` mode」**！
    - `@EntityGraph` 只需要指定 `attributePaths` 屬性即可，`value` 與 `type` 預設即可
- 因此當呼叫 Repository 的某方法返回 Entity 物件後，還必須用該 Entity 物件 get 它的某個關聯屬性時，就必須將 Repository 的該方法標記 `@EntityGraph` annotation，並將 `attributePaths` 屬性值指定為該關聯屬性
    - 使用代名詞非常繞口，以 Demo 的例子來說，就是呼叫 `ProductTypeRepository` 的 `findAll()` 方法取得 `ProductType` 物件後，如果一定還會透過 `ProductType` 物件呼叫 `getProducts()` 方法獲取關聯 TABLE 的資料時，我們就應該將 `findAll()` 方法標記 `@EntityGraph` annotation，並將 `attributePaths` 屬性值指定為 `products`
        
        ```java
        public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
            @EntityGraph(
                attributePaths = { "products" }
            )
            List<ProductType> findAll();
        }
        ```