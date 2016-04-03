ssh xzhan160@sp16-cs525-g38-01.cs.illinois.edu
#ssh key gen
ssh-keygen -t rsa
cat ~/.ssh/id_rsa.pub
#copy to github
#install java and ant
sudo yum install java-1.8.0-openjdk-devel
sudo yum install ant
#checkout from github
git clone git@github.com:XueweiKent/LockedTransaction.git

#compile
cd LockedTransaction && ant build

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


#run multi-node cluster
cd ~/LockedTransaction
ps auwx | grep cassandra
sudo kill -9 PID
sudo rm -rf /var/lib/cassandra/*
rm -rf ./data



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

cp ./conf/cassandra07.yaml ./conf/cassandra.yaml
cp ./conf/cassandra08.yaml ./conf/cassandra.yaml
cp ./conf/cassandra09.yaml ./conf/cassandra.yaml
cp ./conf/cassandra10.yaml ./conf/cassandra.yaml

sudo rm -rf /var/lib/cassandra/* && rm -rf ./data && bin/cassandra -f
sudo iptables -F

INSERT INTO emp (empID, deptID, first_name, last_name)
VALUES (104, 15, 'jane', 'smith');

INSERT INTO emp (empID, deptID, first_name, last_name)
VALUES (104, 15, 'jane1', 'smith1');