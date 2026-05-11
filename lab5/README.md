# Lab 5 JSON TCP Chat

Учебная реализация чата для лабораторной работы по сетевому программированию: TCP/IP, Swing-клиент, сервер, JSON-сообщения с длиной в первых 4 байтах.

## Запуск

Сервер (`ru.nsu.ccfit.vmoskalyuk.Chat.server.ChatServer`):

```bash
./gradlew runServer
```

Клиент (`ru.nsu.ccfit.vmoskalyuk.Chat.client.SwingChatClient`):

```bash
./gradlew runClient
```

Настройки сервера лежат в `src/main/resources/server.properties`:

```properties
port=5555
logging.enabled=true
history.size=20
client.timeout.ms=120000
```

Также сервер можно запустить с внешним файлом настроек:

```bash
./gradlew runServer --args=/path/to/server.properties
```

## Формат сообщений

Перед каждым JSON-сообщением передаются 4 байта `Java int` с длиной JSON в байтах, затем сам JSON в UTF-8.

Команды клиента:

```json
{"command":{"name":"login","user":"USER_NAME","type":"Java Swing JSON Client"}}
{"command":{"name":"list","session":"UNIQUE_SESSION_ID"}}
{"command":{"name":"message","message":"MESSAGE","session":"UNIQUE_SESSION_ID"}}
{"command":{"name":"logout","session":"UNIQUE_SESSION_ID"}}
```

Ответы и события сервера:

```json
{"success":{"session":"UNIQUE_SESSION_ID"}}
{"success":{"listusers":[{"name":"USER_1","type":"CLIENT_TYPE"}]}}
{"success":{}}
{"error":{"message":"REASON"}}
{"event":{"name":"message","message":"MESSAGE","user":"CHAT_NAME_FROM"}}
{"event":{"name":"userlogin","user":"USER_NAME"}}
{"event":{"name":"userlogout","user":"USER_NAME"}}
```
