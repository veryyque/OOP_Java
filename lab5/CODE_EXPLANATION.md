# Подробный разбор проекта Lab 5

В проекте есть две версии чата:

- `ru.nsu.ccfit.vmoskalyuk.Chat` - JSON-версия. Сообщение идет по TCP как 4 байта длины + JSON-строка.
- `ru.nsu.ccfit.vmoskalyuk.Serialization` - версия на Java-сериализации. По TCP передаются Java-объекты через `ObjectInputStream` и `ObjectOutputStream`.

Обе версии устроены одинаково по архитектуре:

1. Сервер открывает `ServerSocket` на порту из конфига.
2. Клиент открывает `Socket` к серверу.
3. Для каждого клиента сервер создает отдельный `ClientHandler` в отдельном потоке.
4. Клиент после подключения отправляет login.
5. Сервер создает уникальную session, сохраняет пользователя и возвращает success.
6. Сообщения от одного клиента сервер рассылает всем подключенным клиентам.
7. Клиент читает входящие сообщения в отдельном потоке, чтобы Swing-интерфейс не зависал.

## build.gradle

`plugins { id 'java'; id 'application' }` подключает Java-проект и возможность запускать main-класс через Gradle.

`group = 'ru.nsu.ccfit.vmoskalyuk'` задает группу проекта. Это влияет на имя артефакта, но не запускает код.

`version = '1.0-SNAPSHOT'` задает версию сборки.

`repositories { mavenCentral() }` говорит Gradle, откуда брать зависимости.

`dependencies` содержит JUnit-зависимости для тестов. Сейчас тестов нет, поэтому при сборке Gradle пишет `NO-SOURCE`.

`application { mainClass = 'ru.nsu.ccfit.vmoskalyuk.Chat.client.SwingChatClient' }` задает основной main-класс по умолчанию.

`runServer` - отдельная Gradle-задача для запуска JSON-сервера `ChatServer`.

`runClient` - отдельная Gradle-задача для запуска JSON Swing-клиента.

`test { useJUnitPlatform() }` настраивает запуск JUnit 5.

## settings.gradle

`rootProject.name = 'lab5'` задает имя Gradle-проекта.

## src/main/resources/server.properties

`port=5555` - порт, на котором сервер слушает клиентов.

`logging.enabled=true` - включает вывод логов сервера в консоль.

`history.size=20` - сколько последних сообщений отправить новому клиенту после подключения.

`client.timeout.ms=120000` - таймаут чтения из сокета клиента. Если клиент долго ничего не присылает, сервер считает его проблемным соединением.

# JSON-версия: пакет ru.nsu.ccfit.vmoskalyuk.Chat

## ChatServer.java

Это главный сервер JSON-версии.

Поля класса:

- `ServerConfig config` - настройки сервера: порт, логирование, история, таймаут.
- `Map<String, ChatUser> usersBySession` - хранит пользователей по session id.
- `Map<String, ChatUser> usersByName` - хранит пользователей по нику, чтобы запретить одинаковые ники.
- `ArrayDeque<Map<String, Object>> history` - очередь последних сообщений в виде JSON-подобных Map.

`main(String[] args)`:

1. Вызывает `ServerConfig.load(args)`.
2. Создает `new ChatServer(config)`.
3. Запускает `start()`.
4. Если порт занят или другая IO-ошибка, печатает `Server error`.

`start()`:

1. Пишет лог `Server started on port ...`.
2. Создает `ServerSocket`.
3. В бесконечном цикле ждет `serverSocket.accept()`.
4. Для каждого клиента ставит `socket.setSoTimeout(...)`.
5. Создает новый поток `new Thread(new ClientHandler(...)).start()`.

`login(name, type, handler)`:

1. Проверяет, что имя не пустое.
2. Проверяет, что ник не занят.
3. Создает `session` через `UUID.randomUUID().toString()`.
4. Создает `ChatUser`.
5. Кладет пользователя в `usersBySession` и `usersByName`.
6. Запоминает пользователя внутри `ClientHandler`.
7. Логирует вход.
8. Рассылает всем другим клиентам событие `userlogin`.
9. Возвращает копию истории сообщений.

`logout(handler)`:

1. Берет пользователя из обработчика.
2. Если пользователя нет, просто выходит.
3. Удаляет пользователя из обеих Map.
4. Логирует выход.
5. Рассылает остальным событие `userlogout`.
6. Обнуляет пользователя в обработчике.

`sendChatMessage(session, text)`:

1. Проверяет session через `requireUser`.
2. Проверяет, что сообщение не пустое.
3. Создает event `message`.
4. Сохраняет event в историю через `remember`.
5. Логирует сообщение.
6. Рассылает event всем клиентам через `broadcast`.

`listUsers(session)`:

1. Проверяет session.
2. Проходит по всем пользователям.
3. Для каждого создает JSON-объект пользователя через `Messages.user`.
4. Возвращает `success` с полем `listusers`.

`requireUser(session)` проверяет, существует ли пользователь с такой session. Если нет - кидает `IOException("Unknown session")`.

`log(message)` печатает лог только если `logging.enabled=true`.

`remember(event)` добавляет сообщение в историю и удаляет самые старые, если размер истории больше `history.size`.

`broadcast(message)` отправляет сообщение всем пользователям.

`broadcastExcept(message, session)` отправляет всем, кроме пользователя с указанной session. Это используется для `userlogin` и `userlogout`, чтобы событие не приходило самому вошедшему или вышедшему клиенту.

## ClientHandler.java

Это серверный обработчик одного клиента.

Поля:

- `ChatServer server` - ссылка на общий сервер.
- `Socket socket` - TCP-соединение с конкретным клиентом.
- `JsonConnection connection` - обертка над сокетом для чтения и записи JSON.
- `ChatUser user` - пользователь, который сидит на этом соединении.

`run()`:

1. Создает `JsonConnection`.
2. Пока сокет не закрыт, читает сообщение `readMessage()`.
3. Передает сообщение в `process(...)`.
4. Если случился `SocketTimeoutException`, сервер логирует таймаут.
5. Если случился `EOFException`, клиент закрыл соединение.
6. Если другая ошибка, сервер логирует ее и пытается отправить клиенту `error`.
7. В `finally` всегда вызывает `server.logout(this)`, чтобы удалить клиента из списка.

`send(message)` синхронизирован, чтобы два потока не записали в один сокет одновременно. Он отправляет JSON клиенту через `connection.sendMessage`.

`process(message)`:

1. Достает объект `command`.
2. Берет `command.name`.
3. По `switch` выбирает команду.

Команды:

- `login` - вызывает `handleLogin`.
- `list` - отправляет список пользователей.
- `message` - передает текст серверу и отправляет `success`.
- `logout` - проверяет session, отправляет `success`, закрывает сокет.
- неизвестная команда - отправляет `error`.

`handleLogin(command)`:

1. Берет `user` и `type` из JSON.
2. Вызывает `server.login`.
3. Отправляет клиенту `success` с session.
4. Отправляет клиенту старые сообщения из истории.

`visibleName()` нужен для логов: если пользователь уже залогинен, пишет ник; иначе адрес сокета.

## ChatUser.java

`record ChatUser(String session, String name, String type, ClientHandler handler)` - компактный класс-хранилище.

Он автоматически создает:

- конструктор;
- методы `session()`, `name()`, `type()`, `handler()`;
- `equals`, `hashCode`, `toString`.

Здесь хранится session, ник, тип клиента и обработчик, через который можно отправлять сообщения конкретному пользователю.

## JsonConnection.java

Это низкоуровневый класс протокола JSON.

Поля:

- `MAX_MESSAGE_SIZE` - максимальная длина JSON-сообщения.
- `Socket socket` - TCP-соединение.
- `DataInputStream input` - поток для чтения примитивов и байтов.
- `DataOutputStream output` - поток для записи примитивов и байтов.

Конструктор получает `Socket` и открывает входной и выходной потоки.

`readMessage()`:

1. Читает первые 4 байта через `input.readInt()`.
2. Это длина JSON-сообщения в байтах.
3. Проверяет, что длина положительная и не слишком большая.
4. Читает ровно столько байтов через `readNBytes`.
5. Если байтов пришло меньше, значит соединение оборвалось.
6. Превращает байты в UTF-8 строку.
7. Парсит строку через `JsonUtil.parseObject`.

`sendMessage(message)`:

1. Превращает Map в JSON-строку через `JsonUtil.stringify`.
2. Превращает строку в UTF-8 байты.
3. Записывает длину через `output.writeInt(bytes.length)`.
4. Записывает сами байты.
5. Делает `flush()`, чтобы данные сразу ушли по сети.

`close()` закрывает сокет.

## JsonUtil.java

Это самодельный маленький JSON-сериализатор и парсер, чтобы не подключать внешние библиотеки.

`stringify(Object value)` превращает Java-объект в JSON-строку. Поддерживает:

- `null`;
- `String`;
- `Number`;
- `Boolean`;
- `Map`;
- `Iterable`, например `List`.

`parseObject(String json)` парсит JSON и требует, чтобы корнем был объект `{...}`.

`object(Object value)` безопасно приводит значение к `Map<String, Object>`. Если там не Map, возвращает пустую Map.

`string(Object value)` безопасно превращает значение в строку. Если значение `null`, возвращает пустую строку.

`map(Object... pairs)` создает `LinkedHashMap` из пар ключ-значение. Например `map("name", "login", "user", "Ann")`.

`mapWithName(String name, Object... pairs)` создает Map с первым полем `"name": name`, а потом добавляет остальные пары. Используется для команд и событий.

`list(Object... values)` создает список.

`writeValue(...)` - рекурсивный метод записи JSON. Если значение Map, он пишет `{...}`; если список - `[...]`; если строка - вызывает `writeString`.

`writeString(...)` экранирует кавычки, обратный слеш, переводы строк и управляющие символы.

Внутренний класс `Parser` читает JSON:

- `parse()` запускает чтение и проверяет, что после JSON нет лишних символов.
- `readValue()` определяет тип следующего значения.
- `readObject()` читает `{ key: value }`.
- `readArray()` читает `[value, value]`.
- `readString()` читает строку и обрабатывает escape-последовательности.
- `readNumber()` читает число.
- `expect(...)`, `peek(...)`, `skipSpaces()` - технические методы парсера.

## Messages.java

Это фабрика сообщений, чтобы не собирать Map вручную в каждом месте.

`command(name, pairs)` создает:

```json
{"command":{"name":"login", ...}}
```

`success(pairs)` создает:

```json
{"success":{...}}
```

`error(message)` создает:

```json
{"error":{"message":"..."}}
```

`event(name, pairs)` создает:

```json
{"event":{"name":"message", ...}}
```

`user(name, type)` создает объект пользователя для списка участников.

## ServerConfig.java

Этот класс читает настройки сервера.

Поля:

- `port`;
- `loggingEnabled`;
- `historySize`;
- `clientTimeoutMs`.

`load(args)`:

1. Создает `Properties`.
2. Загружает `server.properties` из ресурсов.
3. Если при запуске передали путь к файлу, загружает еще и его.
4. Читает значения с дефолтами.
5. Возвращает `ServerConfig`.

`loadFromClasspath(...)` читает файл из `src/main/resources`.

`loadFromFile(...)` читает внешний файл, если сервер запустили с аргументом.

## Chat/SwingChatClient.java

Это Swing-интерфейс JSON-клиента.

Поля UI:

- `hostField` - поле адреса сервера.
- `portField` - поле порта.
- `nameField` - ник.
- `connectButton` - подключиться.
- `disconnectButton` - отключиться.
- `messagesArea` - область сообщений.
- `usersModel` и `usersList` - список пользователей справа.
- `messageField` - поле ввода сообщения.
- `sendButton` - отправка.
- `emojiButton` - выбор emoji.

`main` запускает окно через `SwingUtilities.invokeLater`, чтобы Swing создавался в правильном UI-потоке.

`applyGlobalLookAndFeel()` настраивает цвета Swing-компонентов.

`createInterface()` собирает окно:

1. Верхняя панель - host, port, nick, кнопки connect/disconnect.
2. Центр - сообщения и список пользователей через `JSplitPane`.
3. Низ - поле сообщения, emoji и send.

`bindActions()` связывает кнопки с методами:

- Connect -> `connect()`;
- Disconnect -> `disconnect()`;
- Send -> `sendMessage()`;
- Enter в поле сообщения -> `sendMessage()`;
- закрытие окна -> `disconnect()` и `dispose()`.

`connect()`:

1. Читает host, port, nick.
2. Создает `ChatClientConnection`.
3. Делает `login`.
4. Пишет системное сообщение.
5. Переключает UI в состояние connected.
6. Запрашивает список пользователей.

`disconnect()`:

1. Если соединения нет, сразу выходит.
2. Отправляет logout.
3. Пишет `Disconnected`.
4. Отключает кнопки отправки.
5. Очищает список пользователей.

`sendMessage()` берет текст из поля ввода, отправляет его через connection и очищает поле.

`requestUsers()` отправляет команду списка пользователей.

`handleServerMessage(...)` получает входящий JSON от сетевого потока и обновляет Swing через `SwingUtilities.invokeLater`.

Если пришел `error`, пишет системную ошибку.

Если пришел `success.listusers`, обновляет список пользователей.

Если пришел `event.message`, добавляет обычное сообщение.

Если пришел `event.userlogin`, пишет, что пользователь вошел, и обновляет список.

Если пришел `event.userlogout`, пишет, что пользователь вышел, и обновляет список.

`showUsers(...)` очищает правый список и добавляет пользователей.

`appendMessage(...)` добавляет строку обычного сообщения.

`appendSystem(...)` добавляет системную строку.

`setConnected(...)` включает/выключает поля и кнопки.

`closeConnection()` закрывает TCP-соединение.

Нижние helper-методы `barbieButton`, `styledTextField`, `barbieLabel`, `pinkScrollPane`, `showEmojiPicker` отвечают только за внешний вид и emoji.

## Chat/ChatClientConnection.java

Это сетевая часть JSON-клиента.

Поля:

- `JsonConnection connection` - низкоуровневое JSON-соединение.
- `Consumer<Map<String, Object>> listener` - callback, куда передаются входящие сообщения.
- `volatile boolean running` - флаг работы фонового потока.
- `String session` - session id после login.

Конструктор открывает `new Socket(host, port)` и оборачивает его в `JsonConnection`.

`login(name)`:

1. Отправляет команду login.
2. Ждет первый ответ сервера синхронно.
3. Проверяет error.
4. Достает session из success.
5. Запускает фоновый reader-поток.

`sendText(text)` требует session и отправляет команду message.

`requestUsers()` требует session и отправляет команду list.

`logout()` отправляет команду logout и закрывает соединение.

`startReader()` создает daemon-поток, который постоянно читает сообщения от сервера и передает их в Swing через listener.

`checkError(...)` смотрит, есть ли в сообщении поле error.

`requireSession()` запрещает команды до login.

# Serialization-версия: пакет ru.nsu.ccfit.vmoskalyuk.Serialization

## Serialization/Connection.java

Это аналог `JsonConnection`, но для Java-сериализации.

Поля:

- `Socket socket`;
- `ObjectInputStream in`;
- `ObjectOutputStream out`.

В конструкторе важно сначала создать `ObjectOutputStream` и сделать `flush()`, а потом `ObjectInputStream`. Это помогает избежать взаимной блокировки при создании потоков на двух сторонах.

`send(Object obj)` вызывает `out.writeObject(obj)` - Java сама сериализует объект в байты.

`read()` вызывает `in.readObject()` - Java восстанавливает объект из байтов.

`close()` закрывает socket.

## Serialization/ChatServer.java

Сервер сериализационной версии.

Он почти такой же, как JSON-сервер, но история хранится как `LinkedList<ChatEvent>`, а сообщения - это объекты классов `LoginCommand`, `ChatEvent`, `SuccessResponse` и т.д.

`start()` открывает `ServerSocket`, принимает клиентов и запускает `ClientHandler`.

`login(...)` проверяет ник, создает session, сохраняет `ChatUser`, рассылает `new ChatEvent("userlogin", user.name(), null)` и возвращает историю.

`logout(...)` удаляет пользователя и рассылает `userlogout`.

`sendMessage(...)` создает `ChatEvent("message", user.name(), text)`, сохраняет в историю и рассылает всем.

`listUsers(...)` возвращает `UserListResponse`.

`broadcast(...)` принимает `ChatProtocol`, то есть любой объект протокола, и отправляет всем.

## Serialization/ClientHandler.java

Это обработчик одного клиента на сервере сериализации.

`run()`:

1. Создает `Connection`.
2. В цикле читает объект через `conn.read()`.
3. Передает объект в `process`.
4. В `finally` вызывает `server.logout(this)`.

`process(Object obj)` использует `instanceof`:

- если `LoginCommand`, логинит пользователя и отправляет `SuccessResponse`;
- если `MessageCommand`, отправляет сообщение всем;
- если `ListUsersCommand`, отправляет список пользователей;
- если `LogoutCommand`, отправляет success и закрывает socket.

Это как раз то, что обычно требуют для версии с сериализацией: получать объект, проверять его реальный тип через `instanceof`, потом обрабатывать.

`send(Object obj)` отправляет объект клиенту через `connection.send(obj)`.

`visibleName()` нужен для логов.

## Serialization/ChatClientConnection.java

Это сетевая часть клиента сериализации.

`login(name)`:

1. Отправляет `new LoginCommand(name, "Java Serialization Client")`.
2. Синхронно ждет ответ.
3. Если `SuccessResponse`, сохраняет session.
4. Если `ErrorResponse`, бросает IOException.
5. Запускает reader-поток.

`sendMessage(text)` отправляет `new MessageCommand(text)`.

`requestUsers()` отправляет `new ListUsersCommand()`.

`logout()` отправляет `new LogoutCommand()` и закрывает соединение.

`startReader()` постоянно читает объекты от сервера и передает их в Swing.

Исправленный момент: если `EOFException` произошел после нормального `Disconnect`, ошибка `Connection closed` больше не отправляется в UI. Раньше из-за этого появлялось две строки: `Disconnected` и `Error: Connection closed`.

`isConnected()` возвращает, залогинен ли клиент и работает ли поток.

## Serialization/SwingChatClient.java

Это Swing-интерфейс клиента сериализации.

Он похож на JSON Swing-клиент, но работает с объектами, а не с Map.

Поля с цветами `BARBIE_*` задают палитру интерфейса.

Поля `FONT_*` задают шрифты.

Поля UI создают верхнюю панель, область сообщений, список пользователей, поле ввода и кнопки.

`main` запускает окно.

`applyGlobalLookAndFeel()` задает глобальные цвета Swing.

`createInterface()` собирает окно.

`bindActions()` связывает кнопки с методами.

`connect()` создает `ChatClientConnection`, делает login, пишет системное сообщение и запрашивает список пользователей.

`disconnect()` теперь сначала проверяет `if (connection == null) return;`. Это защищает от повторной записи `Disconnected`.

`sendMessage()` отправляет текст через `connection.sendMessage`.

`requestUsers()` запрашивает список.

`processMessage(Object obj)` разбирает входящие объекты:

- `ErrorResponse` - системная ошибка;
- `ChatEvent` - message/userlogin/userlogout;
- `UserListResponse` - обновление списка пользователей.

`showUsers(...)` обновляет список справа.

`appendMessage(...)` добавляет сообщение в чат.

`appendSystem(...)` добавляет системное сообщение.

`setConnected(...)` включает/выключает поля и кнопки.

Методы `barbieButton`, `styledTextField`, `barbieLabel`, `pinkScrollPane`, `showEmojiPicker` отвечают за оформление и emoji.

## Serialization/ChatUser.java

Это record с данными пользователя:

- `session`;
- `name`;
- `type`;
- `handler`.

Через `handler` сервер может отправить сообщение конкретному пользователю.

# Serialization/message

Все классы в этой папке являются объектами протокола.

## ChatProtocol.java

`ChatProtocol extends Serializable` - общий маркерный интерфейс. Любой класс, который реализует `ChatProtocol`, можно отправлять через `ObjectOutputStream`.

## LoginCommand.java

Команда входа.

Поля:

- `name` - ник пользователя;
- `type` - тип клиента.

Сервер получает этот объект и регистрирует пользователя.

## MessageCommand.java

Команда отправки сообщения.

Поле:

- `text` - текст сообщения.

Сервер получает этот объект и рассылает `ChatEvent`.

## ListUsersCommand.java

Команда запроса списка пользователей.

Полей нет, потому что сам тип объекта уже означает команду.

## LogoutCommand.java

Команда выхода из чата.

Полей нет, потому что сам объект означает logout.

## SuccessResponse.java

Успешный ответ сервера.

Поле:

- `session` - session id при login. Для других команд может быть `null`.

## ErrorResponse.java

Ответ сервера или клиента об ошибке.

Поле:

- `message` - текст ошибки.

## ChatEvent.java

Событие от сервера клиентам.

Поля:

- `eventType` - тип события: `message`, `userlogin`, `userlogout`;
- `user` - ник пользователя;
- `text` - текст сообщения, если событие `message`;
- `timestamp` - время создания события.

## UserInfo.java

Информация об одном пользователе:

- `name`;
- `type`.

Используется внутри `UserListResponse`.

## UserListResponse.java

Ответ сервера на запрос списка пользователей.

Поле:

- `List<UserInfo> users` - список всех подключенных участников.

# Главный путь сообщения

## JSON-версия

1. Пользователь нажимает Send.
2. `SwingChatClient.sendMessage()` вызывает `ChatClientConnection.sendText(text)`.
3. `ChatClientConnection` создает JSON-команду `message`.
4. `JsonConnection.sendMessage()` пишет 4 байта длины и JSON.
5. На сервере `ClientHandler.run()` читает JSON.
6. `ClientHandler.process()` видит команду `message`.
7. `ChatServer.sendChatMessage()` создает event и вызывает `broadcast`.
8. Каждый `ClientHandler.send()` отправляет event своему клиенту.
9. На клиенте reader-поток получает event.
10. `SwingChatClient.handleServerMessage()` добавляет строку в `messagesArea`.

## Serialization-версия

1. Пользователь нажимает Send.
2. `SwingChatClient.sendMessage()` вызывает `ChatClientConnection.sendMessage(text)`.
3. Клиент создает `new MessageCommand(text)`.
4. `Connection.send()` пишет объект через `ObjectOutputStream`.
5. Сервер читает объект через `ObjectInputStream`.
6. `ClientHandler.process()` проверяет `obj instanceof MessageCommand`.
7. `ChatServer.sendMessage()` создает `ChatEvent`.
8. Сервер рассылает `ChatEvent` всем клиентам.
9. Клиенты читают объект и обновляют Swing.

# Почему раньше было два сообщения при Disconnect в Serialization

Было так:

1. `SwingChatClient.disconnect()` писал `Disconnected`.
2. `connection.logout()` закрывал сокет.
3. Reader-поток в `ChatClientConnection` просыпался от закрытия сокета.
4. Он ловил `EOFException`.
5. Даже при нормальном закрытии он отправлял в UI `ErrorResponse("Connection closed")`.

Поэтому появлялись две строки:

```text
Disconnected
Error: Connection closed
```

Теперь исправлено:

- при `EOFException` ошибка отправляется только если соединение не было уже остановлено вручную;
- `disconnect()` сразу выходит, если `connection == null`.
