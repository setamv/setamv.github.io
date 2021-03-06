[Back](../index.md)

# Introduction
  
"High Performance MySQL" 一书的目录信息。

# Book Catalogues

- Table of Contents -26
- Foreword  -14
- Preface   -12
    - How This Book Is Organized    -12
        - A Broad Overview  -11
        - Building a Solid Foundation   -11
        - Configuring Your Application  -10
        - MySQL as an Infrastructure Component  -10
        - Miscellaneous Useful Topics   -9
    - Software Versions and Availability    -9
    - Conventions Used in This Book -8
    - Using Code Examples   -7
    - Safari® Books Online  -7
    - How to Contact Us -6
    - Acknowledgments for the Third Edition -6
    - Acknowledgments for the Second Edition    -5
        - From Baron    -5
        - From Peter    -5
        - From Vadim    -4
        - From Arjen    -4
    - Acknowledgments for the First Edition -3
        - From Jeremy   -2
        - From Derek    -2
- Chapter 1. MySQL Architecture and History 1
    - MySQL’s Logical Architecture  1
        - Connection Management and Security    2
        - Optimization and Execution    3
    - Concurrency Control   3
        - Read/Write Locks  4
        - Lock Granularity  4
            - Table locks   5
            - Row locks 5
    - Transactions  6
        - Isolation Levels  7
        - Deadlocks 9
        - Transaction Logging   10
        - Transactions in MySQL 10
            - AUTOCOMMIT    10
            - Mixing storage engines in transactions    11
            - Implicit and explicit locking 11
    - Multiversion Concurrency Control  12
    - MySQL’s Storage Engines   13
        - The InnoDB Engine 15
            - InnoDB’s history  16
            - InnoDB overview   16
        - The MyISAM Engine 17
            - Storage   18
            - MyISAM features   18
            - Compressed MyISAM tables  19
            - MyISAM performance    19
        - Other Built-in MySQL Engines  19
            - The Archive engine    19
            - The Blackhole engine  20
            - The CSV engine    20
            - The Federated engine  20
            - The Memory engine 20
            - The Merge storage engine  21
            - The NDB Cluster engine    21
        - Third-Party Storage Engines   21
            - OLTP storage engines  22
            - Column-oriented storage engines   22
            - Community storage engines 23
        - Selecting the Right Engine    24
            - Logging   25
            - Read-only or read-mostly tables   26
            - Order processing  26
            - Bulletin boards and threaded discussion forums    27
            - CD-ROM applications   27
            - Large data volumes    27
        - Table Conversions 28
            - ALTER TABLE   28
            - Dump and import   28
            - CREATE and SELECT 28
    - A MySQL Timeline  29
    - MySQL’s Development Model 33
    - Summary   34
- Chapter 2. Benchmarking MySQL 35
    - Why Benchmark?    35
    - Benchmarking Strategies   37
        - What to Measure   38
    - Benchmarking Tactics  40
        - Designing and Planning a Benchmark    41
        - How Long Should the Benchmark Last?   42
        - Capturing System Performance and Status   44
        - Getting Accurate Results  45
        - Running the Benchmark and Analyzing Results   47
        - The Importance of Plotting    49
    - Benchmarking Tools    50
        - Full-Stack Tools  51
        - Single-Component Tools    51
    - Benchmarking Examples 54
        - http_load 54
        - MySQL Benchmark Suite 55
        - sysbench  56
            - The sysbench CPU benchmark    56
            - The sysbench file I/O benchmark   57
            - The sysbench OLTP benchmark   59
            - Other sysbench features   60
        - dbt2 TPC-C on the Database Test Suite 61
        - Percona’s TPCC-MySQL Tool 64
    - Summary   66
- Chapter 3. Profiling Server Performance   69
    - Introduction to Performance Optimization  69
        - Optimization Through Profiling    72
        - Interpreting the Profile  74
    - Profiling Your Application    75
        - Instrumenting PHP Applications    77
    - Profiling MySQL Queries   80
        - Profiling a Server’s Workload 80
            - Capturing MySQL’s queries to a log    80
            - Analyzing the query log   82
        - Profiling a Single Query  84
            - Using SHOW PROFILE    85
            - Using SHOW STATUS 88
            - Using the slow query log  89
            - Using the Performance Schema  90
        - Using the Profile for Optimization    91
    - Diagnosing Intermittent Problems  92
        - Single-Query Versus Server-Wide Problems  93
            - Using SHOW GLOBAL STATUS  93
            - Using SHOW PROCESSLIST    94
            - Using query logging   95
            - Making sense of the findings  96
        - Capturing Diagnostic Data 97
            - The diagnostic trigger    97
            - What kinds of data should you collect?    98
            - Interpreting the data 99
        - A Case Study in Diagnostics   102
    - Other Profiling Tools 110
        - Using the USER_STATISTICS Tables  110
        - Using strace  111
    - Summary   112
- Chapter 4. Optimizing Schema and Data Types   115
    - Choosing Optimal Data Types   115
        - Whole Numbers 117
        - Real Numbers  118
        - String Types  119
            - VARCHAR and CHAR types    119
            - BLOB and TEXT types   121
            - Using ENUM instead of a string type   123
        - Date and Time Types   125
        - Bit-Packed Data Types 127
        - Choosing Identifiers  129
        - Special Types of Data 131
    - Schema Design Gotchas in MySQL    131
    - Normalization and Denormalization 133
        - Pros and Cons of a Normalized Schema  134
        - Pros and Cons of a Denormalized Schema    135
        - A Mixture of Normalized and Denormalized  136
    - Cache and Summary Tables  136
        - Materialized Views    138
        - Counter Tables    139
    - Speeding Up ALTER TABLE   141
        - Modifying Only the .frm File  142
        - Building MyISAM Indexes Quickly   143
    - Summary   145
- Chapter 5. Indexing for High Performance  147
    - Indexing Basics   147
        - Types of Indexes  148
            - B-Tree indexes    148
                - Types of queries that can use a B-Tree index  151
            - Hash indexes  152
                - Building your own hash indexes    154
                - Handling hash collisions  156
            - Spatial (R-Tree) indexes  157
            - Full-text indexes 157
            - Other types of index  158
    - Benefits of Indexes   158
    - Indexing Strategies for High Performance  159
        - Isolating the Column  159
        - Prefix Indexes and Index Selectivity  160
        - Multicolumn Indexes   163
        - Choosing a Good Column Order  165
        - Clustered Indexes 168
            - Comparison of InnoDB and MyISAM data layout   170
                - MyISAM’s data layout  171
                - InnoDB’s data layout  172
            - Inserting rows in primary key order with InnoDB   173
        - Covering Indexes  177
        - Using Index Scans for Sorts   182
        - Packed (Prefix-Compressed) Indexes    184
        - Redundant and Duplicate Indexes   185
        - Unused Indexes    187
        - Indexes and Locking   188
    - An Indexing Case Study    189
        - Supporting Many Kinds of Filtering    190
        - Avoiding Multiple Range Conditions    192
        - Optimizing Sorts  193
    - Index and Table Maintenance   194
        - Finding and Repairing Table Corruption    194
        - Updating Index Statistics 195
        - Reducing Index and Data Fragmentation 197
    - Summary   199
- Chapter 6. Query Performance Optimization 201
    - Why Are Queries Slow? 201
    - Slow Query Basics: Optimize Data Access   202
        - Are You Asking the Database for Data You Don’t Need?  202
        - Is MySQL Examining Too Much Data? 204
            - Response time 204
            - Rows examined and rows returned   205
            - Rows examined and access types    205
    - Ways to Restructure Queries   207
        - Complex Queries Versus Many Queries   207
        - Chopping Up a Query   208
        - Join Decomposition    209
    - Query Execution Basics    210
        - The MySQL Client/Server Protocol  210
            - Query states  213
        - The Query Cache   214
        - The Query Optimization Process    214
            - The parser and the preprocessor   214
            - The query optimizer   215
            - Table and index statistics    220
            - MySQL’s join execution strategy   220
            - The execution plan    222
            - The join optimizer    223
            - Sort optimizations    226
        - The Query Execution Engine    228
        - Returning Results to the Client   228
    - Limitations of the MySQL Query Optimizer  229
        - Correlated Subqueries 229
            - When a correlated subquery is good    230
        - UNION Limitations 233
        - Index Merge Optimizations 234
        - Equality Propagation  234
        - Parallel Execution    234
        - Hash Joins    234
        - Loose Index Scans 235
        - MIN() and MAX()   237
        - SELECT and UPDATE on the Same Table   237
    - Query Optimizer Hints 238
    - Optimizing Specific Types of Queries  241
        - Optimizing COUNT() Queries    241
            - What COUNT() does 242
            - Myths about MyISAM    242
            - Simple optimizations  242
            - Using an approximation    243
            - More complex optimizations    244
        - Optimizing JOIN Queries   244
        - Optimizing Subqueries 244
        - Optimizing GROUP BY and DISTINCT  244
            - Optimizing GROUP BY WITH ROLLUP   246
        - Optimizing LIMIT and OFFSET   246
        - Optimizing SQL_CALC_FOUND_ROWS    248
        - Optimizing UNION  248
        - Static Query Analysis 249
        - Using User-Defined Variables  249
            - Optimizing ranking queries    250
            - Avoiding retrieving the row just modified 252
            - Counting UPDATEs and INSERTs  252
            - Making evaluation order deterministic 253
            - Writing a lazy UNION  254
            - Other uses for variables  255
    - Case Studies  256
        - Building a Queue Table in MySQL   256
        - Computing the Distance Between Points 258
        - Using User-Defined Functions  262
    - Summary   263
- Chapter 7. Advanced MySQL Features    265
    - Partitioned Tables    265
        - How Partitioning Works    266
        - Types of Partitioning 267
        - How to Use Partitioning   268
        - What Can Go Wrong 270
        - Optimizing Queries    272
        - Merge Tables  273
    - Views 276
        - Updatable Views   278
        - Performance Implications of Views 279
        - Limitations of Views  280
    - Foreign Key Constraints   281
    - Storing Code Inside MySQL 282
        - Stored Procedures and Functions   284
        - Triggers  286
        - Events    288
        - Preserving Comments in Stored Code    289
    - Cursors   290
    - Prepared Statements   291
        - Prepared Statement Optimization   292
        - The SQL Interface to Prepared Statements  293
        - Limitations of Prepared Statements    294
    - User-Defined Functions    295
    - Plugins   297
    - Character Sets and Collations 298
        - How MySQL Uses Character Sets 298
            - Defaults for creating objects 298
            - Settings for client/server communication  299
            - How MySQL compares values 300
            - Special-case behaviors    300
        - Choosing a Character Set and Collation    301
        - How Character Sets and Collations Affect Queries  302
    - Full-Text Searching   305
        - Natural-Language Full-Text Searches   306
        - Boolean Full-Text Searches    308
        - Full-Text Changes in MySQL 5.1    310
        - Full-Text Tradeoffs and Workarounds   310
        - Full-Text Configuration and Optimization  312
    - Distributed (XA) Transactions 313
        - Internal XA Transactions  314
        - External XA Transactions  315
    - The MySQL Query Cache 315
        - How MySQL Checks for a Cache Hit  316
        - How the Cache Uses Memory 318
        - When the Query Cache Is Helpful   320
        - How to Configure and Maintain the Query Cache 323
            - Reducing fragmentation    324
            - Improving query cache usage   325
        - InnoDB and the Query Cache    326
        - General Query Cache Optimizations 327
        - Alternatives to the Query Cache   328
    - Summary   329
- Chapter 8. Optimizing Server Settings 331
    - How MySQL’s Configuration Works   332
        - Syntax, Scope, and Dynamism   333
        - Side Effects of Setting Variables 335
        - Getting Started   337
        - Iterative Optimization by Benchmarking    338
    - What Not to Do    340
    - Creating a MySQL Configuration File   342
        - Inspecting MySQL Server Status Variables  346
    - Configuring Memory Usage  347
        - How Much Memory Can MySQL Use?    347
        - Per-Connection Memory Needs   348
        - Reserving Memory for the Operating System 349
        - Allocating Memory for Caches  349
        - The InnoDB Buffer Pool    350
        - The MyISAM Key Caches 351
            - The MyISAM key block size 353
        - The Thread Cache  353
        - The Table Cache   354
        - The InnoDB Data Dictionary    356
    - Configuring MySQL’s I/O Behavior  356
        - InnoDB I/O Configuration  357
            - The InnoDB transaction log    357
                - Log file size and the log buffer  358
                - How InnoDB flushes the log buffer 360
            - How InnoDB opens and flushes log and data files   361
            - The InnoDB tablespace 364
                - Configuring the tablespace    364
                - Old row versions and the tablespace   366
            - The doublewrite buffer    368
            - Other I/O configuration options   368
        - MyISAM I/O Configuration  369
    - Configuring MySQL Concurrency 371
        - InnoDB Concurrency Configuration  372
        - MyISAM Concurrency Configuration  373
    - Workload-Based Configuration  375
        - Optimizing for BLOB and TEXT Workloads    375
        - Optimizing for Filesorts  377
    - Completing the Basic Configuration    378
    - Safety and Sanity Settings    380
    - Advanced InnoDB Settings  383
    - Summary   385
- Chapter 9. Operating System and Hardware Optimization 387
    - What Limits MySQL’s Performance?  387
    - How to Select CPUs for MySQL  388
        - Which Is Better: Fast CPUs or Many CPUs?  388
        - CPU Architecture  390
        - Scaling to Many CPUs and Cores    391
    - Balancing Memory and Disk Resources   393
        - Random Versus Sequential I/O  394
        - Caching, Reads, and Writes    395
        - What’s Your Working Set?  395
        - Finding an Effective Memory-to-Disk Ratio 397
        - Choosing Hard Disks   398
    - Solid-State Storage   400
        - An Overview of Flash Memory   401
        - Flash Technologies    402
        - Benchmarking Flash Storage    403
        - Solid-State Drives (SSDs) 404
            - Using RAID with SSDs  405
        - PCIe Storage Devices  406
        - Other Types of Solid-State Storage    407
        - When Should You Use Flash?    407
        - Using Flashcache  408
        - Optimizing MySQL for Solid-State Storage  410
    - Choosing Hardware for a Replica   414
    - RAID Performance Optimization 415
        - RAID Failure, Recovery, and Monitoring    417
        - Balancing Hardware RAID and Software RAID 418
        - RAID Configuration and Caching    419
            - The RAID stripe chunk size    420
            - The RAID cache    420
    - Storage Area Networks and Network-Attached Storage    422
        - SAN Benchmarks    423
        - Using a SAN over NFS or SMB   424
        - MySQL Performance on a SAN    424
        - Should You Use a SAN? 425
    - Using Multiple Disk Volumes   427
    - Network Configuration 429
    - Choosing an Operating System  431
    - Choosing a Filesystem 432
    - Choosing a Disk Queue Scheduler   434
    - Threading 435
    - Swapping  436
    - Operating System Status   438
        - How to Read vmstat Output 438
        - How to Read iostat Output 440
        - Other Helpful Tools   441
        - A CPU-Bound Machine   442
        - An I/O-Bound Machine  443
        - A Swapping Machine    444
        - An Idle Machine   444
    - Summary   445
- Chapter 10. Replication   447
    - Replication Overview  447
        - Problems Solved by Replication    448
        - How Replication Works 449
    - Setting Up Replication    451
        - Creating Replication Accounts 451
        - Configuring the Master and Replica    452
        - Starting the Replica  453
        - Initializing a Replica from Another Server    456
        - Recommended Replication Configuration 458
    - Replication Under the Hood    460
        - Statement-Based Replication   460
        - Row-Based Replication 460
        - Statement-Based or Row-Based: Which Is Better?    461
        - Replication Files 463
        - Sending Replication Events to Other Replicas  465
        - Replication Filters   466
    - Replication Topologies    468
        - Master and Multiple Replicas  468
        - Master-Master in Active-Active Mode   469
        - Master-Master in Active-Passive Mode  471
        - Master-Master with Replicas   473
        - Ring Replication  473
        - Master, Distribution Master, and Replicas 474
        - Tree or Pyramid   476
        - Custom Replication Solutions  477
            - Selective replication 477
            - Separating functions  478
            - Data archiving    478
            - Using replicas for full-text searches 479
            - Read-only replicas    479
            - Emulating multisource replication 480
            - Creating a log server 481
    - Replication and Capacity Planning 482
        - Why Replication Doesn’t Help Scale Writes 483
        - When Will Replicas Begin to Lag?  484
        - Plan to Underutilize  485
    - Replication Administration and Maintenance    485
        - Monitoring Replication    485
        - Measuring Replication Lag 486
        - Determining Whether Replicas Are Consistent with the Master   487
        - Resyncing a Replica from the Master   488
        - Changing Masters  489
            - Planned promotions    490
            - Unplanned promotions  491
            - Locating the desired log positions    492
        - Switching Roles in a Master-Master Configuration  494
    - Replication Problems and Solutions    495
        - Errors Caused by Data Corruption or Loss  495
        - Using Nontransactional Tables 498
        - Mixing Transactional and Nontransactional Tables  498
        - Nondeterministic Statements   499
        - Different Storage Engines on the Master and Replica   500
        - Data Changes on the Replica   500
        - Nonunique Server IDs  500
        - Undefined Server IDs  501
        - Dependencies on Nonreplicated Data    501
        - Missing Temporary Tables  502
        - Not Replicating All Updates   503
        - Lock Contention Caused by InnoDB Locking Selects  503
        - Writing to Both Masters in Master-Master Replication  505
        - Excessive Replication Lag 507
            - Don’t duplicate the expensive part of writes  508
            - Do writes in parallel outside of replication  509
            - Prime the cache for the replication thread    509
        - Oversized Packets from the Master 511
        - Limited Replication Bandwidth 511
        - No Disk Space 511
        - Replication Limitations   512
    - How Fast Is Replication?  512
    - Advanced Features in MySQL Replication    514
    - Other Replication Technologies    516
    - Summary   518
- Chapter 11. Scaling MySQL 521
    - What Is Scalability?  521
        - A Formal Definition   523
    - Scaling MySQL 527
        - Planning for Scalability  527
        - Buying Time Before Scaling    528
        - Scaling Up    529
        - Scaling Out   531
            - Functional partitioning   531
            - Data sharding 533
            - Choosing a partitioning key   535
            - Multiple partitioning keys    537
            - Querying across shards    537
            - Allocating data, shards, and nodes    538
            - Arranging shards on nodes 539
            - Fixed allocation  541
            - Dynamic allocation    542
            - Mixing dynamic and fixed allocation   543
            - Explicit allocation   543
            - Rebalancing shards    544
            - Generating globally unique IDs    545
            - Tools for sharding    546
        - Scaling by Consolidation  547
        - Scaling by Clustering 548
            - MySQL Cluster (NDB Cluster)   550
            - Clustrix  551
            - ScaleBase 551
            - GenieDB   551
            - Akiban    552
        - Scaling Back  552
            - Keeping active data separate  554
    - Load Balancing    555
        - Connecting Directly   556
            - Splitting reads and writes in replication 557
            - Changing the application configuration    559
            - Changing DNS names    559
            - Moving IP addresses   560
        - Introducing a Middleman   560
            - Load balancers    561
            - Load-balancing algorithms 562
            - Adding and removing servers in the pool   563
        - Load Balancing with a Master and Multiple Replicas    564
    - Summary   565
- Chapter 12. High Availability 567
    - What Is High Availability?    567
    - What Causes Downtime? 568
    - Achieving High Availability   569
        - Improving Mean Time Between Failures  570
        - Improving Mean Time to Recovery   571
    - Avoiding Single Points of Failure 572
        - Shared Storage or Replicated Disk 573
        - Synchronous MySQL Replication 576
            - MySQL Cluster 576
            - Percona XtraDB Cluster    577
        - Replication-Based Redundancy  580
    - Failover and Failback 581
        - Promoting a Replica or Switching Roles    583
        - Virtual IP Addresses or IP Takeover   583
        - Middleman Solutions   584
        - Handling Failover in the Application  585
    - Summary   586
- Chapter 13. MySQL in the Cloud    589
    - Benefits, Drawbacks, and Myths of the Cloud   590
    - The Economics of MySQL in the Cloud   592
    - MySQL Scaling and HA in the Cloud 593
    - The Four Fundamental Resources    594
    - MySQL Performance in Cloud Hosting    595
        - Benchmarks for MySQL in the Cloud 598
    - MySQL Database as a Service (DBaaS)   600
        - Amazon RDS    600
        - Other DBaaS Solutions 602
    - Summary   602
- Chapter 14. Application-Level Optimization    605
    - Common Problems   605
    - Web Server Issues 608
        - Finding the Optimal Concurrency   609
    - Caching   611
        - Caching Below the Application 611
        - Application-Level Caching 612
        - Cache Control Policies    614
        - Cache Object Hierarchies  616
        - Pregenerating Content 617
        - The Cache as an Infrastructure Component  617
        - Using HandlerSocket and memcached Access  618
    - Extending MySQL   618
    - Alternatives to MySQL 619
    - Summary   620
- Chapter 15. Backup and Recovery   621
    - Why Backups?  622
    - Defining Recovery Requirements    623
    - Designing a MySQL Backup Solution 624
        - Online or Offline Backups?    625
        - Logical or Raw Backups?   627
            - Logical backups   627
            - Raw backups   628
        - What to Back Up   629
            - Incremental and differential backups  630
        - Storage Engines and Consistency   632
            - Data consistency  632
            - File consistency  633
        - Replication   634
    - Managing and Backing Up Binary Logs   634
        - The Binary Log Format 635
        - Purging Old Binary Logs Safely    636
    - Backing Up Data   637
        - Making a Logical Backup   637
            - SQL dumps 637
            - Delimited file backups    638
        - Filesystem Snapshots  640
            - How LVM snapshots work    640
            - Prerequisites and configuration   641
            - Creating, mounting, and removing an LVM snapshot  642
            - LVM snapshots for online backups  643
            - Lock-free InnoDB backups with LVM snapshots   644
            - Planning for LVM backups  645
            - Other uses and alternatives   647
    - Recovering from a Backup  647
        - Restoring Raw Files   648
            - Starting MySQL after restoring raw files  649
        - Restoring Logical Backups 649
            - Loading SQL files 650
            - Loading delimited files   651
        - Point-in-Time Recovery    652
        - More Advanced Recovery Techniques 653
            - Delayed replication for fast recovery 654
            - Recovering with a log server  654
        - InnoDB Crash Recovery 655
            - Causes of InnoDB corruption   656
            - How to recover corrupted InnoDB data  656
    - Backup and Recovery Tools 658
        - MySQL Enterprise Backup   658
        - Percona XtraBackup    658
        - mylvmbackup   659
        - Zmanda Recovery Manager   659
        - mydumper  659
        - mysqldump 660
    - Scripting Backups 661
    - Summary   664
- Chapter 16. Tools for MySQL Users 665
    - Interface Tools   665
    - Command-Line Utilities    666
    - SQL Utilities 667
    - Monitoring Tools  667
        - Open Source Monitoring Tools  668
        - Commercial Monitoring Systems 670
        - Command-Line Monitoring with Innotop  672
    - Summary   677
- Appendix A. Forks and Variants of MySQL   679
    - Percona Server    679
    - MariaDB   681
    - Drizzle   682
    - Other MySQL Variants  683
    - Summary   683
- Appendix B. MySQL Server Status   685
    - System Variables  685
    - SHOW STATUS   686
        - Thread and Connection Statistics  688
        - Binary Logging Status 688
        - Command Counters  689
        - Temporary Files and Tables    689
        - Handler Operations    690
        - MyISAM Key Buffer 690
        - File Descriptors  690
        - Query Cache   690
        - SELECT Types  690
        - Sorts 691
        - Table Locking 692
        - InnoDB-Specific   692
        - Plugin-Specific   692
    - SHOW ENGINE INNODB STATUS 692
        - Header    693
        - SEMAPHORES    693
        - LATEST FOREIGN KEY ERROR  695
        - LATEST DETECTED DEADLOCK  697
        - TRANSACTIONS  699
        - FILE I/O  702
        - INSERT BUFFER AND ADAPTIVE HASH INDEX 703
        - LOG   703
        - BUFFER POOL AND MEMORY    704
        - ROW OPERATIONS    705
    - SHOW PROCESSLIST  706
    - SHOW ENGINE INNODB MUTEX  707
    - Replication Status    708
    - The INFORMATION_SCHEMA    709
        - InnoDB Tables 710
        - Tables in Percona Server  711
    - The Performance Schema    712
    - Summary   713
- Appendix C. Transferring Large Files  715
    - Copying Files 715
        - A Naïve Example   716
        - A One-Step Method 716
        - Avoiding Encryption Overhead  716
        - Other Options 717
    - File Copy Benchmarks  718
- Appendix D. Using EXPLAIN 719
    - Invoking EXPLAIN  719
        - Rewriting Non-SELECT Queries  721
    - The Columns in EXPLAIN    722
        - The id Column 723
        - The select_type Column    724
        - The table Column  724
            - Derived tables and unions 725
            - An example of complex SELECT types    726
        - The type Column   727
        - The possible_keys Column  729
        - The key Column    729
        - The key_len Column    729
        - The ref Column    730
        - The rows Column   731
        - The filtered Column   732
        - The Extra Column  732
    - Tree-Formatted Output 733
    - Improvements in MySQL 5.6 734
- Appendix E. Debugging Locks   735
    - Lock Waits at the Server Level    735
        - Table Locks   736
            - Finding out who holds a lock  738
        - The Global Read Lock  738
        - Name Locks    739
        - User Locks    740
    - Lock Waits in InnoDB  740
        - Using the INFORMATION_SCHEMA Tables   742
- Appendix F. Using Sphinx with MySQL   745
    - A Typical Sphinx Search   746
    - Why Use Sphinx?   749
        - Efficient and Scalable Full-Text Searching    749
        - Applying WHERE Clauses Efficiently    750
        - Finding the Top Results in Order  751
        - Optimizing GROUP BY Queries   752
        - Generating Parallel Result Sets   753
        - Scaling   754
        - Aggregating Sharded Data  755
    - Architectural Overview    756
        - Installation Overview 757
        - Typical Partition Use 758
    - Special Features  759
        - Phrase Proximity Ranking  759
        - Support for Attributes    760
        - Filtering 761
        - The SphinxSE Pluggable Storage Engine 761
        - Advanced Performance Control  763
    - Practical Implementation Examples 764
        - Full-Text Searching on Mininova.org   764
        - Full-Text Searching on BoardReader.com    765
        - Optimizing Selects on Sahibinden.com  767
        - Optimizing GROUP BY on BoardReader.com    768
        - Optimizing Sharded JOIN Queries on Grouply.com    769
    - Summary   770
- Index 771
