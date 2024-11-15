# saga-orquestrado

### arquivo build.py
Ao executar o script, executa os comandos necessários para subir os containers  
python .\build.py

### Executar containers
docker compose up -d  

### Buildar containers
docker-compose up --build -d

### Verificar logs de um container específico
docker logs --follow orchestrator-service

### Acessar MongoDB através do container
docker exec -it order-db mongosh "mongodb:admin:123456@localhost27017"

### Parar e remover containers
docker-compose down