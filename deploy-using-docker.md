# Docker basics
Docker made the container concepts very light compare to VM.

Follow the instruction to install on specifc OS type   - https://docs.docker.com/desktop/


Containers are actually processed in a Linux host that uses

- **Linux namespace** (which provides the isolation between conatiners)
- **Linux Control Groups**(Which used to limit the amount of CPU)

*Running an interactive shell in a docker container - `docker exec -it container-name sh`*

### Frequently used command 

docker images -a | grep "backend" | awk '{print $3}'

**Removing images according to pattern**

`docker images -a | grep "pattern" | awk '{print $3}' | xargs docker rmi`

**Clean unwanted Container**

`docker rm -f $(docker ps -aq)`

**docker remove images without tag**

docker rmi $(docker images --filter "dangling=true" -q --no-trunc)
