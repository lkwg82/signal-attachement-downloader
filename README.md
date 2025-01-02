# build

```
mvn clean verify
```

# run

```
USERNAME=<your signal phone number> ./run.sh
```

# release

```
RELEASE=1 ./run.sh
```

# develop

## 1. fill `messages.log`

create `.env` with `USERNAME=⁺49123456` (your signal registered phone number)

```
docker run --rm -i -v $HOME/.local/share/signal-cli:/home/user/.local/share/signal-cli -v ./:/output --env-file=.env signal-cli
```

## 2. parse `messages.log`

run `src/main/java/de/lgohlke/signal/attachmentdownloader/QuarkusApp.java`