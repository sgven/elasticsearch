# restclient

## 优化点

- 1.初始化数据，如果数据量过大，耗时比较久，两点优化：

    1.1 比方说项目中，限制了dao一次性查询的数量不能超过2W，
    则需要先查询数量，然后对数据进行分批查询、导入es
    1.2 将此过程作为任务，在多线程中执行。
    
    1.3 但实际上，这样分批查询导入的话，
    直接查数据库，你并不知道哪些数据已经同步，哪些数据没有同步，
    这样的话，就需要在数据库加字段标记，或者再反查es，
    这样都是不合理的，简单的事情复杂化了。

    所以，还是要绕过dao的限制，通过底层的jdbcTemplate等查询数据。
    
    1.4 一次性查询数据量太大jdbcTemplate也会有问题，NullPointException，
    还是可以实现分批查询的，需要对数据按照主键排序，mysql用limit，oracle用rownum，oracle特殊点。
    流程如下：
    
    1.4.1 查询总数量
    1.4.2 分批查询，oracle sql如下：
    
        SELECT * FROM  (
              SELECT  ROWNUM SN, t. *   
              FROM  table t 
              ORDER  BY  gid
         )
         WHERE SN > 0  
         AND SN <= 1000; 
    
    1.4.3 原生sql查不了，jdbcTemplate获取连接池空指针，dao.getSession也是session is closed，
    可以用项目中dao封装的findPage(hql,pageIndex,pageSize)分页查询
    
    1.4.4 在java中循环初始化，分批插入，数据量不大的时候是可以的。然而，在数据量很大，单挑数据也很大的情况下，
    很容易执行过程中出现OOM，java.lang.OutOfMemoryError: Java heap space。
    
    原因可能是一个方法中循环太多次且单条数据量大，
    
    解决办法：
    1.考虑使用消息，一是使数据快速使用完，不堆积在方法中；二是使数据产生和消费解耦，分散到不同的方法中。
    2.ESDocument注解上加pageSize，针对数据量特大的接口日志，减少每批处理的数据量。

- 2.初始化数据，demo中不管数据是否删除，在项目中数据实际上是有is_delete的

    所以，考虑is_delete，在初始化时，先对未删除的insert，
    然后找到已删除的，查看在es中是否存在，对已存在的更新或删除
    
    实际上，还是不需要考虑is_delete，es中有UpdateRequest，即可以更新，也可以在es中不存在的时候插入。
    
        bulkRequest.add(new UpdateRequest()
            .index(indexName).type(typeName)
            .id((String) jsonMap.get("id")).doc(jsonMap)
            .upsert(jsonMap));//upsert:当文档不存在时，新增(up_insert)
    
    这样在初始化时，数据没有变化，version就不会改变，一旦有变化就会更新，version+1。
    
 ## 实验
  浏览器分别访问，es-head 和 h2 web
  
  h2 web：http://localhost:8081/h2