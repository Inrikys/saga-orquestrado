# saga-orquestrado

### Executar containers
docker compose up -d  

### Acessar MongoDB através do container
docker exec -it order-db mongosh "mongodb:admin:123456@localhost27017"