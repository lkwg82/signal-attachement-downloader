![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/lkwg82/signal-attachement-downloader/main.yml)
![Latest Release](https://img.shields.io/github/v/tag/lkwg82/signal-attachement-downloader)

This is a little helper to save files (mostly images) from groups outside of signal.

My usecase is to have a family chat and like to have a calendar of the year, so I need to save them outside.

# architecture

2 Containers

- signal-cli: to connect and retrieve the files
- attachment-mover: to collect files and sort them in a specific manner

Both are meant to run as cron job, say every 24 hours.

# build

```
mvn clean verify
```

# run

initially invoke `./run.sh` this will build both containers

## container - signal-cli

Purpose: receive attachments and collect messages in plain json lines in files (to be consumed)

add/set in the file `.env` (is git ignored)

```
USERNAME=+4912345678
```

should result into

```shell
$ docker compose up bot
[+] Running 1/0
 ✔ Container signal-attachment-downloader-bot-1  Recreated                                                                                                                                                                                                                                                       0.1s 
Attaching to bot-1
bot-1  | User +4912345678 is not registered.
bot-1 exited with code 1
```

Linking:

```
$ docker run --rm -ti --entrypoint bash -w /home/user signal-cli
user@95abf5e0f749:~$ signal-cli link -n "optional device name" | tee >(xargs -L 1 qrencode -t utf8)
sgnl://linkdevice?uuid=xMQ044o-MH8NMjAKL2yWZA%3D%3D&pub_key=BV%2B7ymqEm0Cbn8e3Kw8DQjE9TCkfycxD5fusqrZ%2Fk5II
█████████████████████████████████████████████████
█████████████████████████████████████████████████
████ ▄▄▄▄▄ ██ ▄▄ ▄██▀▄ █▄▄ ▄█ ▀█▄█▀█▀█ ▄▄▄▄▄ ████
████ █   █ █▀█  ▄█ ▄ █▀█ ▄▄█▄▀█▄ ██ ▄█ █   █ ████
████ █▄▄▄█ █▄   █▄▄ ▄██▀▄▀█▄▄█ █ ██▄▀█ █▄▄▄█ ████
████▄▄▄▄▄▄▄█▄█ █▄█ ▀ █▄▀ █ ▀ █▄█ █▄█ █▄▄▄▄▄▄▄████
████▄ █▀▀▄▄ █▀▄▄▄ █▀█ ▄▄█  ▄▄▄   ▄█▀ ██▀  ███████
████▄▄▄▀▄█▄▄ ▀█▀▀ ▄ ▄ ▀▀▄▄▀▀ █ ▄██▀█  ▀▀█▀▄▀█████
████▀██▄▄▀▄▄▀▀▄█▀ ███▄▀▄█ █▀▄▀▀ █▄▀▄ █▄█▀ ▀█ ████
████▀▀▄▀██▄█▄ ██▀█▄▄ ▄▄█ ▄ ▀▄▄▄▄███▀▀ ▄  ▄▀▀█████
████ ▄▄▀  ▄ █▄ ▀██▄ ▀█▀▀▄▀▀▀▄▄█ ▄▄▀ ▀█▀▀▀  ██████
█████▄▄█▄▄▄▀██▀ █▄  ▀▄▄▄▀▄ ▄██ ▄██▄ █ ▀█▄▄▄▀▄████
████▄▄█▄ █▄▀▀▀ █▄█████▄██▄▀▄▄ ▄▄█ ▀▀█▀█▀▀▄ ▄█████
████ ▀▀ ██▄▀▄▄█ ▄▄█▀▀█▀ ▀██▀ █ ▄██▄█▄███▄▀█▀▀████
████  ██▄▀▄▄ ▀▀▀▀ ▀█▀▀ ▀▄▀▀▄▄▄██▀▀▀  █  ▀▄   ████
██████ ▄▀ ▄█▀▀▀▀    ▀ ▀█ ▀ ▀▄██▄▄▄▄█ ▀█▀▄█ ▀▀████
████▀▀▄▄▄▀▄▄█ ▄█▀█▄▄▄█▀█▄ ▄ ▀  ▀ ▄▀▄  ▀▄▀█▄▀ ████
████ ▄▄██ ▄▄▄█▄▄▀██▄██▀▄█▄ ▄█▄▀ █▄▀▄ ▀▄ █▀██▄████
████▄█▄█▄█▄▄▀ ▀█▄▄▀ █▄███▄▄█▄█ ▄█▄▄█ ▄▄▄ ▀▄█▄████
████ ▄▄▄▄▄ █ ▀ ▀ █▀▄ █▄▀█▄▀█ ▄█▄█▄▀  █▄█ ▄▄▀█████
████ █   █ ██▀▄▄▄▄ ▄ ▄▄█ ▄ ▀  ▀▀ ▀█▄  ▄▄▄▄█▄▀████
████ █▄▄▄█ █▀▄  ▄█▄▀██  ██▄▀ █▀▄ ██▄▀█▄ ▄▀ █▀████
████▄▄▄▄▄▄▄█▄█▄▄█▄██▄▄█▄▄▄▄▄█▄██▄▄█▄█▄█▄██▄██████
█████████████████████████████████████████████████
█████████████████████████████████████████████████
```

For more about linking see: https://github.com/AsamK/signal-cli/wiki/Linking-other-devices-(Provisioning)

See `docker-compose.yml` for signal-cli config locations
config (https://github.com/AsamK/signal-cli?tab=readme-ov-file#storage)

## container - attachment-mover

Purpose: consume plain json line messages from log and link (hardlink/copy) attachments in separate directory structure.

```shell
$ docker compose up attachment-mover
[+] Running 2/0
 ✔ Container signal-attachment-downloader-init-1              Created                                                                                                                                                                                                                                            0.0s 
 ✔ Container signal-attachment-downloader-attachment-mover-1  Created                                                                                                                                                                                                                                            0.0s 
Attaching to attachment-mover-1
attachment-mover-1  | + export JAVA_TOOL_OPTIONS=-Dfile.encoding=utf8
attachment-mover-1  | + JAVA_TOOL_OPTIONS=-Dfile.encoding=utf8
attachment-mover-1  | + java -jar quarkus-run.jar --moved-attachment-dir /moved_attachments --signal-attachment-dir /signal_attachments --messages-log /output/messages.log --messages-log /output/messages.log.1 --messages-log /output/messages.log.2 --messages-log /output/messages.log.3 --messages-log /output/messages.log.4 --debug
attachment-mover-1  | Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=utf8
attachment-mover-1  | 2025-01-03 20:12:15,627 INFO  [io.quarkus] (main) signal-attachment-downloader 1.0.0-SNAPSHOT on JVM (powered by Quarkus 3.17.5) started in 0.482s. 
attachment-mover-1  | 2025-01-03 20:12:15,636 INFO  [io.quarkus] (main) Profile prod activated. 
attachment-mover-1  | 2025-01-03 20:12:15,636 INFO  [io.quarkus] (main) Installed features: [cdi, picocli]
attachment-mover-1  | 2025-01-03 20:12:15,774 INFO  [de.lgo.sig.att.MainCommand] (main) reading from /output/messages.log,/output/messages.log.1,/output/messages.log.2,/output/messages.log.3,/output/messages.log.4 ...
attachment-mover-1  | 2025-01-03 20:12:15,774 INFO  [de.lgo.sig.att.MainCommand] (main) search in /signal_attachments for attachments
attachment-mover-1  | 2025-01-03 20:12:15,779 INFO  [de.lgo.sig.att.ReactionHandlerFactory] (main) reaction handler map: {?=ReactionHandler{basePath=/moved_attachments,reactionFolder=reactions/calendar}}
attachment-mover-1  | 2025-01-03 20:12:15,789 INFO  [de.lgo.sig.att.MainCommand] (main) line: {"envelope": ...
...
attachment-mover-1  | 2025-01-03 20:13:36,888 INFO  [de.lgo.sig.att.MainCommand] (main) read 176 lines
attachment-mover-1  | 2025-01-03 20:13:36,905 INFO  [io.quarkus] (main) signal-attachment-downloader stopped in 0.017s
attachment-mover-1 exited with code 0
```

target folder is ^^^here `moved_attachments`.

for wiring see `docker-compose.yml` it is the most minified version of working setup.

# release

```
DRY_RUN=0 ./release.sh
```

# develop

## 1. fill `messages.log`

create `.env` with `USERNAME=⁺49123456` (your signal registered phone number)

```
docker run --rm -i -v $HOME/.local/share/signal-cli:/home/user/.local/share/signal-cli -v ./:/output --env-file=.env signal-cli
```

## 2. parse `messages.log`

run `src/main/java/de/lgohlke/signal/attachmentdownloader/QuarkusApp.java`
