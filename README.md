# mstShield

mstShield - это плагин который позволяет поставить щиту противника кулдаун различными предметами

## Особенности

- Поддержка версий Minecraft 1.16+
- Настраиваемое время задержки для каждого предмета
- Возможность включать/отключать предметы
- Система проверки обновлений
- Поддержка прав доступа
- Простая конфигурация

## Установка

1. Скачайте последнюю версию плагина из [релизов](https://github.com/mistaste/mstShield/releases)
2. Поместите JAR файл в папку `plugins` вашего сервера
3. Перезапустите сервер
4. Настройте конфигурацию в файле `plugins/mstShield/config.yml`

## Конфигурация

```yaml
# Список предметов и их параметры
items:
  - material: WOODEN_AXE
    cooldown: 5.0
  - material: STONE_AXE
    cooldown: 6.0
```

## Команды

- `/mstshield reload` - Перезагрузить конфигурацию плагина

## Права доступа

- `mstshield.use` - Право на использование команд плагина
- `mstshield.bypass` - Право на байпас

## Для разработчиков

### Сборка из исходного кода

1. Клонируйте репозиторий
```bash
git clone https://github.com/mistaste/mstShield.git
```

2. Соберите проект с помощью Maven
```bash
mvn clean package
```