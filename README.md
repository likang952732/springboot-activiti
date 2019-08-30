#### spring-boot-with-activiti
整合工作流
activiti version 5.22.0
spring boot version 1.5.9.RELEASE
mysql5.0+
mybatis

#### 文件中心

1. 通过git下载activiti源码：https://github.com/Activiti/Activiti
2. 切换分支到5.22
    git checkout activiti-5.22.0
3. 参考引用http://jmysql.com/activiti/126.html
4. 作者博客地址： https://blog.csdn.net/las723

#### 相关接口

1.首页 http://localhost

2.我发起的记录 http://localhost/history/process/mys?userId=c00776c0-186c-11e9-b046-e58aa127f5e6

3.我待办的记录 http://localhost/runtime/tasks/ing?userId=bee0dbc0-1956-11e9-bde4-5516b71b463e

4.我操作的记录 http://localhost/history/tasks/myc?userId=bee0dbc0-1956-11e9-bde4-5516b71b463e

#### 使用说明

1.将源码down到本地，用idea或eclipse打开，会初始化maven依赖，直到成功

2.新建数据库，更改src/main/resources/application.yml对应配置信息

3.初始化src/main/resources/db-init.sql表结构，自己新加几条数据做测试用

4.启动ActivitiApplication.java

5.访问首页即可，

#### 注意事项

1.因业务需要使用自己环境的用户权限；

2.配置流程图时，流程名称须填、流程key必须唯一
    流程开始节点id必须配置为S00000，当然你也可以根据代码修改
    
3.有疑问的同学可在博客下方留言

#### 代码结构

1.src/main/java

    ----com.ww                                       *****此包下面都是业务代码，需要我们去优化*****
        ----activti
            ----config                  //工作流的配置，包含8个大接口的引入，需要使用其中某个service直接@Autowired
            ----controller              //控制器：
                ----ActivitiController  //简单的流程启动接口                          
                ----HistoryController   //操作历史接口，包含我的审批、我发起的记录  对应的是act_hi_开头的表数据
                ----ModelerController   //模型相关接口，包含新增模型、发布模型为流程   对应的是act_re_model表数据
                ----PageController      //打开编辑器页面的接口
                ----ProcessController   //流程相关接口，包含查看流程图、带业务参数启动流程、获取流程节点  对应的是act_re_procdef、act_re_deployment表数据
                ----RuntimeController   //流程运行操作接口，包含执行任务(审批)、我的待办  对应的是act_ru_开头的表数据
            ----listener
                ----MyProcessExecutionListener  //流程开始或者结束的监听器，在配置流程开始节点和结束节点的配置里添加
                ----MyTaskCompletedListener     //每个节点执行后的监听器，在流程节点(不包含开始结束节点)的配置里添加
            ----vo                                                                   
        ----common                      //常量包
        ----dao                         //mybatis生成mapper.java
        ----model                       //mybatis生成实体
        ----service                     //业务接口
        ----util                        //工具类
        ActivitiApplication.java        //启动类
    ----org.activiti                                   *****此包下面是从activiti源码里拷贝的，包含编辑器相关服务，不需要我们去动他们*****

2.src/main/resources

    ----mapper                          //mybatis生成mapper.xml
    ----mybatis                         //mybatis生成代码
    ----static                          //从activiti源码里拷贝的，编辑器的资源
    ----templates                       //从activiti源码里拷贝的，编辑器的页面
    application.yml                     //配置
    db-init.sql                         //我自己业务上的权限表，这里根据你自己的业务，可以使用你自己的业务表
    stencilset.json                     //从activiti源码里拷贝的，编辑器的页面文字结构汉化                

3.接口文档

文档在根目录下面，里面很详细的介绍了各个接口对应的信息，以及如何配置流程的详细介绍，参考“ACTIVITI工作流接口文档.docx”


#### 使用参考
--------------------------------------------------------- 
作者：艾斯-李 
来源：CSDN 
原文：https://blog.csdn.net/las723/article/details/88417152 
版权声明：本文为博主原创文章，转载请附上博文链接！