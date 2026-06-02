# 小区英语组班微信小程序

## 本地运行

```bash
cd miniapp
npm install
npm run dev:mp-weixin
```

使用微信开发者工具打开 `miniapp/dist/dev/mp-weixin`。本地后端默认地址为 `http://localhost:18055`，可通过 `VITE_API_BASE_URL` 覆盖。

建议使用 Node.js 22.x。当前 `src/manifest.json` 使用微信测试号 `touristappid`，拿到真实小程序 AppID 后再替换 `mp-weixin.appid`。

## HBuilderX

HBuilderX 直接打开 `D:\workspace\edu\miniapp`。如果提示依赖缺失，先在该目录执行 `npm install`，再运行到微信开发者工具。

## Mock 登录账号

- 老师：`13900000011`
- 学生：`13900000201`
- 家长：`13900000101`

后端默认启用 `app.wechat.miniapp.mock-enabled=true`，填写手机号即可完成本地绑定登录。切换真实微信登录时，配置 `app-id`、`app-secret` 并关闭 mock。
