rsync -avz --size-only --no-perms --no-times -e "ssh -i ~/.ssh/id-postexchange2" ./target/PostExchange-1.0-SNAPSHOT/ postexchange@postexchange.icytools.cn:/www/wwwroot/postexchange.icytools.cn/
