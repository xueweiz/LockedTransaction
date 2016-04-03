#run cassandra in front
ps auwx | grep cassandra
sudo kill -9 PID

cd LockedTransaction
git stash
git pull

#first cluster
cp ./conf/cassandra01.yaml ./conf/cassandra.yaml  &&  cp ./test/conf/cassandra01.yaml  test/conf/cassandra.yaml
cp ./conf/cassandra02.yaml ./conf/cassandra.yaml  &&  cp ./test/conf/cassandra02.yaml  test/conf/cassandra.yaml
cp ./conf/cassandra03.yaml ./conf/cassandra.yaml  &&  cp ./test/conf/cassandra03.yaml  test/conf/cassandra.yaml
#second cluster
cp ./conf/cassandra04.yaml ./conf/cassandra.yaml  &&  cp ./test/conf/cassandra04.yaml  test/conf/cassandra.yaml
cp ./conf/cassandra05.yaml ./conf/cassandra.yaml  &&  cp ./test/conf/cassandra05.yaml  test/conf/cassandra.yaml
cp ./conf/cassandra06.yaml ./conf/cassandra.yaml  &&  cp ./test/conf/cassandra06.yaml  test/conf/cassandra.yaml
#third cluster
cp ./conf/cassandra07.yaml ./conf/cassandra.yaml  &&  cp ./test/conf/cassandra07.yaml  test/conf/cassandra.yaml
cp ./conf/cassandra08.yaml ./conf/cassandra.yaml  &&  cp ./test/conf/cassandra08.yaml  test/conf/cassandra.yaml
cp ./conf/cassandra09.yaml ./conf/cassandra.yaml  &&  cp ./test/conf/cassandra09.yaml  test/conf/cassandra.yaml
cp ./conf/cassandra10.yaml ./conf/cassandra.yaml  &&  cp ./test/conf/cassandra10.yaml  test/conf/cassandra.yaml

#run cassandra
sudo rm -rf /var/lib/cassandra/* 
rm -rf ./data && bin/cassandra -f

#ssh ip
ssh xzhan160@sp16-cs525-g38-01.cs.illinois.edu

#compile
cd LockedTransaction && ant build

#ssh without password
ssh-copy-id xzhan160@sp16-cs525-g38-01.cs.illinois.edu
ssh-copy-id xzhan160@sp16-cs525-g38-02.cs.illinois.edu
ssh-copy-id xzhan160@sp16-cs525-g38-03.cs.illinois.edu
ssh-copy-id xzhan160@sp16-cs525-g38-04.cs.illinois.edu
ssh-copy-id xzhan160@sp16-cs525-g38-05.cs.illinois.edu
ssh-copy-id xzhan160@sp16-cs525-g38-06.cs.illinois.edu
ssh-copy-id xzhan160@sp16-cs525-g38-07.cs.illinois.edu
ssh-copy-id xzhan160@sp16-cs525-g38-08.cs.illinois.edu
ssh-copy-id xzhan160@sp16-cs525-g38-09.cs.illinois.edu
ssh-copy-id xzhan160@sp16-cs525-g38-10.cs.illinois.edu


01-03
172.22.153.97	(seed)
172.22.153.98
172.22.153.99

04-06
172.22.153.100	(seed)
172.22.153.101
172.22.153.102

07-10
172.22.153.103	(seed)
172.22.153.104
172.22.153.105
172.22.153.106


#open netcat connections
sudo iptables -F

INSERT INTO emp (empID, deptID, first_name, last_name)
VALUES (104, 15, 'jane', 'smith');
INSERT INTO emp (empID, deptID, first_name, last_name)
VALUES (104, 15, 'jane1', 'smith1');



create keyspace mlgb1 with replication = {'class':'SimpleStrategy', 'replication_factor':1};
describe keyspaces
use mlgb;
create table cnm (pid int primary key, name text, value int);
insert into cnm (pid, name, value) values(13, 'xxx', 432);
select * from cnm;
update cnm set name = 'asdf' where pid = 15;


key = 15
key	923567	
timestamp	1459715786943




#ssh key gen
ssh-keygen -t rsa
cat ~/.ssh/id_rsa.pub
#copy to github
#install java and ant
sudo yum install java-1.8.0-openjdk-devel
sudo yum install ant
#checkout from github
git clone git@github.com:XueweiKent/LockedTransaction.git