# Kayji-MyCooldown

Plugin Minecraft (Spigot) **giới hạn spam**: hạn chế boost bay (Firework/Elytra/Riptide), spam cần gạt lever, spam Redstone, và giới hạn số ArmorStand/Minecart trong một khu vực.

> **Tác giả:** Kayji_Tizi · **Phiên bản:** 1.0 · **API:** 1.20 · **Java:** 17

## Tính năng

- **Boost** (Firework/Elytra & Riptide Trident): cooldown `boost.cooldown-seconds` giữa 2 lần boost.
- **Lever**: cooldown chống spam cần gạt (mặc định 2 giây).
- **Redstone Dust**: cooldown chống đặt/điều khiển redstone quá nhanh (mặc định 3 giây).
- **ArmorStand**: giới hạn số lượng theo bán kính **chunk** (`radius-chunks` + `limit`).
- **Minecart**: giới hạn số lượng theo bán kính chunk.
- Từng mục **bật/tắt riêng** bằng `enabled` và có **tin nhắn lỗi tùy chỉnh** (hỗ trợ `{time}`, `{count}`, `{limit}`).
- Lệnh reload không cần restart, kèm tab-complete.

## Bảng lệnh

Gõ trong game với dấu `/`, có tab-complete:

| Lệnh | Quyền | Mô tả |
| --- | --- | --- |
| `/cooldownplugin reload` | `cooldownplugin.reload` | Tải lại `config.yml` |
| `/cdp reload` | `cooldownplugin.reload` | Alias rút gọn của `/cooldownplugin reload` |

> Quyền `cooldownplugin.reload` — mặc định chỉ `op`.

## Cấu hình

```yaml
boost:
  enabled: true
  cooldown-seconds: 5
  cooldown-message: "&cBạn phải chờ &6{time}&c giây nữa mới được boost bay lại."

lever:
  enabled: true
  cooldown-seconds: 2
  cooldown-message: "&cBạn phải chờ &6{time}&c giây nữa mới được kích hoạt lever."

redstone:
  enabled: true
  cooldown-seconds: 3
  cooldown-message: "&cBạn đang đặt Redstone quá nhanh, hãy chờ &6{time}&c giây nữa."

armorstand:
  enabled: true
  radius-chunks: 16
  limit: 30
  limit-message: "&cChỉ được tạo tối đa {limit} ArmorStand trong vòng này (đã có {count})."

minecart:
  enabled: true
  radius-chunks: 32
  limit: 30                     # giới hạn Minecart trong bán kính chunk
```

## Cài đặt

```bash
mvn clean package
```

Copy `target/Kayji-Cooldown-2.0-SNAPSHOT.jar` vào thư mục `plugins/` rồi restart server.

## Cấu trúc dự án

```
├── pom.xml                                Maven (Java 17, spigot-api 1.20)
└── src/main
    ├── java/com/aefamily/cooldownplugin
    │   ├── Main.java                      Lắng nghe sự kiện + lệnh reload
    │   └── CooldownManager.java           Quản lý thời gian chờ theo từng mục
    └── resources
        ├── plugin.yml                     Lệnh + quyền
        └── config.yml                     5 mục: boost, lever, redstone, armorstand, minecart
```

## Giấy phép

[GNU General Public License v3.0](LICENSE)
