<<<<<<< HEAD
=======
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
aws s3 cp "/root/$UserName.pem" s3://bastionkeypairs/
adduser $UserName
cd /
cd home/$UserName/
mkdir .ssh
chmod 700 .ssh
chown $UserName .ssh
chown :$UserName .ssh
touch .ssh/authorized_keys
chmod 600 .ssh/authorized_keys
cd .ssh
mv /root/$UserName.pem .
chmod 400 $UserName.pem
ssh-keygen -y -f $UserName.pem > authorized_keys
chown $UserName authorized_keys
chown :$UserName authorized_keys
rm -r $UserName.pem
>>>>>>> eb48c426b303c4dd4a68977518b9b50aa6de898e
