# SAP系统中的名词术语

# Catalogue <a id="≡"></a>
- [Client（集团）](#client)
- [Company（公司）](#company)
- [Company Code（公司代码）](#companycode)


# Terminology List

## Client（集团）<a id="client">[≡](#≡)</a>
Client（集团）是SAP系统的一个组织单元/结构。该组织单元是最高层次的，也是所有实施SAP应用模块必须创建并维护的组织结构。
每个Client在与其他业务、组织、技术等方面与其他Client都是独立的，有各自的主数据和数据库表。    
在Client层次设置的系统属性和参数，适用于于该Client下的所有公司/公司代码等其他组织架构/单元；所有的输入和产出、处理加工都是分Client进行的，即不跨Client处理。这也就是说，一个单位多个公司，设置成多个Client后，则：        
1. 一个用户处理不同Client的业务，需要在每个Client下创建用户账号，并分配权限；   
2. 不同Client中公司的同一个客户、供应商、物料等等都必须在各自的Client下创建维护，不能在一个Client下一次性创建，多个Client共享。 
           
R/3初始安装时，有两个默认的Client即000和001；这两个Client包含必要的默认配置内容，因此可以作为创建新Client的模板。    
实务处理中，一个Client可以与一个公司对应，也可以与多个公司对应。例如，一个集团，跨了很多行业，每个行业都有不少的公司。此时，如何有效设置和管理Client？
为了保证Client层次上的数据充分共享和一致，整个设置为一个Client，而其下的行业等可以通过其他维度，比如控制范围等来划分。一个集团两个公司，设置为两个Client的，那么同一用户同时涉及到两个单位的业务，就必须为该用户在两个Client创建两个账户，分配两次权限。这显然会增加维护工作量，并且难以保证Client层次上一些数据的一致性。

## Company（公司）<a id="company">[≡](#≡)</a>
在SAP中，Company（公司）与Company Code（公司代码/公司码）是两个不同，但是又相联系的概念。    
Company是根据相关法律规定出具财务报告的最小层次组织单元。    
一个Client下，一个Company可以与一个或多个Company Code对应起来，一个Company Code只能对应一个Company。在启用SAP模块中，Company Code是必须的，而Company是选择性的。Company Code层次财务报表时自动生成的，而Company的财务报表是基于Company Code的合并报表。这也是SAP多维度和层次出具财务报表的一个体现。对于不需要出具这种合并报表的单位，Company是不需要进行维护的。    
某单位实施SAP时，有这样的场景：全国一个总公司，下属42个分公司（非法人）。SAP处理中，将这43个总分机构都设置成Company Code，同时，设置了43个一样的Company。因为公司也需要出具各个分公司的报表和总公司的财务报表，所以上述设置没有真正启用SAP的合并报表功能，如果43个Company Code对应到1个Company，则可在系统内出具总公司层次的合并报表。   
上面这段有失偏颇，理由有2：    
1. 设置43个company可以出合并报表，对于数据的汇总，设置成1个company还是43个company没有区别    
2. 设成43个company可以很好的解决内部抵消，如果设置成1个，无法实现灵活的内部抵消   

所以，我支持设置成43个。   
这里系统在组织架构本身，体现了如何处理法人和非法人的关系——会计上，法律实体一定是会计主体，而会计主体未必一定是法律主体。就像上面的，分公司是会计主体，却并非法律实体。因此，会计主体对应到Company Code层次，而法律实体对应到Company层次是可以满足两个层次分别出具报表要求的。    
为了保证数据的一致性和可合并性，同一个Company下的Company Code必须使用相同的会计科目表（Chart of Account）和会计年度（Fiscal Year）。但是每个公司代码都可以使用不同的本币（Local Currency）。    
基本的配置分为两部分：    （1）定义Company：组织架构-定义-财务会计-定义公司；    （2）将Company分配给已经定义的Company Code：分配-财务会计-将公司（Company）分派给公司代码（CompanyCode）。

## Company Code（公司代码）<a id="companycode">