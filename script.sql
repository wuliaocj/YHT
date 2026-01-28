create table address
(
    id             int auto_increment comment '地址ID'
        primary key,
    user_id        int                                  not null comment '用户ID',
    consignee      varchar(50)                          not null comment '收货人姓名',
    phone          varchar(20)                          not null comment '收货人电话',
    province       varchar(50)                          not null comment '省份',
    city           varchar(50)                          not null comment '城市',
    district       varchar(50)                          not null comment '区县',
    detail_address varchar(200)                         not null comment '详细地址',
    postal_code    varchar(10)                          null comment '邮政编码',
    is_default     tinyint(1) default 0                 null comment '是否默认：0-否，1-是',
    status         tinyint(1) default 1                 null comment '状态：0-删除，1-正常',
    create_time    datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time    datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '收货地址表';

create index idx_is_default
    on address (is_default);

create index idx_user_id
    on address (user_id);

create table admin
(
    id              int auto_increment comment '管理员ID'
        primary key,
    username        varchar(50)                          not null comment '用户名',
    password        varchar(255)                         not null comment '密码（加密）',
    real_name       varchar(50)                          null comment '真实姓名',
    phone           varchar(20)                          null comment '手机号',
    email           varchar(100)                         null comment '邮箱',
    avatar          varchar(500)                         null comment '头像',
    role            tinyint(1) default 1                 null comment '角色：1-普通管理员，2-店长，3-超级管理员',
    status          tinyint(1) default 1                 null comment '状态：0-禁用，1-启用',
    last_login_time datetime                             null comment '最后登录时间',
    last_login_ip   varchar(50)                          null comment '最后登录IP',
    create_time     datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint idx_username
        unique (username)
)
    comment '管理员表';

create index idx_role
    on admin (role);

create table banner
(
    id          int auto_increment comment '轮播图ID'
        primary key,
    title       varchar(100)                         null comment '标题',
    image_url   varchar(500)                         not null comment '图片地址',
    link_type   tinyint(1) default 1                 null comment '链接类型：1-商品，2-分类，3-网页',
    link_value  varchar(200)                         null comment '链接值',
    sort_order  int        default 0                 null comment '排序序号',
    status      tinyint(1) default 1                 null comment '状态：0-禁用，1-启用',
    start_time  datetime                             null comment '开始展示时间',
    end_time    datetime                             null comment '结束展示时间',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '轮播图表';

create index idx_status_sort
    on banner (status, sort_order);

create table cart
(
    id             int auto_increment comment '购物车ID'
        primary key,
    user_id        int                                      not null comment '用户ID',
    product_id     int                                      not null comment '商品ID',
    quantity       int            default 1                 not null comment '商品数量',
    selected_specs text                                     null comment '已选规格（JSON格式）',
    unit_price     decimal(10, 2)                           null comment '商品单价',
    total_price    decimal(10, 2) default 0.00              null comment '商品总价',
    is_selected    tinyint(1)     default 1                 null comment '是否选中：0-否，1-是',
    create_time    datetime       default CURRENT_TIMESTAMP null comment '加入时间',
    update_time    datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    product_name   varchar(100)                             null,
    product_image  varchar(500)                             null,
    spec_ids       text                                     null
)
    comment '购物车表';

create index idx_create_time
    on cart (create_time);

create index idx_user_id
    on cart (user_id);

create table category
(
    id          int auto_increment comment '分类ID'
        primary key,
    name        varchar(50)                          not null comment '分类名称',
    icon        varchar(200)                         null comment '分类图标',
    description varchar(200)                         null comment '分类描述',
    sort_order  int        default 0                 null comment '排序序号',
    status      tinyint(1) default 1                 null comment '状态：0-禁用，1-启用',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '商品分类表';

create table config
(
    id           int auto_increment comment '配置ID'
        primary key,
    config_key   varchar(100)                       not null comment '配置键',
    config_value text                               null comment '配置值',
    config_name  varchar(100)                       null comment '配置名称',
    config_group varchar(50)                        null comment '配置分组',
    remark       varchar(200)                       null comment '备注',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint idx_config_key
        unique (config_key)
)
    comment '系统配置表';

create table coupon
(
    id                    int auto_increment comment '优惠券ID'
        primary key,
    name                  varchar(100)                             not null comment '优惠券名称',
    type                  tinyint(1)     default 1                 not null comment '类型：1-满减券，2-折扣券',
    value                 decimal(10, 2)                           not null comment '优惠值（满减金额或折扣比例）',
    min_amount            decimal(10, 2) default 0.00              null comment '最低消费金额',
    total_count           int                                      not null comment '发行总量',
    remaining_count       int                                      not null comment '剩余数量',
    limit_per_user        int            default 1                 null comment '每人限领张数',
    validity_type         tinyint(1)     default 1                 null comment '有效期类型：1-固定日期，2-领取后生效',
    start_time            datetime                                 null comment '有效期开始时间',
    end_time              datetime                                 null comment '有效期结束时间',
    valid_days            int                                      null comment '有效天数（领取后生效）',
    applicable_products   text                                     null comment '适用商品（JSON数组，空表示全部）',
    applicable_categories text                                     null comment '适用分类（JSON数组，空表示全部）',
    status                tinyint(1)     default 1                 null comment '状态：0-禁用，1-启用',
    create_time           datetime       default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '优惠券表';

create index idx_status
    on coupon (status);

create index idx_validity
    on coupon (start_time, end_time);

create table feedback
(
    id          int auto_increment comment '反馈ID'
        primary key,
    user_id     int                                  null comment '用户ID',
    type        tinyint(1) default 1                 null comment '反馈类型：1-产品建议，2-问题反馈，3-投诉',
    content     text                                 not null comment '反馈内容',
    contact     varchar(100)                         null comment '联系方式',
    images      text                                 null comment '图片（JSON数组）',
    status      tinyint(1) default 0                 null comment '状态：0-未处理，1-已处理',
    reply       text                                 null comment '回复内容',
    reply_time  datetime                             null comment '回复时间',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '用户反馈表';

create index idx_status
    on feedback (status);

create index idx_user_id
    on feedback (user_id);

create table operation_log
(
    id            int auto_increment comment '日志ID'
        primary key,
    admin_id      int                                  null comment '管理员ID',
    module        varchar(50)                          null comment '操作模块',
    operation     varchar(100)                         null comment '操作内容',
    method        varchar(10)                          null comment '请求方法',
    params        text                                 null comment '请求参数',
    ip            varchar(50)                          null comment '操作IP',
    user_agent    varchar(500)                         null comment '用户代理',
    execute_time  int                                  null comment '执行时间（毫秒）',
    status        tinyint(1) default 1                 null comment '操作状态：0-失败，1-成功',
    error_message text                                 null comment '错误信息',
    create_time   datetime   default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '操作日志表';

create index idx_admin_id
    on operation_log (admin_id);

create index idx_create_time
    on operation_log (create_time);

create table `order`
(
    id              int auto_increment comment '订单ID'
        primary key,
    order_no        varchar(50)                              not null comment '订单号',
    user_id         int                                      not null comment '用户ID',
    total_amount    decimal(10, 2)                           not null comment '订单总金额',
    discount_amount decimal(10, 2) default 0.00              null comment '优惠金额',
    delivery_fee    decimal(10, 2) default 0.00              null comment '配送费',
    actual_amount   decimal(10, 2)                           not null comment '实付金额',
    payment_method  tinyint(1)     default 1                 null comment '支付方式：1-微信支付，2-余额支付',
    payment_status  tinyint(1)     default 0                 null comment '支付状态：0-未支付，1-已支付，2-支付失败',
    payment_time    datetime                                 null comment '支付时间',
    transaction_id  varchar(100)                             null comment '微信支付交易号',
    order_status    tinyint        default 0                 not null comment '订单状态：0-待付款，1-已付款/制作中，2-制作完成，3-待取餐，4-配送中，5-已完成，6-已取消',
    order_type      tinyint(1)     default 0                 not null comment '订单类型：0-到店自取，1-外卖配送',
    take_code       varchar(10)                              null comment '取餐码',
    estimated_time  datetime                                 null comment '预计取餐/送达时间',
    complete_time   datetime                                 null comment '完成时间',
    cancel_reason   varchar(200)                             null comment '取消原因',
    cancel_time     datetime                                 null comment '取消时间',
    user_remark     varchar(200)                             null comment '用户备注',
    admin_remark    varchar(200)                             null comment '商家备注',
    create_time     datetime       default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint idx_order_no
        unique (order_no)
)
    comment '订单表';

create index idx_create_time
    on `order` (create_time);

create index idx_order_status
    on `order` (order_status);

create index idx_payment_status
    on `order` (payment_status);

create index idx_user_id
    on `order` (user_id);

create table order_item
(
    id            int auto_increment comment '订单详情ID'
        primary key,
    order_id      int                                not null comment '订单ID',
    order_no      varchar(50)                        not null comment '订单号',
    product_id    int                                not null comment '商品ID',
    product_name  varchar(100)                       not null comment '商品名称',
    product_image varchar(500)                       null comment '商品图片',
    spec_info     text                               null comment '规格信息（JSON格式）',
    unit_price    decimal(10, 2)                     not null comment '单价',
    quantity      int      default 1                 not null comment '数量',
    total_price   decimal(10, 2)                     not null comment '总价',
    rating        tinyint(1)                         null comment '评分（1-5）',
    review        varchar(500)                       null comment '评价',
    review_time   datetime                           null comment '评价时间',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '订单详情表';

create index idx_order_id
    on order_item (order_id);

create index idx_order_no
    on order_item (order_no);

create index idx_product_id
    on order_item (product_id);

create table payment_record
(
    id               int auto_increment comment '主键ID'
        primary key,
    order_no         varchar(50)                              not null comment '关联订单号',
    payment_no       varchar(64)                              not null comment '支付系统生成的支付单号',
    user_id          int                                      not null comment '支付用户ID',
    amount           decimal(10, 2) default 0.00              not null comment '支付金额（和订单actual_amount一致）',
    payment_method   tinyint(1)                               not null comment '支付方式：1-微信支付，2-支付宝，3-现金',
    payment_status   tinyint(1)     default 0                 not null comment '支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款',
    transaction_id   varchar(100)                             null comment '微信/支付宝返回的交易单号',
    create_time      datetime       default CURRENT_TIMESTAMP not null comment '支付单创建时间',
    pay_time         datetime                                 null comment '实际支付时间',
    callback_time    datetime                                 null comment '支付平台回调时间',
    callback_content text                                     null comment '支付平台回调的原始数据（用于对账）',
    refund_amount    decimal(10, 2) default 0.00              null comment '退款金额',
    refund_time      datetime                                 null comment '退款时间',
    refund_reason    varchar(255)                             null comment '退款原因',
    constraint uk_payment_no
        unique (payment_no)
)
    comment '支付记录表';

create index idx_order_no
    on payment_record (order_no);

create index idx_payment_no
    on payment_record (payment_no);

create index idx_payment_status
    on payment_record (payment_status);

create index idx_user_id
    on payment_record (user_id);

create table product
(
    id             int auto_increment comment '商品ID'
        primary key,
    category_id    int                                  not null comment '分类ID',
    name           varchar(100)                         not null comment '商品名称',
    en_name        varchar(100)                         null comment '英文名称',
    description    text                                 null comment '商品描述',
    detail         text                                 null comment '商品详情',
    main_image     varchar(500)                         not null comment '主图',
    images         text                                 null comment '商品图集（JSON数组）',
    base_price     decimal(10, 2)                       not null comment '基础价格',
    origin_price   decimal(10, 2)                       null comment '原价',
    inventory      int        default 999               null comment '库存（-1表示不限）',
    sales_count    int        default 0                 null comment '销量',
    is_hot         tinyint(1) default 0                 null comment '是否热销：0-否，1-是',
    is_new         tinyint(1) default 1                 null comment '是否新品：0-否，1-是',
    is_recommend   tinyint(1) default 0                 null comment '是否推荐：0-否，1-是',
    custom_options text                                 null comment '定制选项配置（JSON格式）',
    status         tinyint(1) default 1                 null comment '状态：0-下架，1-上架',
    sort_order     int        default 0                 null comment '排序序号',
    create_time    datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time    datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '商品表';

create index idx_category_id
    on product (category_id);

create index idx_hot
    on product (is_hot);

create index idx_sales
    on product (sales_count);

create index idx_sort
    on product (sort_order);

create index idx_status
    on product (status);

create table product_spec_price
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    product_id  bigint                                   not null comment '关联商品表id（对应product.id）',
    spec_type   varchar(50)                              not null comment '规格类型（cup_type=杯型，topping=小料）',
    spec_name   varchar(50)                              not null comment '规格名称（大杯/珍珠/椰果等）',
    price_add   decimal(10, 2) default 0.00              not null comment '加价金额（正数加价，负数减价，0不变）',
    status      int            default 1                 not null comment '状态：1=可用，0=不可用',
    create_time datetime       default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint idx_product_spec
        unique (product_id, spec_type, spec_name)
)
    comment '商品规格加价表';

create index idx_product_id
    on product_spec_price (product_id);

create table promotion
(
    id          int auto_increment comment '活动ID'
        primary key,
    name        varchar(100)                         not null comment '活动名称',
    type        tinyint(1)                           not null comment '活动类型：1-满减，2-折扣，3-特价，4-套餐',
    description text                                 null comment '活动描述',
    rule_config text                                 not null comment '活动规则配置（JSON格式）',
    start_time  datetime                             not null comment '开始时间',
    end_time    datetime                             not null comment '结束时间',
    status      tinyint(1) default 1                 null comment '状态：0-禁用，1-启用',
    sort_order  int        default 0                 null comment '排序序号',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '促销活动表';

create index idx_status_time
    on promotion (status, start_time, end_time);

create table spec_option
(
    id          int auto_increment comment '规格选项ID'
        primary key,
    product_id  int                                      not null comment '商品ID',
    spec_type   varchar(20)                              not null comment '规格类型：sweetness-甜度，temperature-温度，topping-加料，size-杯型',
    spec_name   varchar(50)                              not null comment '规格名称',
    spec_value  varchar(50)                              not null comment '规格值',
    extra_price decimal(10, 2) default 0.00              null comment '额外价格',
    is_default  tinyint(1)     default 0                 null comment '是否默认：0-否，1-是',
    sort_order  int            default 0                 null comment '排序序号',
    status      tinyint(1)     default 1                 null comment '状态：0-禁用，1-启用',
    create_time datetime       default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '商品规格选项表';

create index idx_product_id
    on spec_option (product_id);

create index idx_spec_type
    on spec_option (spec_type);

create table t_user
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    openid      varchar(100)                           not null comment '小程序openid',
    nickname    varchar(50)  default ''                null comment '昵称',
    avatar      varchar(255) default ''                null comment '头像',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_openid
        unique (openid) comment 'openid唯一索引'
)
    comment '用户表';

create table user
(
    id                int auto_increment comment '用户ID'
        primary key,
    openid            varchar(100)                             not null comment '微信openid，唯一标识',
    unionid           varchar(100)                             null comment '微信unionid',
    nickname          varchar(100)                             null comment '微信昵称',
    avatar_url        varchar(500)                             null comment '微信头像',
    phone             varchar(20)                              null comment '手机号码',
    gender            tinyint(1)     default 0                 null comment '性别：0-未知，1-男，2-女',
    province          varchar(50)                              null comment '省份',
    city              varchar(50)                              null comment '城市',
    integral          int            default 0                 null comment '积分',
    vip_level         tinyint        default 0                 null comment '会员等级：0-普通，1-白银，2-黄金，3-钻石',
    total_consumption decimal(10, 2) default 0.00              null comment '累计消费金额',
    last_login_time   datetime                                 null comment '最后登录时间',
    status            tinyint(1)     default 1                 null comment '状态：0-禁用，1-正常',
    create_time       datetime       default CURRENT_TIMESTAMP null comment '创建时间',
    update_time       datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint idx_openid
        unique (openid)
)
    comment '用户表';

create index idx_create_time
    on user (create_time);

create index idx_phone
    on user (phone);

create table user_coupon
(
    id            int auto_increment comment '用户优惠券ID'
        primary key,
    user_id       int                                  not null comment '用户ID',
    coupon_id     int                                  not null comment '优惠券ID',
    coupon_code   varchar(50)                          null comment '优惠券码',
    status        tinyint(1) default 0                 null comment '状态：0-未使用，1-已使用，2-已过期',
    used_time     datetime                             null comment '使用时间',
    used_order_id int                                  null comment '使用的订单ID',
    receive_time  datetime   default CURRENT_TIMESTAMP null comment '领取时间',
    expire_time   datetime                             not null comment '过期时间'
)
    comment '用户优惠券表';

create index idx_coupon_id
    on user_coupon (coupon_id);

create index idx_expire
    on user_coupon (expire_time);

create index idx_status
    on user_coupon (status);

create index idx_user_id
    on user_coupon (user_id);


