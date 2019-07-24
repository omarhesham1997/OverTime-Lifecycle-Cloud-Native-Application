provider "aws" {
  region = "us-east-1"
}

//vpc

resource "aws_vpc" "OverTime-LifeCycle" {

  cidr_block       = "10.0.0.0/24"
  instance_tenancy = "default"

  tags {
    Name = "OverTime-LifeCycle"
  }
}

//SUBNETS

resource "aws_subnet" "Networking-Public" {
  vpc_id     = "${aws_vpc.OverTime-LifeCycle.id}"
  cidr_block = "10.0.0.64/26"

  tags {
    Name = "Networking-Public"
  }
}

resource "aws_subnet" "DEV1-RDS" {
  vpc_id     = "${aws_vpc.OverTime-LifeCycle.id}"
  cidr_block = "10.0.0.0/28"

  tags {
    Name = "DEV1-RDS"
  }
}


resource "aws_subnet" "DEV2-RDS" {
  vpc_id     = "${aws_vpc.OverTime-LifeCycle.id}"
  cidr_block = "10.0.0.16/28"

  tags {
    Name = "DEV2-RDS"
  }
}


resource "aws_subnet" "TEST1-RDS" {
  vpc_id     = "${aws_vpc.OverTime-LifeCycle.id}"
  cidr_block = "10.0.0.32/18"

  tags {
    Name = "TEST1-RDS"
  }
}


resource "aws_subnet" "TEST2-RDS" {
  vpc_id     = "${aws_vpc.OverTime-LifeCycle.id}"
  cidr_block = "10.0.0.48/28"

  tags {
    Name = "TEST2-RDS"
  }
}


resource "aws_subnet" "Orch-VM" {
  vpc_id     = "${aws_vpc.OverTime-LifeCycle.id}"
  cidr_block = "10.0.0.128/28"

  tags {
    Name = "Orch-VM"
  }
}


//elastic ip

resource "aws_eip" "nat" {
  vpc      = true
  
  tags = {
    Name = "nat"
  }
}


//Internet Gateway

resource "aws_internet_gateway" "igw" {
  vpc_id = "${aws_vpc.OverTime-LifeCycle.id}"

  tags {
    Name = "igw"
  }
}

//Nating

resource "aws_nat_gateway" "ngw" {
  vpc_id = "${aws_vpc.OverTime-LifeCycle.id}"
  allocation_id = "${aws_eip.nat.id}"
  subnet_id     = "${aws_subnet.Networking-Public.id}"

  tags {
    Name = "ngw"
  }
}

//Route Tables

resource "aws_route_table" "Custom-RT" {
  vpc_id = "${aws_vpc.OverTime-LifeCycle.id}"

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.igw.id}"
  }

  tags {
    Name = "Custom-RT"
  }
}


resource "aws_route_table" "Main-RT" {
  vpc_id = "${aws_vpc.OverTime-LifeCycle.id}"

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_nat_gateway.ngw.id}"
  }

  tags {
    Name = "Main-RT"
  }
}

//Route Tables Association 

resource "aws_route_table_association" "Custom" {
  subnet_id      = "${aws_subnet.Networking-Public.id}"
  route_table_id = "${aws_route_table.Custom-RT.id}"
}


resource "aws_route_table_association" "Main" {     
  subnet_id     = ["${aws_subnet.DEV1-RDS.id}","${aws_subnet.DEV2-RDS.id}","${aws_subnet.TEST1-RDS.id}","${aws_subnet.TEST2-RDS.id}","${aws_subnet.Orch-VM.id}"]
  route_table_id = "${aws_route_table.Main-RT.id}"
}



//Security Groups

resource "aws_security_group" "Bastion-SG" {
  vpc_id                 = "${aws_vpc.OverTime-LifeCycle.id}"

  ingress {
    protocol   = "tcp"
    cidr_block = "0.0.0.0/0"
    from_port  = 22
    to_port    = 22
  }

  egress {
    protocol   = "tcp"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535
  }
  
  tags {
    Name = "Bastion-SG"
  }
}


resource "aws_security_group" "ORCH-VM-SG" {
  vpc_id                 = "${aws_vpc.OverTime-LifeCycle.id}"

  ingress {
    protocol   = "tcp"
    cidr_block = "10.0.0.126/32"
    from_port  = 22
    to_port    = 22
  }

  egress {
    protocol   = "tcp"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535
  }
  
  tags {
    Name = "ORCH-VM-SG"
  }
}


resource "aws_security_group" "DEV-RDS-SG" {
  vpc_id                 = "${aws_vpc.OverTime-LifeCycle.id}"

  ingress {
    protocol   = "tcp"
    cidr_block = "10.0.0.133/32"
    from_port  = 3306
    to_port    = 3306
  }
  
  ingress {
    protocol   = "tcp"
    cidr_block = "10.0.0.126/32"
    from_port  = 3306
    to_port    = 3306
  }

  egress {
    protocol   = "tcp"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535
  }
  
  tags {
    Name = "DEV-RDS-SG"
  }
}


resource "aws_security_group" "TEST-RDS-SG" {
  vpc_id                 = "${aws_vpc.OverTime-LifeCycle.id}"

  ingress {
    protocol   = "tcp"
    cidr_block = "10.0.0.133/32"
    from_port  = 3306
    to_port    = 3306
  }
  
  ingress {
    protocol   = "tcp"
    cidr_block = "10.0.0.126/32"
    from_port  = 3306
    to_port    = 3306
  }

  egress {
    protocol   = "tcp"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535
  }
  
  tags {
    Name = "TEST-RDS-SG"
  }
}




//EC2
resource "aws_instance" "Orch-VM"{
	ami                    = "ami-026c8acd92718196b"
  vpc_id               = "${aws_vpc.OverTime-LifeCycle.id}"
	subnet_id              ="${aws_subnet.Orch-VM.id}"
	vpc_security_group_ids = "${aws_security_group.Bastion-SG.id}"
	private_ip 			   = "10.0.0.133"
	root_block_device {
    volume_type           = "gp2"
    volume_size           = 20
    delete_on_termination = true
  }
  root_block_device = {
    volume_type           = "gp2"
    volume_size           = 10
    delete_on_termination = true
  }
	tags {
	Name = "Orch-VM"
	}
}

resource "aws_instance" "Bastion"{
	ami                    = "ami-0b898040803850657"
  vpc_id                 = "${aws_vpc.OverTime-LifeCycle.id}"
	subnet_id              = "${aws_subnet.Networking-Public.id}"
	vpc_security_group_ids = "${aws_security_group.ORCH-VM-SG.id}"
	private_ip 			       = "10.0.0.126"
	root_block_device {
    volume_type           = "gp2"
    volume_size           = 8
    delete_on_termination = true
  }

	tags {
	Name = "Bastion"
	}
}







//NACLS

resource "aws_network_acl" "Public-Network" {
  vpc_id     = "${aws_vpc.OverTime-LifeCycle.id}"
  subnet_ids = "${aws_subnet.Networking-Public.id}"

  egress {
    protocol   = "tcp"
    rule_no    = 1
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535
  }

  ingress {
    protocol   = "tcp"
    rule_no    = 1
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535
  }

  tags {
    Name = "Public-Network"
  }
}


resource "aws_network_acl" "DEV_RDS" {
  vpc_id     = "${aws_vpc.OverTime-LifeCycle.id}"
  subnet_ids = ["${aws_subnet.DEV1-RDS.id}","${aws_subnet.DEV2-RDS.id}",]
  
  egress {
    protocol   = "tcp"
    rule_no    = 1
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535
  }

  ingress {
    protocol   = "tcp"
    rule_no    = 1
    action     = "allow"
    cidr_block = "10.0.0.133/32"
    from_port  = 3306
    to_port    = 3306
  }
  ingress {
    protocol   = "tcp"
    rule_no    = 2
    action     = "allow"
    cidr_block = "10.0.0.126/32"
    from_port  = 3306
    to_port    = 3306
  }

  tags {
    Name = "DEV_RDS"
  }
}



resource "aws_network_acl" "TEST-RDS" {
  vpc_id     = "${aws_vpc.OverTime-LifeCycle.id}"
  subnet_ids = ["${aws_subnet.TEST1-RDS.id}","${aws_subnet.TEST2-RDS.id}",]
  
  egress {
    protocol   = "tcp"
    rule_no    = 1
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535
  }

  ingress {
    protocol   = "tcp"
    rule_no    = 1
    action     = "allow"
    cidr_block = "10.0.0.133/32"
    from_port  = 3306
    to_port    = 3306
  }
  ingress {
    protocol   = "tcp"
    rule_no    = 2
    action     = "allow"
    cidr_block = "10.0.0.126/32"
    from_port  = 3306
    to_port    = 3306
  }

  tags {
    Name = "TEST-RDS"
  }
}


resource "aws_network_acl" "ORCH-VM" {
  vpc_id = "${aws_vpc.OverTime-LifeCycle.id}"
  subnet_ids = "${aws_subnet.Orch-VM.id}"
  egress {
    protocol   = "tcp"
    rule_no    = 1
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535
  }

  ingress {
    protocol   = "tcp"
    rule_no    = 1
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535
  }
  ingress {
    protocol   = "tcp"
    rule_no    = 2
    action     = "allow"
    cidr_block = "10.0.0.126/32"
    from_port  = 22
    to_port    = 22
  }

  tags {
    Name = "ORCH-VM"
  }
}



//IAM Groups

resource "aws_iam_group" "AWS-Admin" {
  name = "AWS-Admin"
}
resource "aws_iam_group_policy_attachment" "Admin" {
  group      = "${aws_iam_group.AWS-Admin.name}"
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}


resource "aws_iam_group" "Developer" {
  name = "Developer"
}
resource "aws_iam_group_policy_attachment" "Dev" {
  group      = "${aws_iam_group.Developer.name}"
  policy_arn = "arn:aws:iam::aws:policy/AWSLambdaFullAccess"
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
  policy_arn = "arn:aws:iam::aws:policy/AmazonRDSDataFullAccess"
  policy_arn = "arn:aws:iam::aws:policy/AmazonSESFullAccess"
}

resource "aws_iam_group" "Tester" {
  name = "Tester"
}
resource "aws_iam_group_policy_attachment" "Test" {
  group      = "${aws_iam_group.Tester.name}"
  policy_arn = "arn:aws:iam::aws:policy/AWSLambdaFullAccess"
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
  policy_arn = "arn:aws:iam::aws:policy/AmazonRDSDataFullAccess"
  policy_arn = "arn:aws:iam::aws:policy/AmazonSESFullAccess"
}


//Users

resource "aws_iam_user" "w" {
  name = "Wali"
}
resource "aws_iam_user" "o" {
  name = "Omar"
}
resource "aws_iam_user" "r" {
  name = "Reham"
}
resource "aws_iam_user_group_membership" "ADMIN" {
  user = ["${aws_iam_user.w.name}","${aws_iam_user.o.name}","${aws_iam_user.r.name}",]

  groups = "${aws_iam_group.AWS-Admin.name}"
}


resource "aws_iam_user" "a" {
  name = "Abdelrahman-Hesham"
}
resource "aws_iam_user" "r" {
  name = "Randa"
}
resource "aws_iam_user" "n" {
  name = "Nour"
}
resource "aws_iam_user" "m" {
  name = "Merna"
}
resource "aws_iam_user_group_membership" "ADMIN" {
  user = ["${aws_iam_user.a.name}","${aws_iam_user.r.name}","${aws_iam_user.n.name}","${aws_iam_user.m.name}",]

  groups = "${aws_iam_group.Developer.name}"
}
resource "aws_iam_user_group_membership" "ADMIN" {
  user = ["${aws_iam_user.w.name}","${aws_iam_user.o.name}","${aws_iam_user.r.name}",]

  groups = "${aws_iam_group.Tester.name}"
}



//RDS

resource "aws_db_subnet_group" "dev-rds-group" {
  name       = "dev-rds-group"
  subnet_ids = ["${aws_subnet.DEV1-RDS.id}", "${aws_subnet.DEV2-RDS.id}"]
}
resource "aws_db_instance" "dev-rds" {
  allocated_storage     = 20
  max_allocated_storage = 1000
  storage_type          = "gp2"
  engine                = "mysql"
  engine_version        = "5.7.22"
  instance_class        = "db.t2.micro"
  name                  = "OTLifeCycle"
  username              = "wali96"
  password              = "wali_9696"
  parameter_group_name  = "default.mysql5.7"
  db_subnet_group_name  = "dev-rds-group"
  security_group_names  = "${aws_security_group.DEV-RDS-SG.id}"
}


resource "aws_db_subnet_group" "test-rds-group" {
  name       = "dev-rds-group"
  subnet_ids = ["${aws_subnet.TEST1-RDS.id}", "${aws_subnet.TEST2-RDS.id}"]
}
resource "aws_db_instance" "test-rds" {
  allocated_storage     = 20
  max_allocated_storage = 1000
  storage_type          = "gp2"
  engine                = "mysql"
  engine_version        = "5.7.22"
  instance_class        = "db.t2.micro"
  name                  = "TestRDS"
  username              = "wali96"
  password              = "wali_9696"
  parameter_group_name  = "default.mysql5.7"
  db_subnet_group_name  = "test-rds-group"
  security_group_names  = "${aws_security_group.TEST-RDS-SG.id}"
}