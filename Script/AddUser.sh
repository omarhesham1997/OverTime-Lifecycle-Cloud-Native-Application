#!/bin/sh
read -p "Enter User Name: " UserName
getent passwd $UserName
var=$?
while [ $var -eq 0 ]
do
  read -p "Name Exists, Please Enter Unique Name: " UserName
  getent passwd $UserName
  var=$?
done
aws ec2 create-key-pair --key-name $UserName > temp.pem
sed '1d;23d' temp.pem > temp1.pem
cp FixedFile.pem temp.pem
sed -e "1r temp1.pem" temp.pem > $UserName.pem
rm -r temp.pem
rm -r temp1.pem
aws s3 cp "/home/ec2-user/$UserName.pem" s3://bastionkeypairs/
sudo adduser $UserName --disabled-password
sudo su -l $UserName
mkdir .ssh
cd .ssh
//error
mv /home/ec2-user/$UserName.pem .
ssh-keygen -y -f $UserName.pem > authorized_keys
rm -r $UserName.pem
yes
cd ../
exit
