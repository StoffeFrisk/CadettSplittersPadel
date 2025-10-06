@echo off
echo Stopping cadett-splitters-padel
docker stop cadett-splitters-padel
echo Deleting container cadett-splitters-padel
docker rm cadett-splitters-padel
echo Deleting image cadett-splitters-padel
docker rmi cadett-splitters-padel
echo Running mvn package
call mvn package
echo Creating image cadett-splitters-padel
docker build -t cadett-splitters-padel .
echo Creating and running container cadett-splitters-padel
docker run -d -p 7575:7575 --name cadett-splitters-padel --network cadett-splitters-net cadett-splitters-padel
echo Done!