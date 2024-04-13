rsync -avz --size-only --no-perms --no-times -e "ssh -i ~/.ssh/id-postexchange2" ./target/classes/ postexchange@postexchange.icytools.cn:/www/wwwroot/postexchange.icytools.cn/WEB-INF/classes/
