# 在VMWare中扩展CentOS的磁盘空间
有的时候会发现虚拟机中安装系统的时候为虚拟操作系统分配的磁盘空间不够用，此时，需要扩展磁盘空间。

## 思路
创建一个新的逻辑分区，将新的逻辑分区格式化ext3（或其他类型）的文件系统，mount到磁盘空间不够的文件系统，就跟原来的分区/文件系统一样的使用

## 准备
1. 注意使用VMware自带的Expand功能不能有Snapshot存在，所以在expand之前先把所有的Snapshot删掉了
2. 为了以防万一，把VMware虚拟机copy了一份备用。

## 操作步骤
1. 查看挂载点
    ```
    [root@vnode1 ~]# df -h
    文件系统 容量 已用 可用 已用% 挂载点
    /dev/mapper/centos-root 36G 5.4G 31G 15% /
    devtmpfs 3.9G 0 3.9G 0% /dev
    tmpfs 3.9G 0 3.9G 0% /dev/shm
    tmpfs 3.9G 8.5M 3.9G 1% /run
    tmpfs 3.9G 0 3.9G 0% /sys/fs/cgroup
    /dev/sda1 497M 125M 373M 26% /boot
    tmpfs 781M 0 781M 0% /run/user/0
    3、扩展VMWare硬盘空间
    3.1 关闭Vmware 的 Linux系统
    ```

2. 在VMWare菜单中扩展磁盘空间
    选中需要扩展的虚拟机系统，在右侧选择“编辑虚拟机设置”，然后以此选中：硬盘 -> 扩展
    填写需要扩展的磁盘大小，假设新增加了 40G 
    VM -> Settings… -> Hardware -> Hard Disk -> Utilities -> Expand

3. 对新增加的硬盘进行分区
    执行步骤：
    执行“fdisk /dev/sda”，进入到fdisk
    输入p指令，查看已分区数量（有两个 /dev/sda1 /dev/sda2）
    输入 n {new partition}指令，新增加一个分区
    输入p {primary partition}，分区类型选择为主分区
    输入分区号 3 {partition number} ，分区号选3（上面显示我已经有2个分区了）
    回车　　　　　　默认（起始扇区）
    回车　　　　　　默认（结束扇区）
    输入t {change partition id}指令，修改分区类型
    按提示输入刚才的分区号3
    输入分区类型 8e {Linux LVM partition}
    输入w指令，将以上改动写入分区表
    最后完成，退出fdisk命令
    [root@vnode1 ~]# fdisk /dev/sda
    欢迎使用 fdisk (util-linux 2.23.2)。
    
    更改将停留在内存中，直到您决定将更改写入磁盘。
    使用写入命令前请三思。
    
    命令(输入 m 获取帮助)：p
    ....
    设备 Boot Start End Blocks Id System
    /dev/sda1 * 2048 1026047 512000 83 Linux
    /dev/sda2 1026048 83886079 41430016 8e Linux LVM
    
    命令(输入 m 获取帮助)：n
    Partition type:
    p primary (2 primary, 0 extended, 2 free)
    e extended
    Select (default p): p
    分区号 (3,4，默认 3)：3
    起始 扇区 (83886080-167772159，默认为 83886080)：
    将使用默认值 83886080
    Last 扇区, +扇区 or +size{K,M,G} (83886080-167772159，默认为 167772159)：
    将使用默认值 167772159
    分区 3 已设置为 Linux 类型，大小设为 40 GiB
    
    命令(输入 m 获取帮助)：t
    分区号 (1-3，默认 3)：3
    Hex 代码(输入 L 列出所有代码)：8e
    已将分区“Linux”的类型更改为“Linux LVM”
    
    命令(输入 m 获取帮助)：w
    The partition table has been altered!
    
    Calling ioctl() to re-read partition table.
    
    WARNING: Re-reading the partition table failed with error 16: 设备或资源忙.
    The kernel still uses the old table. The new table will be used at
    the next reboot or after you run partprobe(8) or kpartx(8)
    正在同步磁盘。
    重启系统: 
    [root@vnode1 ~]# shutdown -r now （很重要）

4. 对新增加的硬盘格式化
    [root@vnode1 ~]# mkfs.ext3 /dev/sda3
    mke2fs 1.42.9 (28-Dec-2013)
    文件系统标签=
    OS type: Linux
    块大小=4096 (log=2)
    分块大小=4096 (log=2)
    Stride=0 blocks, Stripe width=0 blocks
    2621440 inodes, 10485760 blocks
    524288 blocks (5.00%) reserved for the super user
    第一个数据块=0
    Maximum filesystem blocks=4294967296
    320 block groups
    32768 blocks per group, 32768 fragments per group
    8192 inodes per group
    Superblock backups stored on blocks:
    32768, 98304, 163840, 229376, 294912, 819200, 884736, 1605632, 2654208,
    4096000, 7962624
    
    Allocating group tables: 完成
    正在写入inode表: 完成
    Creating journal (32768 blocks): 完成
    Writing superblocks and filesystem accounting information: 完成

5. 添加新LVM到已有的LVM组，实现扩容
    [root@vnode1 ~]# lvm
    lvm> pvcreate /dev/sda3
    WARNING: ext3 signature detected on /dev/sda3 at offset 1080. Wipe it? [y/n]: y
    Wiping ext3 signature on /dev/sda3.
    Physical volume "/dev/sda3" successfully created
    lvm> vgextend centos /dev/sda3
    Volume group "centos" successfully extended
    lvm> lvextend -L +39.9G /dev/mapper/centos-root
    Rounding size to boundary between physical extents: 39.90 GiB
    Size of logical volume centos/root changed from 35.47 GiB (9080 extents) to 75.37 GiB (19295 extents).
    Logical volume root successfully resized.
    lvm> pvdisplay
    --- Physical volume ---
    PV Name /dev/sda2
    VG Name centos
    PV Size 39.51 GiB / not usable 3.00 MiB
    Allocatable yes (but full)
    PE Size 4.00 MiB
    Total PE 10114
    Free PE 0
    Allocated PE 10114
    PV UUID vtNvX3-b1yw-ePoh-YGHQ-tDhL-x0ru-mfts3k
    
    --- Physical volume ---
    PV Name /dev/sda3
    VG Name centos
    PV Size 40.00 GiB / not usable 4.00 MiB
    Allocatable yes
    PE Size 4.00 MiB
    Total PE 10239
    Free PE 34
    Allocated PE 10205
    PV UUID jWqvcF-R53u-ZeAy-zO2L-PbtI-51VP-yHNZ1g
    
    lvm> quit
    Exiting.
    [root@vnode1 ~]#
    备注：

    lvm　　　　　　　　　　　　 进入lvm管理
    lvm> pvcreate /dev/sda3 这是初始化刚才的分区，必须的
    lvm>vgextend centos /dev/sda3 将初始化过的分区加入到虚拟卷组vg_dc01
    lvm>lvextend -L +39.9G /dev/mapper/centos-root　　扩展已有卷的容量（注意容量大小）
    lvm>pvdisplay　　　　　　　　　　　　　　 查看卷容量，这时你会看到一个很大的卷了
    lvm>quit　　　　　　　　　　　　　　 　　　退出
    以上只是卷扩容了，下面是文件系统的真正扩容，输入以下命令：

    [root@vnode1 ~]# resize2fs /dev/mapper/centos-root
    resize2fs 1.42.9 (28-Dec-2013)
    resize2fs: Bad magic number in super-block 当尝试打开 /dev/mapper/centos-root 时
    找不到有效的文件系统超级块.
    报错：当尝试打开 /dev/mapper/centos-root 时 找不到有效的文件系统超级块

    因为我的centos7的某些分区用的是xfs的文件系统（使用df -T查看即可知道）

    [root@vnode1 ~]# df -T
    文件系统                           类型          1K-块        已用        可用          已用%  挂载点
    /dev/mapper/centos-root   xfs           37173520 5574340  31599180  15%      /
    devtmpfs 　　                   devtmpfs  3987400   0              3987400    0%       /dev
    tmpfs                                 tmpfs        3997856   0              3997856    0%      /dev/shm
    tmpfs                                 tmpfs        3997856   8628        3989228     1%     /run
    tmpfs                                 tmpfs        3997856   0               3997856     0%    /sys/fs/cgroup
    /dev/sda1                          xfs            508588      127152    381436       26%  /boot
    tmpfs                                tmpfs         799572      0              799572       0%   run/user/0
    [root@vnode1 ~]#
    将resize2fs替换为xfs_growfs，重新执行一遍即可，如下：

    [root@vnode1 ~]# xfs_growfs /dev/mapper/centos-root
    meta-data=/dev/mapper/centos-root isize=256 agcount=4, agsize=2324480 blks
    = sectsz=512 attr=2, projid32bit=1
    = crc=0 finobt=0
    data = bsize=4096 blocks=9297920, imaxpct=25
    = sunit=0 swidth=0 blks
    naming =version 2 bsize=4096 ascii-ci=0 ftype=0
    log =internal bsize=4096 blocks=4540, version=2
    = sectsz=512 sunit=0 blks, lazy-count=1
    realtime =none extsz=4096 blocks=0, rtextents=0
    data blocks changed from 9297920 to 19758080
    最后再运行下：df -h 
    即可看到扩容后的磁盘空间

    [root@vnode1 ~]# df -h
    文件系统 容量 已用 可用 已用% 挂载点
    /dev/mapper/centos-root 76G 5.4G 71G 8% /
    devtmpfs 3.9G 0 3.9G 0% /dev
    tmpfs 3.9G 0 3.9G 0% /dev/shm
    tmpfs 3.9G 8.5M 3.9G 1% /run
    tmpfs 3.9G 0 3.9G 0% /sys/fs/cgroup
    /dev/sda1 497M 125M 373M 26% /boot
    tmpfs 781M 0 781M 0% /run/user/0
    ps:修复 unknown 区  vgreduce --removemissing centos

    --- Physical volume ---
    PV Name               [unknown]
    VG Name               centos
    PV Size               10.00 GiB / not usable 4.00 MiB
    Allocatable           yes 
    PE Size               4.00 MiB
    Total PE              2559
    Free PE               2559
    Allocated PE          0
    PV UUID               VCwaaP-hhX8-YcSg-Cqji-ctob-AAod-wRnOLO