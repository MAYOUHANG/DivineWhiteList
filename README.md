# DivineWhiteList

DivineWhiteList 是一个面向 Mohist 1.20.1 混合端与 Spigot 体系的白名单插件，使用玩家名进行白名单校验，并在添加白名单时强制绑定 QQ 号，用于降低同一人多号申请的风险。插件不依赖玩家首次进服生成 UUID，管理员可以在玩家从未进入服务器的情况下直接放行。

适用场景  
- 你希望在玩家未进服前就完成白名单审批  
- 你需要在白名单记录里保存 QQ 号与备注信息，便于人工审核与追踪  
- 你希望限制同一个 QQ 号对应的白名单名额数量

## 1. 核心特性

1) 基于玩家名的白名单校验  
- 在玩家登录前进行校验  
- 玩家名不在白名单时直接拒绝连接  
- 默认按不区分大小写匹配，避免大小写导致误拦截

2) 白名单条目强制绑定 QQ 号  
- 管理员添加白名单必须提供 QQ 号  
- 默认一个 QQ 号只允许绑定一个玩家名  
- 可在配置中调整为允许一个 QQ 号绑定多个玩家名

3) 可选的同步到原版白名单机制  
- 插件自身独立判断放行  
- 可选在玩家首次成功进服后，将该玩家的 UUID 同步写入服务器原版 whitelist.json  
- 同步仅用于兼容部分依赖原版白名单的插件或面板，不影响插件自身校验

4) 审计与可追踪性  
- 每次 add remove setqq 操作写入控制台日志  
- 数据文件中保留创建时间、操作者、备注信息  
- 支持查询 QQ 号与玩家名的绑定关系

## 2. 运行环境

- Java：17  
- 服务器：Mohist 1.20.1 或 Spigot 1.20.x  
- 不依赖 Vault  
- online-mode true 或 false 都可用

## 3. 安装

1) 将 DivineWhiteList.jar 放入 plugins 目录  
2) 启动服务器生成配置与数据文件  
3) 执行 /dwl reload 载入配置  
4) 使用命令添加白名单

编译方式  
1) 在仓库根目录执行 mvn package  
2) 生成的 jar 位于 target/DivineWhiteList-1.0.0.jar  

目录结构  
- plugins/DivineWhiteList/config.yml  
- plugins/DivineWhiteList/data.yml  
- plugins/DivineWhiteList/logs/audit.log 可选

## 4. 登录校验机制

推荐实现事件：  
- AsyncPlayerPreLoginEvent：用 event.getName 取玩家名做校验，不通过则 disallow 并返回中文提示  
- PlayerJoinEvent：玩家成功进服后，可选执行同步到原版白名单，补齐 UUID 以兼容其他生态

## 5. 命令

主命令：/dwl

5.1 添加白名单  
/dwl add <玩家名> <QQ号> [备注...] [--force]

示例  
/dwl add Mayoh 12345678 朋友推荐  
/dwl add Alice 987654321  
/dwl add Alice 987654321 已经存在 --force

规则建议  
- 玩家名默认不区分大小写  
- QQ 号要求为纯数字，长度默认 5 到 12 位，可配置  
- 若 QQ 已绑定其他玩家名，默认拒绝添加  
- 若玩家名已存在，默认提示已存在，支持 --force 覆盖

5.2 移除白名单  
/dwl remove <玩家名>

5.3 更新 QQ  
/dwl setqq <玩家名> <新QQ号>

5.4 查看条目  
/dwl info <玩家名>

输出建议  
- 玩家名  
- QQ 号  
- 备注  
- 创建时间与操作者  
- 最近修改时间与操作者

5.5 按 QQ 查询  
/dwl qq <QQ号>

输出建议  
- 该 QQ 绑定的所有玩家名  
- 每个玩家名的备注与创建时间

5.6 列表与分页  
/dwl list [页码]  
建议每页 10 条

5.7 重载  
/dwl reload

5.8 导入导出 可选  
/dwl export vanilla  
/dwl import vanilla

导入说明  
- import 会将原版白名单中的玩家名写入 data.yml  
- QQ 会自动生成纯数字占位号，长度可在 config.yml 的 import.qq-length 设置  
- 备注使用 import.placeholder-note  
- 如 QQ 名额已满会跳过该玩家

## 6. 权限节点

- divinewhitelist.admin  
- divinewhitelist.add  
- divinewhitelist.remove  
- divinewhitelist.setqq  
- divinewhitelist.info  
- divinewhitelist.qq  
- divinewhitelist.list  
- divinewhitelist.reload  
- divinewhitelist.import  
- divinewhitelist.export  

建议只给管理组 admin 权限。

## 7. 配置文件设计

plugins/DivineWhiteList/config.yml 建议字段

enabled: true

case-insensitive-names: true

qq:
  min-length: 5
  max-length: 12
  regex: "^[0-9]+$"

limits:
  max-names-per-qq: 1

kick-message:
  - "&c你不在白名单中。"
  - "&7请按管理组要求提交申请。"
  - "&7你的用户名: &f{player}"
  - "&7服务器: &f{server}"

placeholders:
  server: "PaPa服务器"

sync-vanilla-whitelist:
  enabled: true
  mode: "ON_FIRST_JOIN"
  require-bukkit-whitelist-enabled: false

logging:
  audit-file: true
  audit-console: true

notes:
  max-length: 64

import:
  placeholder-note: "imported from vanilla whitelist"
  qq-length: 10

## 8. 数据文件格式

plugins/DivineWhiteList/data.yml 建议结构

version: 1
entries:
  mayoh:
    nameOriginal: "Mayoh"
    qq: "12345678"
    note: "朋友推荐"
    createdAt: "2026-01-21T12:00:00+08:00"
    createdBy: "Console"
    updatedAt: "2026-01-21T12:10:00+08:00"
    updatedBy: "AdminName"

说明  
- entries 的 key 建议使用标准化后的玩家名，小写  
- nameOriginal 用于保留原始大小写  
- 时间建议 ISO 8601 带时区  
- 写入建议使用临时文件再原子替换，避免断电损坏

## 9. 玩家名校验建议

- 长度 3 到 16  
- 仅允许 [A-Za-z0-9_]

offline-mode 下玩家名等同身份标识的一部分，建议入服审核流程要求玩家固定游戏名。

## 10. 中文本地化

所有玩家提示建议放在 config.yml，便于统一风格。  
支持占位符：{player} {server}

## 11. Codex 实现清单

必须实现  
- 登录前按玩家名拦截  
- /dwl add remove setqq info qq list reload  
- data.yml 持久化  
- QQ 唯一性限制与可配置名额上限  
- 中文踢出提示与服务器名占位符

建议实现  
- 审计日志  
- 可选同步到原版 whitelist.json  
- import export

当前实现补充  
- /dwl add 支持 --force 覆盖  
- /dwl list 默认每页 10 条  
- data.yml 使用临时文件保存后替换，降低损坏风险  

## 12. 许可协议建议

MIT License
